package cn.keking.service.impl;

import cn.keking.model.FileAttribute;
import cn.keking.service.FileHandlerService;
import cn.keking.service.FilePreview;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OfficeFilePreviewMockingTests {

    @Test
    void getPreviewType_shouldCallPdf2jpgAndPopulateModel() throws Exception {
        // Arrange: pdf2jpg returns generated image URLs.
        FileHandlerService fileHandlerService = mock(FileHandlerService.class);
        OtherFilePreviewImpl otherFilePreview = mock(OtherFilePreviewImpl.class);
        FileAttribute fileAttribute = new FileAttribute();
        fileAttribute.setSuffix("docx");
        Model model = new ExtendedModelMap();

        when(fileHandlerService.pdf2jpg("out.pdf", "out.pdf", "cache.pdf", fileAttribute))
                .thenReturn(List.of("/img/0.jpg", "/img/1.jpg"));

        String result = OfficeFilePreviewImpl.getPreviewType(
                model,
                fileAttribute,
                OfficeFilePreviewImpl.OFFICE_PREVIEW_TYPE_IMAGE,
                "cache.pdf",
                "out.pdf",
                fileHandlerService,
                OfficeFilePreviewImpl.OFFICE_PREVIEW_TYPE_IMAGE,
                otherFilePreview
        );

        // Assert both state (model) and interaction (dependency calls).
        assertEquals(FilePreview.OFFICE_PICTURE_FILE_PREVIEW_PAGE, result);
        assertEquals("/img/0.jpg", model.getAttribute("currentUrl"));
        verify(fileHandlerService).pdf2jpg("out.pdf", "out.pdf", "cache.pdf", fileAttribute);
        verifyNoInteractions(otherFilePreview);
    }

    @Test
    void getPreviewType_shouldDelegateToOtherPreviewWhenImageListEmpty() throws Exception {
        // Arrange: conversion returns no images so fallback path should be used.
        FileHandlerService fileHandlerService = mock(FileHandlerService.class);
        OtherFilePreviewImpl otherFilePreview = mock(OtherFilePreviewImpl.class);
        FileAttribute fileAttribute = new FileAttribute();
        fileAttribute.setSuffix("docx");
        Model model = new ExtendedModelMap();

        when(fileHandlerService.pdf2jpg("out.pdf", "out.pdf", "cache.pdf", fileAttribute))
                .thenReturn(Collections.emptyList());
        when(otherFilePreview.notSupportedFile(any(Model.class), eq(fileAttribute), contains("office转图片异常")))
                .thenReturn(FilePreview.NOT_SUPPORTED_FILE_PAGE);

        String result = OfficeFilePreviewImpl.getPreviewType(
                model,
                fileAttribute,
                OfficeFilePreviewImpl.OFFICE_PREVIEW_TYPE_IMAGE,
                "cache.pdf",
                "out.pdf",
                fileHandlerService,
                OfficeFilePreviewImpl.OFFICE_PREVIEW_TYPE_IMAGE,
                otherFilePreview
        );

        // Assert fallback delegation happens with expected message semantics.
        assertEquals(FilePreview.NOT_SUPPORTED_FILE_PAGE, result);
        assertNull(model.getAttribute("currentUrl"));
        verify(fileHandlerService).pdf2jpg("out.pdf", "out.pdf", "cache.pdf", fileAttribute);
        verify(otherFilePreview).notSupportedFile(any(Model.class), eq(fileAttribute), contains("office转图片异常"));
    }
}
