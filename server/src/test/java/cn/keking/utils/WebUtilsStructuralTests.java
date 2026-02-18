package cn.keking.utils;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class WebUtilsStructuralTests {

    @Test
    void encodeAndClearParamAndSuffixChecks() {
        assertEquals("my%20file.txt", WebUtils.encodeFileName("my file.txt"));

        String withFullName = "https://host/a.docx?k=v&fullfilename=test%20name.docx&x=1";
        assertEquals("https://host/a.docx?k=v&x=1", WebUtils.clearFullfilenameParam(withFullName));

        assertEquals("docx", WebUtils.suffixFromUrl("https://host/path/name.docx?x=1"));
        assertTrue(WebUtils.isValidUrl("https://host/file.txt"));
        assertFalse(WebUtils.isValidUrl("host/file.txt"));
    }

    @Test
    void urlEncoderencodeAndFileNameExtraction() {
        String url = "https://host/path/hello world.txt?x=1";
        String encoded = WebUtils.urlEncoderencode(url);
        assertNotNull(encoded);
        assertTrue(encoded.contains("%20"));

        String badUrl = "https://host/path/ok.txt?fullfilename=../evil.txt";
        assertNull(WebUtils.urlEncoderencode(badUrl));

        assertEquals("name.txt", WebUtils.getFileNameFromURL("https://a/b/name.txt?token=1"));
        assertEquals("data.xls", WebUtils.getFileNameFromURL("file:///tmp/data.xls"));

        String fileNameEncoded = WebUtils.encodeUrlFileName("https://x/y/hello world.txt?p=1");
        assertTrue(fileNameEncoded.contains("hello+world.txt"));
        assertEquals("https://x/y/no_extension", WebUtils.encodeUrlFileName("https://x/y/no_extension"));
    }

    @Test
    void urlParameterAndBase64DecodingChecks() {
        String url = "https://host/path?a=1&empty=&key=value";
        assertEquals("1", WebUtils.getUrlParameterReg(url, "a"));
        assertEquals("", WebUtils.getUrlParameterReg(url, "empty"));
        assertNull(WebUtils.getUrlParameterReg(url, "missing"));

        String encoded = Base64.getEncoder().encodeToString("https://host/a.txt".getBytes(StandardCharsets.UTF_8));
        assertEquals("https://host/a.txt", WebUtils.decodeUrl(encoded));
        assertNull(WebUtils.decodeUrl(""));
        assertEquals("example.com", WebUtils.getHost("https://Example.COM/a"));
        assertNull(WebUtils.getHost("::not-url::"));
    }

    @Test
    void getSourceUrlFromRequestVariants() {
        String encodedUrl = Base64.getEncoder().encodeToString("https://a.com/1.png".getBytes(StandardCharsets.UTF_8));
        String encodedCurrent = Base64.getEncoder().encodeToString("https://a.com/2.png".getBytes(StandardCharsets.UTF_8));
        String encodedPath = Base64.getEncoder().encodeToString("https://a.com/3.png".getBytes(StandardCharsets.UTF_8));
        String encodedUrls = Base64.getEncoder().encodeToString("https://a.com/4.png|https://a.com/5.png".getBytes(StandardCharsets.UTF_8));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setParameter("url", encodedUrl);
        assertEquals("https://a.com/1.png", WebUtils.getSourceUrl(req));

        req = new MockHttpServletRequest();
        req.setParameter("currentUrl", encodedCurrent);
        assertEquals("https://a.com/2.png", WebUtils.getSourceUrl(req));

        req = new MockHttpServletRequest();
        req.setParameter("urlPath", encodedPath);
        assertEquals("https://a.com/3.png", WebUtils.getSourceUrl(req));

        req = new MockHttpServletRequest();
        req.setParameter("urls", encodedUrls);
        assertEquals("https://a.com/4.png", WebUtils.getSourceUrl(req));

        req = new MockHttpServletRequest();
        assertNull(WebUtils.getSourceUrl(req));
    }

    @Test
    void multipartAndSessionAccessChecks() {
        MockMultipartFile file = new MockMultipartFile(
                "f",
                "C:\\\\temp\\\\safe&lt;name&gt;.txt",
                "text/plain",
                "x".getBytes(StandardCharsets.UTF_8)
        );
        assertEquals("safe&amp;lt;name&amp;gt;.txt", WebUtils.getFileNameFromMultipartFile(file));

        MockHttpServletRequest req = new MockHttpServletRequest();
        WebUtils.setSessionAttr(req, "k1", "v1");
        WebUtils.setSessionAttr(req, "k2", 42L);
        assertEquals("v1", WebUtils.getSessionAttr(req, "k1"));
        assertEquals(42L, WebUtils.getLongSessionAttr(req, "k2"));

        WebUtils.removeSessionAttr(req, "k1");
        assertNull(WebUtils.getSessionAttr(req, "k1"));
        assertEquals(0L, WebUtils.getLongSessionAttr(req, "missing"));
    }
}
