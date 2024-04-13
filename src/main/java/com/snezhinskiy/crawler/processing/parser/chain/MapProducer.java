package com.snezhinskiy.crawler.processing.parser.chain;

import org.jsoup.nodes.Document;

import java.util.Map;

public interface MapProducer {
    Map<String, Object> getMap(Document document);
}
