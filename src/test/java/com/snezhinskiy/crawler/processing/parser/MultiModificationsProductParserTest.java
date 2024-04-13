package com.snezhinskiy.crawler.processing.parser;

import com.snezhinskiy.crawler.processing.model.ParseRule;
import com.snezhinskiy.crawler.processing.model.ParserJob;
import com.snezhinskiy.crawler.processing.model.ProductData;
import com.snezhinskiy.crawler.processing.model.map.BaseContentParserMap;
import com.snezhinskiy.crawler.processing.model.map.ProductWithModificationsParserMap;
import com.snezhinskiy.crawler.processing.parser.chain.DocumentProcessingChain;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.snezhinskiy.crawler.processing.model.FilterType.CONTAINS;
import static com.snezhinskiy.crawler.processing.model.ParseRuleType.*;
import static org.junit.jupiter.api.Assertions.*;

class MultiModificationsProductParserTest {
    private MultiModificationsProductParser parser = new MultiModificationsProductParser();

    @Test
    void parse_productDescription_description() {
        // Preparations
        ProductWithModificationsParserMap parserMap = createProductWithModificationsParserMap();

        parserMap.setDescription(new DocumentProcessingChain(Arrays.asList(
            new ParseRule(CSS_SELECTOR, ".expandable-content__container .rte", "TEXT")
        )));

        ParserJob job = createJob("data/product_with_modifications.html", parserMap);

        // Exercise
        List<ProductData> productData = parser.parse(job);

        // Assertions
        assertEquals(1, productData.size());

        ProductData data = productData.get(0);

        assertTrue(StringUtils.hasText(data.getDescription()));
    }

    @Test
    void parse_productWithModifications_name() {
        // Preparations
        ProductWithModificationsParserMap parserMap = createProductWithModificationsParserMap();

        parserMap.setName(new DocumentProcessingChain(Arrays.asList(
            new ParseRule(CSS_SELECTOR, "h1.product-meta__title")
        )));

        ParserJob job = createJob("data/product_with_modifications.html", parserMap);

        // Exercise
        List<ProductData> productData = parser.parse(job);

        // Assertions
        assertEquals(1, productData.size());

        ProductData data = productData.get(0);

        assertEquals("Raspberry Pi 5", data.getName());
    }

    @Test
    void parse_productWithModifications_modification() {
        // Preparations
        ProductWithModificationsParserMap parserMap = createProductWithModificationsParserMap();

        parserMap.setModificationCode(new DocumentProcessingChain(List.of(
            new ParseRule(TAG_SELECTOR, "script", "TEXT"),
            new ParseRule(FILTER, "CONTAINS", "@context"),
            new ParseRule(FILTER, "CONTAINS", "\"@type\": \"Product\""),
            new ParseRule(MATCHER, "(?<=offers\\\":)([^<]+)"),
            new ParseRule(SPLITTER, "\\{"),
            new ParseRule(MATCHER, "(?<=sku\\\":\s*\")([^\\\"]+)"),
            new ParseRule(TRIMMER, "\"")
        )));

        parserMap.setModificationName(new DocumentProcessingChain(List.of(
            new ParseRule(TAG_SELECTOR, "script", "TEXT"),
            new ParseRule(FILTER, "CONTAINS", "var meta"),
            new ParseRule(FILTER, "CONTAINS", "variants"),
            new ParseRule(ELEMENTS_COMBINER,
                Arrays.asList(
                    new ParseRule(SPLITTER, "\\},\\{"),
                    new ParseRule(MATCHER, "(?<=sku\\\":\s*\")([^\\\"]+)"),
                    new ParseRule(TRIMMER, "\"")
                ),
                Arrays.asList(
                    new ParseRule(SPLITTER, "\\},\\{"),
                    new ParseRule(MATCHER, "(?<=name\\\":\s*\")([^\\\"]+)"),
                    new ParseRule(TRIMMER, "\"")
                )
            )
        )));

        parserMap.setModificationPrice(new DocumentProcessingChain(List.of(
            new ParseRule(TAG_SELECTOR, "script", "TEXT"),
            new ParseRule(FILTER, "CONTAINS", "@context"),
            new ParseRule(FILTER, "CONTAINS", " \"@type\": \"Product\""),
            new ParseRule(ELEMENTS_COMBINER,
                Arrays.asList(
                    new ParseRule(MATCHER, "(?<=offers\\\":)([^<]+)"),
                    new ParseRule(SPLITTER, "\\{"),
                    new ParseRule(MATCHER, "(?<=sku\\\":\s*\")([^\\\"]+)"),
                    new ParseRule(TRIMMER, "\"")
                ),
                Arrays.asList(
                    new ParseRule(MATCHER, "(?<=offers\\\":)([^<]+)"),
                    new ParseRule(SPLITTER, "\\{"),
                    new ParseRule(MATCHER, "(?<=price\\\":\s*\")([^\\\"]+)"),
                    new ParseRule(TRIMMER, "\"")
                )
            )
        )));

        ParserJob job = createJob("data/product_with_modifications.html", parserMap);

        // Exercise
        List<ProductData> productData = parser.parse(job);

        // Assertions
        assertEquals(2, productData.size());

        assertEquals("SC1112", productData.get(0).getModificationCode());
        assertEquals("Raspberry Pi 5 - 8GB", productData.get(0).getModificationName());
        assertEquals(65.00d, productData.get(0).getPrice());

        assertEquals("SC1111", productData.get(1).getModificationCode());
        assertEquals("Raspberry Pi 5 - 4GB", productData.get(1).getModificationName());
        assertEquals(48.75d, productData.get(1).getPrice());
    }

    private ProductWithModificationsParserMap createProductWithModificationsParserMap() {
        ProductWithModificationsParserMap parserMap = new ProductWithModificationsParserMap();

        parserMap.setPageTest(new DocumentProcessingChain(List.of(
            new ParseRule(CSS_SELECTOR, "h1.product-meta__title")
        )));

        return parserMap;
    }

    private ParserJob createJob(String documentPath, BaseContentParserMap parserMap) {
        return ParserJob.builder()
            .document(
                openDocument(documentPath)
            )
            .parserMap(parserMap)
            .hash(1)
            .domainHash(2)
            .build();
    }

    private Document openDocument(String path) {
        ClassLoader classLoader = getClass().getClassLoader();
        Document document = null;

        try {
            Path filePath = Paths.get(classLoader.getResource(path).toURI());
            String htmlContent = Files.readString(filePath);
            document = Jsoup.parse(htmlContent);
        } catch (IOException | URISyntaxException e) {}

        return document;
    }
}