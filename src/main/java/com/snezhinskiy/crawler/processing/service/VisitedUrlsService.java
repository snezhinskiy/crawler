package com.snezhinskiy.crawler.processing.service;

import java.util.List;

public interface VisitedUrlsService {
    void resetAllForHost(String url);
    boolean isVisited(String url);

    boolean addIfNotVisited(String url);
}
