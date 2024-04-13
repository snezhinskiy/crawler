package com.snezhinskiy.crawler.processing.parser.linksExtractor;

import com.snezhinskiy.crawler.processing.model.ParserJob;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecursiveHierarchicalLinksExtractorTest {
    private RecursiveHierarchicalLinksExtractor service = new RecursiveHierarchicalLinksExtractor();

    @CsvSource({
        "'http://example.com/foo/', 'http://example.com/foo/bar', 'http://example.com/foo/bar', 1",
        "'https://example.com/foo/', 'https://example.com/foo/bar', 'https://example.com/foo/bar', 1",
        "'http://example.com/foo/', 'https://example.com/foo/bar', '', 0",
        "'https://example.com/foo/', 'http://example.com/foo/bar', '', 0",
        "'https://example.com/foo/bar', 'https://example.com/foo/any', '', 0",
        "'https://example.com/foo/bar', 'https://example.com/', '', 0",
        "'https://example.com/foo/bar', 'https://example.com/foo/bar/any', 'https://example.com/foo/bar/any', 1",
        "'https://example.com/foo/bar', '/foo/bar/any', 'https://example.com/foo/bar/any', 1",
        "'https://example.com/foo/bar', '/', '', 0",
        "'https://example.com/foo/bar', '/any', '', 0",
        "'https://example.com/foo/bar', 'bar/any', '', 0",
        "'https://example.com/foo/bar', 'example.com/foo/bar/any', 'https://example.com/foo/bar/any', 1",
        "'https://example.com/foo/bar', 'example.com/bar/any', '', 0",
    })
    @ParameterizedTest
    public void extractTest(String basePath, String hrefValue, String expectedUrl, int expectedTotal) {
        // Preparations
        String htmlContent = "<html><body><a href=\""+hrefValue+"\"></body></html>";
        Document document = Jsoup.parse(htmlContent);

        ParserJob job = ParserJob.builder()
            .base(basePath)
            .document(document)
            .build();

        // Exercise
        List<String> links = service.extract(job);

        // Assertions
        assertEquals(expectedTotal, links.size());

        if (expectedTotal > 0) {
            assertEquals(expectedUrl, links.get(0));
        }
    }
}