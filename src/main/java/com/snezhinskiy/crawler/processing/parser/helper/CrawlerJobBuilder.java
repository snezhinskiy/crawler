package com.snezhinskiy.crawler.processing.parser.helper;

import com.snezhinskiy.crawler.domain.Job;
import com.snezhinskiy.crawler.domain.JobConfig;
import com.snezhinskiy.crawler.domain.SourceParserMap;
import com.snezhinskiy.crawler.domain.embedded.UploadMethod;
import com.snezhinskiy.crawler.processing.model.CrawlerJob;
import com.snezhinskiy.crawler.processing.model.ParserJob;
import com.snezhinskiy.crawler.processing.model.ProductData;
import com.snezhinskiy.crawler.processing.model.map.BaseContentParserMap;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CrawlerJobBuilder {

    public static CrawlerJob fromRequest(
        SourceParserMap parserMap, BaseContentParserMap contentParserMap, String url, CompletableFuture<List<ProductData>> runner
    ) {
        return CrawlerJob.builder()
            .rootJobId(null)
            .contentType(parserMap.getContentType())
            .type(UploadMethod.SINGLE)
            .url(url)
            .parserMap(contentParserMap)
            .runner(runner)
            .build();
    }

    public static CrawlerJob fromScratch(JobConfig config, Job job, BaseContentParserMap contentParserMap) {
        return CrawlerJob.builder()
            .rootJobId(job.getId())
            .jobConfigId(config.getId())
            .contentType(config.getParserMap().getContentType())
            .type(config.getUploadMethod())
            .url(config.getUrl())
            .parserMap(contentParserMap)
            .paginationSelector(config.getPaginationSelector())
            .itemsSelector(config.getItemsSelector())
            .build();
    }

    public static CrawlerJob fromParserJob(ParserJob parserJob, String url) {
        return CrawlerJob.builder()
            .rootJobId(parserJob.getRootJobId())
            .jobConfigId(parserJob.getJobConfigId())
            .contentType(parserJob.getContentType())
            .type(parserJob.getType())
            .url(url)
            .parserMap(parserJob.getParserMap())
            .paginationSelector(parserJob.getPaginationSelector())
            .itemsSelector(parserJob.getItemsSelector())
            .build();
    }
}
