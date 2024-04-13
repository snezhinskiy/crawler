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

import static com.snezhinskiy.crawler.domain.embedded.ContentType.PRODUCT_WITH_MODIFICATIONS;

@Component
public class ProductWithModificationParserMapBuilder implements ContentParserMapBuilder {
    private final ObjectMapper objectMapper;

    public ProductWithModificationParserMapBuilder(
        @Qualifier("objectMapperWithParseRuleModule") ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ContentType getType() {
        return PRODUCT_WITH_MODIFICATIONS;
    }

    @Override
    public BaseContentParserMap build(String mapRawJson) throws JsonProcessingException {
        ProductWithModificationsParserMapDTO mapDTO =
            objectMapper.readValue(mapRawJson, ProductWithModificationsParserMapDTO.class);

        if (mapDTO.getPageTestRule() != null) {
            if (mapDTO.getPageTestRule().getFirst().getFirstArgument() instanceof String firstArgument) {
                Assert.isTrue(
                    StringUtils.hasText(firstArgument),
                    "TestRule must not has empty argument"
                );
            } else
                throw new IllegalArgumentException("First argument is expected to be a String");
        }

        ProductWithModificationsParserMap map = new ProductWithModificationsParserMap();

        Assert.notEmpty(
            mapDTO.getModificationCodeRules(),
            "No one modification code rule has been found"
        );

        if (mapDTO.getPageTestRule() != null) {
            map.setPageTest(new DocumentProcessingChain(mapDTO.getPageTestRule()));
        }

        map.setName(new DocumentProcessingChain(mapDTO.getNameRules()));
        map.setSku(new DocumentProcessingChain(mapDTO.getSkuRules()));
        map.setDescription(new DocumentProcessingChain(mapDTO.getDescriptionRules()));
        map.setPreview(new DocumentProcessingChain(mapDTO.getPreviewRules()));
        map.setPrice(new DocumentProcessingChain(mapDTO.getPriceRules()));
        map.setStock(new DocumentProcessingChain(mapDTO.getStockRules()));

        map.setModificationCode(new DocumentProcessingChain(mapDTO.getModificationCodeRules()));
        map.setModificationName(new DocumentProcessingChain(mapDTO.getModificationNameRules()));
        map.setModificationPrice(new DocumentProcessingChain(mapDTO.getModificationPriceRules()));
        map.setModificationPreview(new DocumentProcessingChain(mapDTO.getModificationPreviewRules()));
        map.setModificationStock(new DocumentProcessingChain(mapDTO.getModificationStockRules()));

        return map;
    }
}
