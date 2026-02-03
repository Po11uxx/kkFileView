package cn.keking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ServerMain.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ImagePreviewTests {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String PREVIEW_API = "/onlinePreview?url=";
    private static final String BASE_TEST_URL = "http://localhost:8012/";

    private String encodeUrl(String fileUrl) {
        String encoded = java.util.Base64.getEncoder().encodeToString(fileUrl.getBytes());
        return encoded.replace("+", "-").replace("/", "_").replace("=", "");
    }

    @Test
    public void testValidImageFormat_PreviewSuccess() {
        String fileUrl = BASE_TEST_URL + "photo.png";
        String encodedUrl = encodeUrl(fileUrl);
        ResponseEntity<String> response = restTemplate.getForEntity(PREVIEW_API + encodedUrl, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
//        System.out.println("=== PNG response ===");
//        System.out.println(response.getBody());
//        System.out.println("=== response end ===");
        assertTrue(response.getBody().contains("image"), "Valid image loads preview");
    }

    @Test
    public void testInvalidImageFormat_PreviewRejected() {
        String fileUrl = BASE_TEST_URL + "photo.zip";
        String encodedUrl = encodeUrl(fileUrl);
        ResponseEntity<String> response = restTemplate.getForEntity(PREVIEW_API + encodedUrl, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("zTreeDemoBackground left"),
                "Invalid format displays different preview page.");
    }

    @Test
    public void testImageDeliveryPerformance() {
        // Test small image
        String smallFileUrl = BASE_TEST_URL + "small.jpg";
        String smallEncodedUrl = encodeUrl(smallFileUrl);
        long smallStart = System.currentTimeMillis();
        ResponseEntity<String> smallResponse = restTemplate.getForEntity(PREVIEW_API + smallEncodedUrl, String.class);
        long smallDuration = System.currentTimeMillis() - smallStart;

        // Test large image
        String largeFileUrl = BASE_TEST_URL + "large.jpg";
        String largeEncodedUrl = encodeUrl(largeFileUrl);
        long largeStart = System.currentTimeMillis();
        ResponseEntity<String> largeResponse = restTemplate.getForEntity(PREVIEW_API + largeEncodedUrl, String.class);
        long largeDuration = System.currentTimeMillis() - largeStart;

        // Assertions
        assertEquals(HttpStatus.OK, smallResponse.getStatusCode(), "Small image should load successfully");
        assertEquals(HttpStatus.OK, largeResponse.getStatusCode(), "Large image should load successfully");
//        System.out.println("smallDuration: " + smallDuration);
//        System.out.println("largeDuration: " + largeDuration);
        assertTrue(smallDuration < 1000, "Small image delivery should take <1 second");
        assertTrue(largeDuration < 5000, "Large image delivery should take <5 seconds (front-end rotation depends on fast delivery)");
    }

}