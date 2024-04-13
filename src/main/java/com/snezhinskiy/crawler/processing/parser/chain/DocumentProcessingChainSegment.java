package com.snezhinskiy.crawler.processing.parser.chain;

import com.snezhinskiy.crawler.processing.model.ParseRule;
import org.springframework.util.Assert;

public abstract class DocumentProcessingChainSegment {
    protected DocumentProcessingChainSegment parent;
    protected ParseRule rule;

    public DocumentProcessingChainSegment(DocumentProcessingChainSegment parent, ParseRule rule) {
        this.parent = parent;
        this.rule = rule;
    }
}
