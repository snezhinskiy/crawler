package com.snezhinskiy.crawler.processing.parser.helper;

import com.snezhinskiy.crawler.processing.model.CrawlerJob;
import com.snezhinskiy.crawler.processing.model.ParserJob;
import com.snezhinskiy.crawler.processing.parser.utils.UrlUtils;
import org.jsoup.nodes.Document;

public class ParserJobBuilder {

    public static ParserJob fromCrawlerJob(CrawlerJob crawlerJob, Document jsoupDocument) {
        final String url = crawlerJob.getUrl();
        String domain = UrlUtils.getHost(url);

        return ParserJob.builder()
            .rootJobId(crawlerJob.getRootJobId())
            .jobConfigId(crawlerJob.getJobConfigId())
            .contentType(crawlerJob.getContentType())
            .type(crawlerJob.getType())
            .url(url)
            .hash(url.hashCode())
            .domainHash(domain.hashCode())
            .parserMap(crawlerJob.getParserMap())
            .paginationSelector(crawlerJob.getPaginationSelector())
            .itemsSelector(crawlerJob.getItemsSelector())
            .base(UrlUtils.getHostWithSchema(url))
            .document(jsoupDocument)
            .runner(crawlerJob.getRunner())
            .build();
    }
}
