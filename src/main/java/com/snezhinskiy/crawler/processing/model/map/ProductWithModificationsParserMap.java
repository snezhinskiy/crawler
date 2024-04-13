package com.snezhinskiy.crawler.processing.model.map;

import com.snezhinskiy.crawler.processing.parser.chain.DocumentProcessingChain;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductWithModificationsParserMap extends BaseContentParserMap {
    private DocumentProcessingChain name = DocumentProcessingChain.emptyChain();
    private DocumentProcessingChain sku = DocumentProcessingChain.emptyChain();
    private DocumentProcessingChain description = DocumentProcessingChain.emptyChain();
    private DocumentProcessingChain preview = DocumentProcessingChain.emptyChain();
    private DocumentProcessingChain price = DocumentProcessingChain.emptyChain();
    private DocumentProcessingChain stock = DocumentProcessingChain.emptyChain();
    private DocumentProcessingChain modificationCode = DocumentProcessingChain.emptyChain();
    private DocumentProcessingChain modificationName = DocumentProcessingChain.emptyChain();
    private DocumentProcessingChain modificationPrice = DocumentProcessingChain.emptyChain();
    private DocumentProcessingChain modificationPreview = DocumentProcessingChain.emptyChain();
    private DocumentProcessingChain modificationStock = DocumentProcessingChain.emptyChain();
}
