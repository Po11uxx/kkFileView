package cn.keking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@SpringBootTest(classes = ServerMain.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OfficeFSMTest {
    private static final Logger logger = LoggerFactory.getLogger(OfficeFSMTest.class);
    @Autowired
    private TestRestTemplate restTemplate;

    // Core preview API (triggers filePreviewHandle in OfficeFilePreviewImpl)
    private static final String PREVIEW_API = "/onlinePreview?url=";
    private static final String BASE_TEST_URL = "http://localhost:8012/";

    private static final String FILE_DIR = "/Users/mac/Desktop/studyeeeee/Master/Winter1/SWE261P/kkFileView/server/src/main/resources/static/test-files/";

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    // Encode URL per kkFileView production logic
    private String encodeUrl(String fileUrl) {
        String encoded = java.util.Base64.getEncoder().encodeToString(fileUrl.getBytes());
        // replace '+/=' in Base64 to URL characters
        return encoded.replace("+", "-").replace("/", "_").replace("=", "");
    }

    // Get real file path
    private String getRealFilePath(String fileName) {
        return FILE_DIR + fileName;
    }

    // Check if file exists
    private boolean isFileExist(String fileName) {
        return new File(getRealFilePath(fileName)).exists();
    }

    // Clean up test files
    private void cleanUpTestFiles(String... fileNames) {
        for (String fileName : fileNames) {
            File file = new File(getRealFilePath(fileName));
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) logger.warn("Failed to delete test file: {}", fileName);
            }
        }
    }

    // Verify DownloadUtils error handling
    private boolean isDownloadFailedResponse(String responseBody) {
        return responseBody.contains("下载失败") || responseBody.contains("文件不存在") || responseBody.contains("文件名不合法");
    }


    // -------------------------------------------------------------------------
    // Core FSM Tests
    // -------------------------------------------------------------------------

    /**
     * Test: Idle → File Downloaded → File Validated → Conversion In Progress → Preview Success
     * Matches:
     * - DownloadUtils.downLoad: download to FILE_DIR → return realPath
     * - No encryption → skip password check → convert to PDF → PDF preview
     */
    @Test
    public void testValidDOCXWorkflow_PDFPreview() {
        String testFileName = "Sample1.docx";
        String testPDFName = "Sample1docx.pdf";

        String fileUrl = BASE_TEST_URL + testFileName;
        String encodedUrl = encodeUrl(fileUrl);
        System.out.println("fileUrl: " + fileUrl);
        System.out.println("encodedUrl: " + encodedUrl);
        ResponseEntity<String> response = restTemplate.getForEntity(PREVIEW_API + encodedUrl, String.class);
        System.out.println("PREVIEW_API + encodedUrl:  " + PREVIEW_API + encodedUrl);
        System.out.println("=== DOCX response ===");
        System.out.println(response.getBody());
        System.out.println("=== response end ===");

        // Assertions (aligned with DownloadUtils)
        assertTrue(isFileExist(testPDFName), "File should be downloaded to dir: " + getRealFilePath(testFileName));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().contains("needFilePassword"), "No password prompt needed");
        assertTrue(response.getBody().contains("data-src"), "PDF preview URL should be present");
        assertFalse(isDownloadFailedResponse(response.getBody()), "No download errors");
    }

    /**
     * Test: Cache Hit → Skip Download
     */
    @Test
    public void testCacheHit_SkipDownload() {
        String testName = "testCacheHit_SkipDownload";
        String testFileName = "Sample.docx";

        // Precondition: Create test file (simulate cache hit)
        File testFile = new File(getRealFilePath(testFileName));
        try {
            testFile.createNewFile();
            long originalTimestamp = testFile.lastModified();
            assertTrue(isFileExist(testFileName), "Precondition: File exists (cache hit)");

            // Trigger Request (forceUpdatedCache=false by default)
            String fileUrl = BASE_TEST_URL + testFileName;
            String encodedUrl = encodeUrl(fileUrl);
            ResponseEntity<String> response = restTemplate.getForEntity(PREVIEW_API + encodedUrl, String.class);

            // Log Response
            System.out.println("=== DOCX skip response ===");
            System.out.println(response.getBody());
            System.out.println("=== response end ===");

            // Assertions
            assertEquals(HttpStatus.OK, response.getStatusCode());
            // Verify file was NOT re-downloaded (timestamp unchanged)
            long newTimestamp = new File(getRealFilePath(testFileName)).lastModified();
            assertEquals(originalTimestamp, newTimestamp, "File timestamp should not change (cache hit)");

        } catch (Exception e) {
            fail(testName + " failed: " + e.getMessage());
        } finally {
            testFile.delete();
        }
    }

    /**
     * Test: Compressed File → Skip Download
     */
    @Test
    public void testCompressedFile_SkipDownload() {
        String zipFileName = "Sample.zip";

        // Trigger Request for ZIP File
        String fileUrl = BASE_TEST_URL + zipFileName;
        String encodedUrl = encodeUrl(fileUrl);
        ResponseEntity<String> response = restTemplate.getForEntity(PREVIEW_API + encodedUrl, String.class);

        // Log Response
        System.out.println("=== zip response ===");
        System.out.println(response.getBody());
        System.out.println("=== response end ===");

        // Assertions (matches DownloadUtils.isCompressFile logic)
        assertTrue(response.getBody().contains("zTreeDemoBackground"), "ZIP file should be rejected");
    }

    /**
     * Test: Password-Protected File → Download + Password Prompt
     */
    @Test
    public void testPasswordProtectedFile_PasswordRequired() {
        String protectedFileName = "Protected.docx";

        // Trigger Request
        String fileUrl = BASE_TEST_URL + protectedFileName;
        String encodedUrl = encodeUrl(fileUrl);
        ResponseEntity<String> response = restTemplate.getForEntity(PREVIEW_API + encodedUrl, String.class);

        // Log Response
        System.out.println("=== pwd response ===");
        System.out.println(response.getBody());
        System.out.println("=== response end ===");

        // Assertions
        assertTrue(isFileExist(protectedFileName), "Password-protected file should be downloaded");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("needFilePassword"), "Password prompt should be present");
    }

    /**
     * Test: Force Cache Update → Re-Download
     */
    @Test
    public void testForceCacheUpdate_ReDownload() {
        String testName = "testForceCacheUpdate_ReDownload";
        String testFileName = "Sample.wps";
        String testPDFName = "Samplewps.pdf";

        // Precondition: Create test file
        File testFile = new File(getRealFilePath(testFileName));
        try {
            testFile.createNewFile();
            long originalTimestamp = testFile.lastModified();
            System.out.println("originalTimestamp: " + originalTimestamp);
            assertTrue(isFileExist(testFileName));

            // Trigger Request with forceUpdatedCache=true
            String fileUrl = BASE_TEST_URL + testFileName;
            String encodedUrl = encodeUrl(fileUrl);
            String previewUrl = PREVIEW_API + encodedUrl + "&forceUpdatedCache=true";
            ResponseEntity<String> response = restTemplate.getForEntity(previewUrl, String.class);
            System.out.println("previewUrl:" + previewUrl);

            // Log Response
            System.out.println("=== redownload response ===");
            System.out.println(response.getBody());
            System.out.println("=== response end ===");
            // Assertions
            long newTimestamp = new File(getRealFilePath(testPDFName)).lastModified();
            System.out.println("newTimestamp: " + newTimestamp);
            assertNotEquals(originalTimestamp, newTimestamp, "File should be re-downloaded (timestamp changed)");

        } catch (Exception e) {
            fail(testName + " failed: " + e.getMessage());
        }
    }

    /**
     * Test: XLSX Web Preview → No PDF Conversion
     */
    @Test
    public void testXLSX_WebPreview_NoPDFConversion() {
        String xlsxFileName = "XLSXDLD.xlsx";

        // Trigger XLSX Web Preview
        String fileUrl = BASE_TEST_URL + xlsxFileName;
        String encodedUrl = encodeUrl(fileUrl);
        String previewUrl = PREVIEW_API + encodedUrl + "&officePreviewType=web";
        ResponseEntity<String> response = restTemplate.getForEntity(previewUrl, String.class);

        // Log Response
        System.out.println("=== xlsx response ===");
        System.out.println(response.getBody());
        System.out.println("=== response end ===");

        // Assertions
        assertTrue(isFileExist(xlsxFileName), "XLSX file should be downloaded");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Test: Non-Existent File → Download Failed
     */
    @Test
    public void testNonExistentFile_DownloadFailed() {
        String nonExistentFile = "nonexistent.docx";

        // Trigger Request for Non-Existent File
        String fileUrl = BASE_TEST_URL + nonExistentFile;
        String encodedUrl = encodeUrl(fileUrl);
        ResponseEntity<String> response = restTemplate.getForEntity(PREVIEW_API + encodedUrl, String.class);

        // Log Response
        System.out.println("=== nonexist response ===");
        System.out.println(response.getBody());
        System.out.println("=== response end ===");

        // Assertions
        assertTrue(isDownloadFailedResponse(response.getBody()), "Download should fail for non-existent file");
        assertFalse(isFileExist(nonExistentFile), "Non-existent file should NOT be downloaded");
    }

}

