package cn.keking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ServerMain.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OfficePreviewTests {

    @Autowired
    private TestRestTemplate restTemplate;

    // Base URL for preview API (format: /onlinePreview?url={base64EncodedUrl})
    private static final String PREVIEW_API = "/onlinePreview?url=";

    // Helper method to encode file URLs to Base64 (mimics frontend behavior)
    private String encodeUrl(String fileUrl) {
        String encoded = java.util.Base64.getEncoder().encodeToString(fileUrl.getBytes());
        // replace '+/=' in Base64 to URL characters
        return encoded.replace("+", "-").replace("/", "_").replace("=", "");
    }

    // Base URL
    private static final String BASE_TEST_URL = "http://localhost:8012/";

    @Test
    public void testValidOfficeFormat_PreviewSuccess() {
        // Test P1-1: Valid DOCX file
        String fileUrl = BASE_TEST_URL + "Sample.docx";
        String encodedUrl = encodeUrl(fileUrl);
        ResponseEntity<String> response = restTemplate.getForEntity(PREVIEW_API + encodedUrl, String.class);
//        System.out.println("=== DOCX response ===");
//        System.out.println(response.getBody());
//        System.out.println("=== response end ===");
        // Assert status = 200
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // kkFileView returns "img-area"container when previewing office files
        assertTrue(response.getBody().contains("img-area"),
                "DOCX preview page should contain img-area container");
    }

    @Test
    public void testValidWpsFormat_PreviewSuccess() {
        // Test P1-2: Valid WPS file
        String fileUrl = BASE_TEST_URL + "Sample.wps";
        String encodedUrl = encodeUrl(fileUrl);
        ResponseEntity<String> response = restTemplate.getForEntity(PREVIEW_API + encodedUrl, String.class);
//        System.out.println("=== WPS response ===");
//        System.out.println(response.getBody());
//        System.out.println("=== response end ===");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // WPS should have the same container of DOCX
        assertTrue(response.getBody().contains("img-area"),
                "WPS preview page should load successfully");
    }

    @Test
    public void testInvalidOfficeFormat_PreviewRedirectToCorrectHandler() {
        // Test P1-3: Invalid format (TXT file)
        String fileUrl = BASE_TEST_URL + "Sample.txt";
        String encodedUrl = encodeUrl(fileUrl);
        ResponseEntity<String> response = restTemplate.getForEntity(PREVIEW_API + encodedUrl, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // TXT preview contains "panel panel-default" container
        assertTrue(response.getBody().contains("panel panel-default"),
                "TXT file should use plain text preview");
    }

    @Test
    public void testCorruptedOfficeFile_PreviewError() {
        // Test P1-4: Corrupted XLSX file
        String fileUrl = BASE_TEST_URL + "corruptedSample.xlsx";
        String encodedUrl = encodeUrl(fileUrl);
        ResponseEntity<String> response = restTemplate.getForEntity(PREVIEW_API + encodedUrl, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        System.out.println("=== XLSX response ===");
        System.out.println(response.getBody());
        System.out.println("=== response end ===");
//        assertTrue(response.getBody().contains("luckysheet"),
//                "XLSX file should use plain text preview");

        boolean hasChineseError = response.getBody().contains("读取excel文件内容失败!");
        boolean hasEnglishError = response.getBody().contains("File is corrupted or unrecognizable");
        assertTrue(hasChineseError || hasEnglishError,
                "Should display corruption error (Chinese or English)");
    }
}