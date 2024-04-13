package com.snezhinskiy.crawler.processing.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Log4j2
@Service
@ConditionalOnProperty(name="run-crawler-services", havingValue = "true")
@RequiredArgsConstructor
public class CrawlerLifecycleService implements SmartLifecycle {
    private volatile boolean isRunning;

    @Qualifier("virtualThreadExecutor")
    private final ExecutorService executorService;
    private final Crawler crawler;
    private final LinksParser linksParser;
    private final ContentParser contentParser;

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void start() {
        log.debug("Start workers");
        executorService.submit(crawler);
        executorService.submit(linksParser);
        executorService.submit(contentParser);
        isRunning = true;
        log.debug("Workers ready now");
    }

    @Override
    public void stop() {
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }
}
