package com.snezhinskiy.crawler.processing.parser;


import com.snezhinskiy.crawler.processing.model.ParserJob;
import com.snezhinskiy.crawler.processing.model.ProductData;
import com.snezhinskiy.crawler.processing.model.map.BaseContentParserMap;
import com.snezhinskiy.crawler.processing.model.map.ProductWithModificationsParserMap;
import com.snezhinskiy.crawler.processing.parser.utils.SafeNumbersConverter;
import com.snezhinskiy.crawler.processing.parser.utils.SafeStringConverter;
import com.snezhinskiy.crawler.processing.parser.utils.UrlUtils;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Component("PRODUCT_WITH_MODIFICATIONS")
public class MultiModificationsProductParser implements MappedProductParser {

    public List<ProductData> parse(ParserJob parserJob) {
        BaseContentParserMap parserMap = parserJob.getParserMap();
        Assert.isInstanceOf(
            ProductWithModificationsParserMap.class, parserMap,
            "Wrong parser map: " + parserMap.getClass()
        );

        ProductWithModificationsParserMap ppm = (ProductWithModificationsParserMap) parserMap;
        Document document = parserJob.getDocument();

        if (parserMap.getPageTest() != null) {
            final String test = parserMap.getPageTest().toString(document);

            if (!StringUtils.hasText(test)) {
                log.debug("Not passed test, url:{}", parserJob.getUrl());
                return Collections.emptyList();
            }
        }

        log.debug("Start parse document, url:{}", parserJob.getUrl());

        List<Object> modCodesRawResult = (List<Object>)ppm.getModificationCode().toList(document);

        List<ProductData> result = new ArrayList<>();

        String baseProductPrice = ppm.getPrice().toString(document);

        String baseProductSKU = ppm.getSku().toString(document);
        String baseProductName = ppm.getName().toString(document);
        String baseProductDescription = ppm.getDescription().toString(document);
        String baseProductPreview = ppm.getPreview().toString(document);
        String baseProductStock = ppm.getStock().toString(document);

        Set<String> modificationCodes = modCodesRawResult.stream()
            .map(code -> String.valueOf(code))
            .collect(Collectors.toSet());

        if (!CollectionUtils.isEmpty(modificationCodes)) {
            log.trace("Parse modification names for document: {}", parserJob.getUrl());
            Map<String, Object> modificationNames = Collections.emptyMap();;
            if (ppm.getModificationName() != null) {
                modificationNames = ppm.getModificationName().toMap(document);
            }

            log.trace("Parse modification prices for document: {}", parserJob.getUrl());
            Map<String, Object> modificationPrices = Collections.emptyMap();;
            if (ppm.getModificationPrice() != null) {
                modificationPrices = ppm.getModificationPrice().toMap(document);
            }

            log.trace("Parse modification previews for document: {}", parserJob.getUrl());
            Map<String, Object> modificationPreviews = Collections.emptyMap();
            if (ppm.getModificationPreview() != null) {
                modificationPreviews = ppm.getModificationPreview().toMap(document);
            }

            log.trace("Parse modification quantities for document: {}", parserJob.getUrl());
            Map<String, Object> modificationStocks = Collections.emptyMap();
            if (ppm.getModificationStock() != null) {
                modificationStocks = ppm.getModificationStock().toMap(document);
            }

            for (String code : modificationCodes) {
                try {
                    ProductData item = spawnProductDate(parserJob);

                    item.setModificationCode(code);
                    item.setSku(baseProductSKU);
                    item.setName(baseProductName);
                    item.setDescription(baseProductDescription);

                    if (modificationNames.containsKey(code)) {
                        item.setModificationName((String) modificationNames.get(code));
                    }

                    if (modificationPrices.containsKey(code)) {
                        item.setPrice(
                            SafeNumbersConverter.toDouble(modificationPrices.get(code))
                        );
                    } else if (StringUtils.hasText(baseProductPrice)) {
                        item.setPrice(
                            SafeStringConverter.toDouble(baseProductPrice)
                        );
                    }

                    if (modificationPreviews.containsKey(code)
                        && modificationPreviews.get(code) instanceof String
                        && StringUtils.hasText((String)modificationPreviews.get(code))
                    ) {
                        String previewUrl = (String)modificationPreviews.get(code);
                        item.setPreviewUrl(UrlUtils.toAbsoluteUrl(parserJob.getBase(), previewUrl));
                    } else if (StringUtils.hasText(baseProductPreview)) {
                        item.setPreviewUrl(UrlUtils.toAbsoluteUrl(parserJob.getBase(), baseProductPreview));
                    }

                    if (modificationStocks.containsKey(code)) {
                        item.setStock(
                            SafeNumbersConverter.toInteger(modificationStocks.get(code))
                        );
                    } else {
                        item.setStock(
                            SafeStringConverter.toInteger(baseProductStock)
                        );
                    }

                    result.add(item);
                } catch (RuntimeException e) {
                    log.error("Error occured during product parsing", e);
                }
            }
        } else {
            try {
                ProductData item = spawnProductDate(parserJob);
                item.setSku(baseProductSKU);
                item.setDescription(baseProductDescription);
                item.setName(baseProductName);
                item.setPrice(
                    SafeStringConverter.toDouble(baseProductPrice)
                );
                item.setPreviewUrl(baseProductPreview);
                item.setStock(
                    SafeStringConverter.toInteger(baseProductStock)
                );
                result.add(item);
            } catch (RuntimeException e) {
                log.error("Error occured during product parsing", e);
            }
        }

        return result;
    }

    private ProductData spawnProductDate(ParserJob parserJob) {
        ProductData productData = new ProductData();
        productData.setHash(parserJob.getHash());
        productData.setDomainHash(parserJob.getDomainHash());
        productData.setUrl(parserJob.getUrl());
        return productData;
    }
}
