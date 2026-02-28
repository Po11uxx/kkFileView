package cn.keking.service;

import cn.keking.model.FileAttribute;
import cn.keking.model.FileType;
import cn.keking.service.cache.CacheService;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileConvertQueueTaskStubbingTests {

    @Test
    void runOnce_shouldUseStubbedDependenciesForOfficeConversion() {
        // Arrange: queue returns one office URL and preview dependency is fully stubbed.
        StubCacheService cacheService = new StubCacheService("https://example.com/a.docx");
        RecordingFilePreview recordingFilePreview = new RecordingFilePreview();
        StubFilePreviewFactory previewFactory = new StubFilePreviewFactory(recordingFilePreview);
        FileHandlerService fileHandlerService = new StubFileHandlerService(cacheService, FileType.OFFICE);

        TestableConvertTask task = new TestableConvertTask(previewFactory, cacheService, fileHandlerService);
        task.runOnce();

        // Assert: conversion path delegates to preview exactly once.
        assertEquals(1, recordingFilePreview.invokeCount);
        assertEquals("https://example.com/a.docx", recordingFilePreview.lastUrl);
        assertFalse(task.sleepCalled, "success path should not sleep");
    }

    @Test
    void runOnce_shouldSkipPreviewForTypeWithoutConversion() {
        StubCacheService cacheService = new StubCacheService("https://example.com/a.pdf");
        RecordingFilePreview recordingFilePreview = new RecordingFilePreview();
        StubFilePreviewFactory previewFactory = new StubFilePreviewFactory(recordingFilePreview);
        FileHandlerService fileHandlerService = new StubFileHandlerService(cacheService, FileType.PDF);

        TestableConvertTask task = new TestableConvertTask(previewFactory, cacheService, fileHandlerService);
        task.runOnce();

        // Non-convertible types should be filtered before reaching preview handlers.
        assertEquals(0, recordingFilePreview.invokeCount);
    }

    @Test
    void runOnce_shouldBeDeterministicWhenQueueThrows() {
        StubCacheService cacheService = new StubCacheService("https://example.com/a.docx");
        cacheService.throwWhenTaking = true;
        RecordingFilePreview recordingFilePreview = new RecordingFilePreview();
        StubFilePreviewFactory previewFactory = new StubFilePreviewFactory(recordingFilePreview);
        FileHandlerService fileHandlerService = new StubFileHandlerService(cacheService, FileType.OFFICE);

        TestableConvertTask task = new TestableConvertTask(previewFactory, cacheService, fileHandlerService);
        task.runOnce();

        // Failure branch should call the sleep hook without invoking real conversion logic.
        assertTrue(task.sleepCalled, "failure path should call sleep hook");
        assertEquals(0, recordingFilePreview.invokeCount);
    }

    static class TestableConvertTask extends FileConvertQueueTask.ConvertTask {
        boolean sleepCalled;

        TestableConvertTask(FilePreviewFactory previewFactory, CacheService cacheService, FileHandlerService fileHandlerService) {
            super(previewFactory, cacheService, fileHandlerService);
        }

        @Override
        protected void sleepOnFailure() {
            // Avoid real waiting in tests, but keep an observable marker for assertions.
            sleepCalled = true;
        }
    }

    static class StubFilePreviewFactory extends FilePreviewFactory {
        private final FilePreview filePreview;

        StubFilePreviewFactory(FilePreview filePreview) {
            super(null);
            this.filePreview = filePreview;
        }

        @Override
        public FilePreview get(FileAttribute fileAttribute) {
            // Always return the same stub preview to isolate queue-task logic from preview selection.
            return filePreview;
        }
    }

    static class StubFileHandlerService extends FileHandlerService {
        private final FileType type;

        StubFileHandlerService(CacheService cacheService, FileType type) {
            super(cacheService);
            this.type = type;
        }

        @Override
        public FileAttribute getFileAttribute(String url, HttpServletRequest req) {
            // Return a minimal, deterministic FileAttribute for this test scenario.
            FileAttribute fileAttribute = new FileAttribute();
            fileAttribute.setType(type);
            fileAttribute.setUrl(url);
            fileAttribute.setName("stub-name");
            fileAttribute.setSuffix("docx");
            return fileAttribute;
        }
    }

    static class RecordingFilePreview implements FilePreview {
        int invokeCount;
        String lastUrl;

        @Override
        public String filePreviewHandle(String url, org.springframework.ui.Model model, FileAttribute fileAttribute) {
            // Record calls to verify whether conversion flow reached preview handling.
            invokeCount++;
            lastUrl = url;
            return PDF_FILE_PREVIEW_PAGE;
        }
    }

    static class StubCacheService implements CacheService {
        private final String taskUrl;
        boolean throwWhenTaking;

        StubCacheService(String taskUrl) {
            this.taskUrl = taskUrl;
        }

        @Override
        public String takeQueueTask() throws InterruptedException {
            if (throwWhenTaking) {
                // Simulate queue access interruption to validate failure-path behavior.
                throw new InterruptedException("stubbed queue failure");
            }
            return taskUrl;
        }

        @Override
        public void initPDFCachePool(Integer capacity) { }

        @Override
        public void initIMGCachePool(Integer capacity) { }

        @Override
        public void initPdfImagesCachePool(Integer capacity) { }

        @Override
        public void initMediaConvertCachePool(Integer capacity) { }

        @Override
        public void putPDFCache(String key, String value) { }

        @Override
        public void putImgCache(String key, List<String> value) { }

        @Override
        public Map<String, String> getPDFCache() { return null; }

        @Override
        public String getPDFCache(String key) { return null; }

        @Override
        public Map<String, List<String>> getImgCache() { return null; }

        @Override
        public List<String> getImgCache(String key) { return null; }

        @Override
        public Integer getPdfImageCache(String key) { return null; }

        @Override
        public void putPdfImageCache(String pdfFilePath, int num) { }

        @Override
        public Map<String, String> getMediaConvertCache() { return null; }

        @Override
        public void putMediaConvertCache(String key, String value) { }

        @Override
        public String getMediaConvertCache(String key) { return null; }

        @Override
        public void cleanCache() { }

        @Override
        public void addQueueTask(String url) { }
    }
}
