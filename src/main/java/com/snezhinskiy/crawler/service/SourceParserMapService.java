package com.snezhinskiy.crawler.service;


import com.snezhinskiy.crawler.domain.SourceParserMap;

public interface SourceParserMapService {
    SourceParserMap save(SourceParserMap entity);

    SourceParserMap getById(Long id);
}
