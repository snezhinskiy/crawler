package com.snezhinskiy.crawler.processing.parser.mapBulder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.snezhinskiy.crawler.domain.embedded.ContentType;
import com.snezhinskiy.crawler.processing.model.map.BaseContentParserMap;

public interface ContentParserMapBuilder<T> {

    BaseContentParserMap build(String mapRawJson) throws JsonProcessingException;

    ContentType getType();
}
