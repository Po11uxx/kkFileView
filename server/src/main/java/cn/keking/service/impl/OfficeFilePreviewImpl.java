package cn.keking.service.impl;

import cn.keking.config.ConfigConstants;
import cn.keking.model.FileAttribute;
import cn.keking.model.ReturnResponse;
import cn.keking.service.FileHandlerService;
import cn.keking.service.FilePreview;
import cn.keking.service.OfficeToPdfService;
import cn.keking.utils.DownloadUtils;
import cn.keking.utils.KkFileUtils;
import cn.keking.utils.OfficeUtils;
import cn.keking.utils.WebUtils;
import cn.keking.web.filter.BaseUrlFilter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.EncryptedDocumentException;
import org.jodconverter.core.office.OfficeException;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * Created by kl on 2018/1/17.
 * Content :处理office文件
 */
@Service
public class OfficeFilePreviewImpl implements FilePreview {

    public static final String OFFICE_PREVIEW_TYPE_IMAGE = "image";
    public static final String OFFICE_PREVIEW_TYPE_ALL_IMAGES = "allImages";
    private static final String OFFICE_PASSWORD_MSG = "password";

    private final FileHandlerService fileHandlerService;
    private final OfficeToPdfService officeToPdfService;
    private final OtherFilePreviewImpl otherFilePreview;

    public OfficeFilePreviewImpl(FileHandlerService fileHandlerService, OfficeToPdfService officeToPdfService, OtherFilePreviewImpl otherFilePreview) {
        this.fileHandlerService = fileHandlerService;
        this.officeToPdfService = officeToPdfService;
        this.otherFilePreview = otherFilePreview;
    }

    @Override
    public String filePreviewHandle(String url, Model model, FileAttribute fileAttribute) {
        // Use request-level preview type when provided; otherwise fall back to global default.
        String officePreviewType = fileAttribute.getOfficePreviewType();
        boolean userToken = fileAttribute.getUsePasswordCache();
        String baseUrl = BaseUrlFilter.getBaseUrl();
        String suffix = fileAttribute.getSuffix();  // file extension
        String fileName = fileAttribute.getName(); // original file name
        String filePassword = fileAttribute.getFilePassword(); // optional password for encrypted office files
        boolean forceUpdatedCache=fileAttribute.forceUpdatedCache();  // bypass conversion cache
        boolean isHtmlView = fileAttribute.isHtmlView();  // render xlsx as html when enabled
        String cacheName = fileAttribute.getCacheName();  // converted file cache key
        String outFilePath = fileAttribute.getOutFilePath();  // converted output path
        if (!officePreviewType.equalsIgnoreCase("html")) {
            if (ConfigConstants.getOfficeTypeWeb() .equalsIgnoreCase("web")) {
                if (suffix.equalsIgnoreCase("xlsx")) {
                    model.addAttribute("pdfUrl", KkFileUtils.htmlEscape(url)); //特殊符号处理
                    return XLSX_FILE_PREVIEW_PAGE;
                }
                if (suffix.equalsIgnoreCase("csv")) {
                    model.addAttribute("csvUrl", KkFileUtils.htmlEscape(url));
                    return CSV_FILE_PREVIEW_PAGE;
                }
            }
        }
        if (forceUpdatedCache|| !fileHandlerService.listConvertedFiles().containsKey(cacheName) || !ConfigConstants.isCacheEnabled()) {
        // Download source file once and reuse local copy when available.
        ReturnResponse<String> response = DownloadUtils.downLoad(fileAttribute, fileName);
        if (response.isFailure()) {
            return otherFilePreview.notSupportedFile(model, fileAttribute, response.getMsg());
        }
            String filePath = response.getContent();
            boolean  isPwdProtectedOffice =  OfficeUtils.isPwdProtected(filePath);    // detect encryption upfront
            if (isPwdProtectedOffice && !StringUtils.hasLength(filePassword)) {
                // Ask user for password before attempting conversion.
                model.addAttribute("needFilePassword", true);
                return EXEL_FILE_PREVIEW_PAGE;
            } else {
                if (StringUtils.hasText(outFilePath)) {
                    try {
                        officeToPdfService.openOfficeToPDF(filePath, outFilePath, fileAttribute);
                    } catch (OfficeException e) {
                        if (isPwdProtectedOffice && !OfficeUtils.isCompatible(filePath, filePassword)) {
                            // Password provided but incompatible; prompt re-entry.
                            model.addAttribute("needFilePassword", true);
                            model.addAttribute("filePasswordError", true);
                            return EXEL_FILE_PREVIEW_PAGE;
                        }
                        return otherFilePreview.notSupportedFile(model, fileAttribute, "抱歉，该文件版本不兼容，文件版本错误。");
                    }
                    if (isHtmlView) {
                        // Normalize encoding after conversion for HTML rendering.
                        fileHandlerService.doActionConvertedFile(outFilePath);
                    }
                    //是否保留OFFICE源文件
//                    if (!fileAttribute.isCompressFile() && ConfigConstants.getDeleteSourceFile()) {
//                        KkFileUtils.deleteFileByPath(filePath);
//                    }
                    if (userToken || !isPwdProtectedOffice) {
                        // Cache the converted target so follow-up previews skip conversion.
                        fileHandlerService.addConvertedFile(cacheName, fileHandlerService.getRelativePath(outFilePath));
                    }
                }
            }

        }
        if (!isHtmlView && baseUrl != null && (OFFICE_PREVIEW_TYPE_IMAGE.equals(officePreviewType) || OFFICE_PREVIEW_TYPE_ALL_IMAGES.equals(officePreviewType))) {
            // Convert generated PDF to images when image-style office preview is requested.
            return getPreviewType(model, fileAttribute, officePreviewType, cacheName, outFilePath, fileHandlerService, OFFICE_PREVIEW_TYPE_IMAGE, otherFilePreview);
        }
        model.addAttribute("pdfUrl", WebUtils.encodeFileName(cacheName));  //输出转义文件名 方便url识别
        return isHtmlView ? EXEL_FILE_PREVIEW_PAGE : PDF_FILE_PREVIEW_PAGE;
    }

