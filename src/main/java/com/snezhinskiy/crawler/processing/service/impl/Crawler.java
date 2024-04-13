package com.snezhinskiy.crawler.processing.service.impl;

import com.snezhinskiy.crawler.configuration.properties.CrawlerProperties;
import com.snezhinskiy.crawler.processing.model.CrawlerJob;
import com.snezhinskiy.crawler.processing.model.ParserJob;
import com.snezhinskiy.crawler.processing.parser.helper.ParserJobBuilder;
import com.snezhinskiy.crawler.processing.service.ContentParserJobService;
import com.snezhinskiy.crawler.processing.service.CrawlerJobService;
import com.snezhinskiy.crawler.processing.service.LinksParserJobService;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
@Component
public class Crawler implements Runnable {
    private final CrawlerProperties properties;
    private final ExecutorService executorService;
    private final CrawlerJobService crawlerJobService;
    private final LinksParserJobService linksParserJobService;

    private final ContentParserJobService contentParserJobService;
    private Semaphore semaphore;
    private final Lock lock = new ReentrantLock();
    private final Condition queueCondition = lock.newCondition();


    public Crawler(
        CrawlerProperties crawlerProperties,
        @Qualifier("virtualThreadExecutor")
        ExecutorService executorService,
        CrawlerJobService crawlerJobService,
        LinksParserJobService linksParserJobService,
        ContentParserJobService contentParserJobService
    ) {
        this.semaphore = new Semaphore(crawlerProperties.getMaxWorkers());
        this.properties = crawlerProperties;
        this.executorService = executorService;
        this.crawlerJobService = crawlerJobService;
        this.linksParserJobService = linksParserJobService;
        this.contentParserJobService = contentParserJobService;
    }

    @Override
    public void run() {
        while(true) {
            try {
                CrawlerJob job = crawlerJobService.getJob();

                if (job != null) {
                    semaphore.acquire();

                    try {
                        executorService.submit(() -> load(job));
                    } finally {
                        semaphore.release();
                    }
                } else {
                    log.trace("No jobs for now. Let's sleep some time");

                    lock.lock();

                    try {
                        queueCondition.await(
                            crawlerJobService.getIdleTimeOrNotify(() -> signal()),
                            TimeUnit.MILLISECONDS
                        );
                    } finally {
                        log.trace("Woke up");
                        lock.unlock();
                    }
                }
            } catch (RuntimeException e) {
                log.error("Crawler error", e);
                // do nothing
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void signal() {
        lock.lock();
        try {
            queueCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    private void load(CrawlerJob job) {
        final String url = job.getUrl();
        final int hashCode = url.hashCode();

        log.debug("Job started - hash:{}, {}", hashCode, job);

        long startTime = System.currentTimeMillis();

        try {
            Document jsoupDocument = Jsoup.connect(url)
                .userAgent(properties.getUserAgent())
                .timeout(properties.getTimeout())
                .referrer(properties.getReferer())
                .get();

            float downloadTime = (System.currentTimeMillis()-startTime)/1000;

            log.debug("Loading of hash:{} finished, in {}sec", hashCode, downloadTime);

            ParserJob parserJob = ParserJobBuilder.fromCrawlerJob(job, jsoupDocument);

            contentParserJobService.addJob(parserJob);
            log.debug("Document -> contentParserJobQueue, hash:{}", hashCode);

            if (job.getType().isRecursive() && job.isExpress() == false) {
                linksParserJobService.addJob(parserJob);
                log.debug("Document -> urlsParseJobQueue, hash:{}", hashCode);
            }
        } catch (IOException e) {
            log.debug("Unable to load document, url:{}", job.getUrl());
        } catch (Exception e) {
            log.debug("Error occured during document loading: {}", e);
        }
    }
}
