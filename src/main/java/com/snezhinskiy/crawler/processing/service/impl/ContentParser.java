package com.snezhinskiy.crawler.processing.service.impl;

import com.snezhinskiy.crawler.configuration.properties.ContentParserProperties;
import com.snezhinskiy.crawler.domain.embedded.ContentType;
import com.snezhinskiy.crawler.processing.model.ParsedContent;
import com.snezhinskiy.crawler.processing.model.ParserJob;
import com.snezhinskiy.crawler.processing.model.ProductData;
import com.snezhinskiy.crawler.processing.parser.MappedProductParser;
import com.snezhinskiy.crawler.processing.service.ContentParserJobService;
import com.snezhinskiy.crawler.processing.service.ContentWriteService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Log4j2
@Component
public class ContentParser implements Runnable {
    private final ExecutorService executorService;
    private final ContentParserJobService contentParserJobService;
    private final ContentWriteService contentWriteService;
    private final Map<ContentType, MappedProductParser> contentParsersMap;
    private Semaphore semaphore;

    public ContentParser(
        ContentParserProperties properties,
        @Qualifier("virtualThreadExecutor")
        ExecutorService executorService,
        ContentParserJobService contentParserJobService,
        ContentWriteService contentWriteService,
        Map<String, MappedProductParser> contentParsersMap
    ) {
        semaphore = new Semaphore(properties.getMaxWorkers());
        this.executorService = executorService;
        this.contentParserJobService = contentParserJobService;
        this.contentWriteService = contentWriteService;
        this.contentParsersMap = contentParsersMap.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> ContentType.valueOf(entry.getKey()), entry -> entry.getValue()
            ));
    }

    @Override
    public void run() {
        while(true) {
            try {
                ParserJob job = contentParserJobService.takeJob();
                semaphore.acquire();

                try {
                    executorService.submit(() -> parse(job));
                } finally {
                    semaphore.release();
                }

            } catch (RuntimeException e) {
                log.error("Content parse runner error", e);
                // do nothing
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void parse(ParserJob job) {
        try {
            if (!contentParsersMap.containsKey(job.getContentType())) {
                log.warn("Unexpected content type: {} in job: {}", job.getContentType(), job.getRootJobId());
                return;
            }

            log.debug("Start parse content of: {}", job);

            MappedProductParser parser = contentParsersMap.get(job.getContentType());
            List<ProductData> data = parser.parse(job);

            if (!CollectionUtils.isEmpty(data)) {
                log.info("BINGO! Number of goods parsed from: {} is: {}", job.getUrl(), data.size());

                ParsedContent content = ParsedContent.builder()
                    .hash(job.getHash())
                    .base(job.getBase())
                    .rootJobId(job.getRootJobId())
                    .jobConfigId(job.getJobConfigId())
                    .url(job.getUrl())
                    .contentType(job.getContentType())
                    .data(data)
                    .runner(job.getRunner())
                    .build();

                contentWriteService.save(content);
                log.info("Parsed goods successfully saved");
            } else {
                log.debug("No data found during process job: {}", job);
            }
        } catch (Exception e) {
            log.error("Error occured during document parsing", e);
        }
    }
}
