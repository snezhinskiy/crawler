package com.snezhinskiy.crawler.processing.parser.chain;

import org.jsoup.nodes.Document;

import java.util.List;

public interface StringProducer {
    String getString(Document document);
    List<String> getStringList(Document document);
}
