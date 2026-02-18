package cn.keking.utils;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class KkFileUtilsStructuralTests {

    @Test
    void illegalAndNumericChecks() {
        assertTrue(KkFileUtils.isIllegalFileName("../evil.txt"));
        assertFalse(KkFileUtils.isIllegalFileName("safe-file.txt"));

        assertTrue(KkFileUtils.isInteger("123"));
        assertTrue(KkFileUtils.isInteger("-12.5"));
        assertFalse(KkFileUtils.isInteger("abc"));
        assertFalse(KkFileUtils.isInteger(" "));
    }

    @Test
    void protocolAndSuffixChecks() throws Exception {
        assertTrue(KkFileUtils.isHttpUrl(new URL("http://example.com/a.txt")));
        assertTrue(KkFileUtils.isHttpUrl(new URL("file:///tmp/a.txt")));
        assertFalse(KkFileUtils.isHttpUrl(new URL("ftp://example.com/a.txt")));

        assertTrue(KkFileUtils.isFtpUrl(new URL("ftp://example.com/a.txt")));
        assertFalse(KkFileUtils.isFtpUrl(new URL("https://example.com/a.txt")));

        assertEquals("txt", KkFileUtils.suffixFromFileName("A.TXT"));
        assertEquals("&lt;script&gt;", KkFileUtils.htmlEscape("<script>"));
        assertNull(KkFileUtils.htmlEscape(null));
    }

    @Test
    void fileDeletionAndExistenceChecks() throws Exception {
        Path tempFile = Files.createTempFile("kkfile-", ".tmp");
        assertTrue(KkFileUtils.isExist(tempFile.toString()));
        assertTrue(KkFileUtils.deleteFileByName(tempFile.toString()));
        assertFalse(KkFileUtils.isExist(tempFile.toString()));
        assertFalse(KkFileUtils.deleteFileByName(tempFile.toString()));
    }

    @Test
    void deleteDirectoryRecursively() throws Exception {
        Path root = Files.createTempDirectory("kk-dir-");
        Path sub = Files.createDirectory(root.resolve("sub"));
        Files.writeString(sub.resolve("file.txt"), "content");
        assertTrue(KkFileUtils.deleteDirectory(root.toString()));
        assertFalse(Files.exists(root));
        assertFalse(KkFileUtils.deleteDirectory(root.toString()));
    }

    @Test
    void deleteFileByPathShouldBeNoopForMissingFile() throws Exception {
        Path file = Files.createTempFile("kk-path-", ".tmp");
        Files.writeString(file, "x");
        KkFileUtils.deleteFileByPath(file.toString());
        assertFalse(Files.exists(file));

        Path missing = file.getParent().resolve("missing-" + System.nanoTime() + ".tmp");
        KkFileUtils.deleteFileByPath(missing.toString());
        assertFalse(Files.exists(missing));
    }

    @Test
    void allowedUploadChecks() {
        assertTrue(KkFileUtils.isAllowedUpload("normal.abcxyz"));
        assertTrue(KkFileUtils.isAllowedUpload("no_suffix"));
        assertFalse(KkFileUtils.isAllowedUpload(""));
    }
}
