package com.snezhinskiy.crawler.processing.parser.mapBulder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snezhinskiy.crawler.domain.embedded.ContentType;
import com.snezhinskiy.crawler.processing.model.map.*;
import com.snezhinskiy.crawler.processing.parser.chain.DocumentProcessingChain;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import static com.snezhinskiy.crawler.domain.embedded.ContentType.PRODUCT;

@Component
public class ProductParserMapBuilder implements ContentParserMapBuilder {

    private final ObjectMapper objectMapper;

    public ProductParserMapBuilder(@Qualifier("objectMapperWithParseRuleModule") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ContentType getType() {
        return PRODUCT;
    }

    @Override
    public BaseContentParserMap build(String mapRawJson) throws JsonProcessingException {
        ProductParserMapDTO mapDTO = objectMapper.readValue(mapRawJson, ProductParserMapDTO.class);

        if (mapDTO.getPageTestRule() != null) {
            if (mapDTO.getPageTestRule().getFirst().getFirstArgument() instanceof String firstArgument) {
                Assert.isTrue(
                    StringUtils.hasText(firstArgument),
                    "TestRule must not has empty argument"
                );
            } else
                throw new IllegalArgumentException("First argument is expected to be a String");

        }

        ProductParserMap map = new ProductParserMap();

        if (mapDTO.getPageTestRule() != null) {
            map.setPageTest(new DocumentProcessingChain(mapDTO.getPageTestRule()));
        }

        map.setName(new DocumentProcessingChain(mapDTO.getNameRules()));
        map.setSku(new DocumentProcessingChain(mapDTO.getSkuRules()));
        map.setDescription(new DocumentProcessingChain(mapDTO.getDescriptionRules()));
        map.setPreview(new DocumentProcessingChain(mapDTO.getPreviewRules()));
        map.setPrice(new DocumentProcessingChain(mapDTO.getPriceRules()));
        map.setStock(new DocumentProcessingChain(mapDTO.getStockRules()));

        return map;
    }
}
