package com.snezhinskiy.crawler.processing.parser.linksExtractor;

import com.snezhinskiy.crawler.processing.model.ParserJob;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SelectivelyLinksExtractorTest {

    private SelectivelyLinksExtractor service = new SelectivelyLinksExtractor();

    @CsvSource({
        "'http://example.com/foo/', 'http://example.com/foo/bar', 'http://example.com/foo/bar', 1",
        "'https://example.com/foo/', 'https://example.com/?id=1', 'https://example.com/?id=1', 1",
        "'http://example.com/foo/', 'https://example.com/foo/bar', '', 0",
        "'https://example.com/foo/', 'http://example.com/foo/bar', '', 0",
        "'https://example.com/foo/bar', 'https://example2.com/?id=1', '', 0",
        "'https://example.com/foo/bar', '/foo/bar/any', 'https://example.com/foo/bar/any', 1",
        "'https://example.com/foo/bar', '/', 'https://example.com/', 1",
        "'https://example.com/foo/bar', '/any', 'https://example.com/any', 1",
        "'https://example.com/foo/bar', 'bar/any', '', 0",
        "'https://example.com/foo/bar', 'example.com/foo/bar/any', 'https://example.com/foo/bar/any', 1",
        "'https://example.com/foo/bar', 'example.com/bar/any', 'https://example.com/bar/any', 1",
    })
    @ParameterizedTest
    public void extractTest(String basePath, String hrefValue, String expectedUrl, int expectedTotal) {

        String htmlContent = "<html><body><a class=\"test_selector\" href=\""+hrefValue+"\">" +
            "<a href=\""+basePath+"must/not/be/extracted\">" +
            "</body></html>";
        Document document = Jsoup.parse(htmlContent);

        ParserJob job = ParserJob.builder()
            .base(basePath)
            .document(document)
            .itemsSelector("a.not_exists")
            .paginationSelector("a.test_selector")
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