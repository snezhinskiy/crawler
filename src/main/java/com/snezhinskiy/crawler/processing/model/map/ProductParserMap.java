package com.snezhinskiy.crawler.processing.model.map;

import com.snezhinskiy.crawler.processing.parser.chain.DocumentProcessingChain;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductParserMap extends BaseContentParserMap {
    private DocumentProcessingChain name;
    private DocumentProcessingChain sku;
    private DocumentProcessingChain description;
    private DocumentProcessingChain preview;
    private DocumentProcessingChain price;
    private DocumentProcessingChain stock;
}
