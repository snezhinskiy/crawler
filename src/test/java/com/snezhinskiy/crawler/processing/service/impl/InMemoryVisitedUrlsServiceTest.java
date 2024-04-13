package com.snezhinskiy.crawler.processing.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryVisitedUrlsServiceTest {
    private InMemoryVisitedUrlsService service = new InMemoryVisitedUrlsService();

    @CsvSource({
        "http://example.com/foo, http://example.com/foo, true",
        "http://example.com/foo, https://example.com/foo, true",
        "https://example.com/foo, http://example.com/foo, true",
        "http://example.com/foo, http://example.com/foo/, true",
        "http://example.com/foo, http://example.com/bar, false",
        "http://example.com/, http://example.com/, true",
        "http://example.com, http://example.com/, true",
        "http://example.com?a=1&b=1, http://example.com/?b=1&a=1, true",
        "http://example.com?a=1, http://example.com/a=2, false"
    })
    @ParameterizedTest
    public void test_IsVisited(String urlA, String urlB, boolean expectedResult) {
        service.addIfNotVisited(urlA);

        assertEquals(
            expectedResult,
            service.isVisited(urlB),
            "Fails on url: " + urlB
        );
    }

    @CsvSource({
        "http://example.com/foo/bar.html, false",
        "http://example.com/foo/bar.htm, false",
        "http://example.com/foo/bar.php, false",
        "http://example.com/foo/bar.asp, false",
        "http://example.com/foo/bar.aspx, false",
        "http://example.com/foo/bar.shtml, false",
        "http://example.com/foo/media.jpg, true",
        "http://example.com/foo/media.js, true",
        "http://example.com/foo/media.css, true"
    })
    @ParameterizedTest
    public void mediaFiles_isVisited_test(String url, boolean expectedResult) {
        assertEquals(
            expectedResult,
            service.isVisited(url),
            "Fails on url: "+url
        );
    }

    @Test
    public void resetAllForHost_test() {
        String url1 = "http://example1.com/foo/bar.html";
        String url2 = "http://example2.com/foo/bar.html";

        service.addIfNotVisited(url1);
        service.addIfNotVisited(url2);

        assertTrue(service.isVisited(url1));
        assertTrue(service.isVisited(url2));

        service.resetAllForHost(url2);

        assertTrue(service.isVisited(url1));
        assertFalse(service.isVisited(url2));
    }
}