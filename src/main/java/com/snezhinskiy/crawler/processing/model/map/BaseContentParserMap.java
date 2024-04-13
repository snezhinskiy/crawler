package com.snezhinskiy.crawler.processing.model.map;

import com.snezhinskiy.crawler.processing.parser.chain.DocumentProcessingChain;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseContentParserMap {
    private DocumentProcessingChain pageTest;
}
