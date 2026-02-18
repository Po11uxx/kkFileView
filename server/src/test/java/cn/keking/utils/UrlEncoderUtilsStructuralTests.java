package cn.keking.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlEncoderUtilsStructuralTests {

    @Test
    void hasUrlEncodedChecks() {
        assertTrue(UrlEncoderUtils.hasUrlEncoded("abcDEF0123-_.*+"));
        assertTrue(UrlEncoderUtils.hasUrlEncoded("hello%20world"));
        assertTrue(UrlEncoderUtils.hasUrlEncoded("%2B"));

        assertFalse(UrlEncoderUtils.hasUrlEncoded("hello world"));
        assertFalse(UrlEncoderUtils.hasUrlEncoded("%2f"));
        assertFalse(UrlEncoderUtils.hasUrlEncoded("%"));
        assertFalse(UrlEncoderUtils.hasUrlEncoded("中文"));
    }
}
