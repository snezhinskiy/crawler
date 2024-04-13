package com.snezhinskiy.crawler.processing.service;

import com.snezhinskiy.crawler.processing.model.CrawlerJob;

public interface CrawlerJobService extends OnDemandNotifier {
    CrawlerJob getJob();
    void addJob(CrawlerJob job);
}
