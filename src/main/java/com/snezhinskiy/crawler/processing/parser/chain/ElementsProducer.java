package com.snezhinskiy.crawler.processing.parser.chain;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public interface ElementsProducer extends StringProducer {
    Elements getElements(Document document);
}