    static String getPreviewType(Model model, FileAttribute fileAttribute, String officePreviewType, String pdfName, String outFilePath, FileHandlerService fileHandlerService, String officePreviewTypeImage, OtherFilePreviewImpl otherFilePreview) {
        String suffix = fileAttribute.getSuffix();
        boolean isPPT = suffix.equalsIgnoreCase("ppt") || suffix.equalsIgnoreCase("pptx");
        List<String> imageUrls = null;
        try {
            // Reuse PDF output path as both source and target folder for generated preview images.
            imageUrls =  fileHandlerService.pdf2jpg(outFilePath,outFilePath, pdfName, fileAttribute);
        } catch (Exception e) {
            Throwable[] throwableArray = ExceptionUtils.getThrowables(e);
            for (Throwable throwable : throwableArray) {
                if (throwable instanceof IOException || throwable instanceof EncryptedDocumentException) {
                    if (e.getMessage().toLowerCase().contains(OFFICE_PASSWORD_MSG)) {
                        model.addAttribute("needFilePassword", true);
                        return EXEL_FILE_PREVIEW_PAGE;
                    }
                }
            }
        }
        if (imageUrls == null || imageUrls.size() < 1) {
            return otherFilePreview.notSupportedFile(model, fileAttribute, "office转图片异常，请联系管理员");
        }
        model.addAttribute("imgUrls", imageUrls);
        model.addAttribute("currentUrl", imageUrls.get(0));
        if (officePreviewTypeImage.equals(officePreviewType)) {
            // PPT 图片模式使用专用预览页面
            return (isPPT ? PPT_FILE_PREVIEW_PAGE : OFFICE_PICTURE_FILE_PREVIEW_PAGE);
        } else {
            return PICTURE_FILE_PREVIEW_PAGE;
        }
    }

}
