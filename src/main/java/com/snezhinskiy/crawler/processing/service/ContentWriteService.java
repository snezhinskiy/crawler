package com.snezhinskiy.crawler.processing.service;

import com.snezhinskiy.crawler.processing.model.ParsedContent;

public interface ContentWriteService {
    void save(ParsedContent content);
}
