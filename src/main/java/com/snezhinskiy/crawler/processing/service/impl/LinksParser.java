package com.snezhinskiy.crawler.processing.service.impl;

import com.snezhinskiy.crawler.configuration.properties.LinksParserProperties;
import com.snezhinskiy.crawler.domain.embedded.UploadMethod;
import com.snezhinskiy.crawler.processing.model.CrawlerJob;
import com.snezhinskiy.crawler.processing.model.ParserJob;
import com.snezhinskiy.crawler.processing.parser.helper.CrawlerJobBuilder;
import com.snezhinskiy.crawler.processing.parser.linksExtractor.LinksExtractor;
import com.snezhinskiy.crawler.processing.service.CrawlerJobService;
import com.snezhinskiy.crawler.processing.service.LinksParserJobService;
import com.snezhinskiy.crawler.processing.service.VisitedUrlsService;
import com.snezhinskiy.crawler.processing.parser.utils.UrlUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Log4j2
@Component
public class LinksParser implements Runnable {
    private final ExecutorService executorService;
    private final LinksParserJobService linksParserJobService;
    private final CrawlerJobService crawlerJobService;
    private final VisitedUrlsService visitedUrlsService;
    private final Map<UploadMethod, LinksExtractor> linksExtractorMap;
    private Semaphore semaphore;

    public LinksParser(
        LinksParserProperties linksParserProperties,
        @Qualifier("virtualThreadExecutor")
        ExecutorService executorService,
        LinksParserJobService linksParserJobService,
        CrawlerJobService crawlerJobService,
        VisitedUrlsService visitedUrlsService,
        List<LinksExtractor> linksExtractors
    ) {
        semaphore = new Semaphore(linksParserProperties.getMaxWorkers());
        this.executorService = executorService;
        this.linksParserJobService = linksParserJobService;
        this.crawlerJobService = crawlerJobService;
        this.visitedUrlsService = visitedUrlsService;
        this.linksExtractorMap = linksExtractors.stream()
            .collect(Collectors.toMap(
                LinksExtractor::getType, extractor -> extractor
            ));
    }

    @Override
    public void run() {
        while(true) {
            try {
                ParserJob job = linksParserJobService.takeJob();
                semaphore.acquire();

                try {
                    executorService.submit(() -> parse(job));
                } finally {
                    semaphore.release();
                }
            } catch (RuntimeException e) {
                log.error("Links parse runner error", e);
                // do nothing
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void parse(ParserJob parserJob) {
        try {
            log.info("Parse links from page {}", parserJob);

            final UploadMethod type = parserJob.getType();

            if (!linksExtractorMap.containsKey(type)) {
                log.warn("Knows nothing about links extractor for jobType: {}", type);
                return;
            }

            final LinksExtractor extractor = linksExtractorMap.get(type);

            List<String> urls = extractor.extract(parserJob).stream()
                .filter(url -> UrlUtils.isMediaFileUrl(url) == false
                    && visitedUrlsService.addIfNotVisited(url) == true
                )
                .collect(Collectors.toList());

            log.debug("New URLs found: {} in parserJob: {}", urls.size(), parserJob.getUrl().hashCode());

            for (String url: urls) {
                CrawlerJob crawlerJob = CrawlerJobBuilder.fromParserJob(parserJob, url);
                crawlerJobService.addJob(crawlerJob);
            }
        } catch (RuntimeException e) {
            log.debug("Parsing error: {}", e);
        }
    }
}
