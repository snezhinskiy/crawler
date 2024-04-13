package com.snezhinskiy.crawler.processing.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.snezhinskiy.crawler.domain.Job;
import com.snezhinskiy.crawler.domain.JobConfig;
import com.snezhinskiy.crawler.domain.SourceParserMap;
import com.snezhinskiy.crawler.domain.embedded.ContentType;
import com.snezhinskiy.crawler.domain.embedded.JobStatus;
import com.snezhinskiy.crawler.processing.model.CrawlerJob;
import com.snezhinskiy.crawler.processing.model.ProductData;
import com.snezhinskiy.crawler.processing.model.map.BaseContentParserMap;
import com.snezhinskiy.crawler.processing.parser.helper.CrawlerJobBuilder;
import com.snezhinskiy.crawler.processing.parser.mapBulder.ContentParserMapBuilder;
import com.snezhinskiy.crawler.processing.service.CrawlerJobService;
import com.snezhinskiy.crawler.service.JobService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
@Service
public class JobRunningService {
    private static final int EXPRESS_JOB_COMPLETION_TIMEOUT = 5000;
    private final CrawlerJobService crawlerJobService;
    private final JobService jobService;
    private final Map<ContentType, ContentParserMapBuilder> parserBuildersMap;

    public JobRunningService(
        CrawlerJobService crawlerJobService,
        JobService jobService,
        List<ContentParserMapBuilder> builders
    ) {
        this.crawlerJobService = crawlerJobService;
        this.jobService = jobService;
        this.parserBuildersMap = builders.stream()
            .collect(Collectors.toMap(
                builder -> builder.getType(), builder -> builder
            ));
    }

    public CompletableFuture<List<ProductData>> runAsync(SourceParserMap parserMap, String url) {
        log.info("Got new express job, mapId:{}, url:{}", parserMap.getId(), url);

        CompletableFuture<List<ProductData>> runner = new CompletableFuture<List<ProductData>>()
            .completeOnTimeout(Collections.emptyList(), EXPRESS_JOB_COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS)
            .exceptionally((ex) -> Collections.emptyList());

        try {
            Assert.notNull(parserMap, "SourceParserMap must be not null");
            ContentType contentType = parserMap.getContentType();

            Assert.isTrue(
                parserBuildersMap.containsKey(contentType),
                "Knows nothing about builder for such contentType"
            );

            ContentParserMapBuilder parserMapBuilder = parserBuildersMap.get(contentType);
            BaseContentParserMap contentParserMap = parserMapBuilder.build(parserMap.getBody());

            CrawlerJob crawlerJob =
                CrawlerJobBuilder.fromRequest(parserMap, contentParserMap, url, runner);

            crawlerJobService.addJob(crawlerJob);
            log.info("Express job added to queue, url:{}", url);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            log.error("Job (url:{})failed with error", url, e);
            runner.completeExceptionally(e);
        }

        return runner;
    }

    public void run(Job job) {
        log.info("Start running new job:{}", job.getId());

        try {
            JobConfig config = job.getConfig();
            Assert.notNull(config, "Config must be not null");

            SourceParserMap map = config.getParserMap();
            Assert.notNull(map, "SourceParserMap must be not null");

            ContentType contentType = map.getContentType();

            Assert.isTrue(
                parserBuildersMap.containsKey(contentType),
                "Knows nothing about builder for such contentType"
            );

            ContentParserMapBuilder parserMapBuilder = parserBuildersMap.get(contentType);
            BaseContentParserMap contentParserMap = parserMapBuilder.build(map.getBody());

            CrawlerJob crawlerJob =
                CrawlerJobBuilder.fromScratch(config, job, contentParserMap);

            crawlerJobService.addJob(crawlerJob);
            log.info("Job id:{} added to queue", job.getId());

            job.setStatus(JobStatus.STARTED);
            jobService.save(job);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            job.setStatus(JobStatus.FAILED);
            log.error("Job id:{} failed with error", job.getId(), e);
            jobService.save(job);
        }
    }
}
