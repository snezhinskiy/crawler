package com.snezhinskiy.crawler.processing.service;

import com.snezhinskiy.crawler.processing.model.ParserJob;

public interface ContentParserJobService {
    ParserJob takeJob();
    void addJob(ParserJob job);
}
