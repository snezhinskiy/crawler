package com.snezhinskiy.crawler.processing.model;

import com.snezhinskiy.crawler.domain.embedded.ContentType;
import com.snezhinskiy.crawler.domain.embedded.UploadMethod;
import com.snezhinskiy.crawler.processing.model.map.BaseContentParserMap;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@Builder
public class CrawlerJob {
    private Long rootJobId;
    private Long jobConfigId;
    private UploadMethod type;
    private ContentType contentType;
    private BaseContentParserMap parserMap;
    private String paginationSelector;
    private String itemsSelector;
    private String url;
    private Integer idleTimeout;
    private CompletableFuture<List<ProductData>> runner;

    public boolean isExpress() {
        return runner != null;
    }

    @Override
    public String toString() {
        return "CrawlerJob{" +
            "rootJobId=" + rootJobId +
            ", jobConfigId=" + jobConfigId +
            ", type=" + type +
            ", contentType=" + contentType +
            ", parserMap=" + parserMap +
            ", url='" + url + '\'' +
            ", paginationSelector=" + paginationSelector +
            ", itemsSelector=" + itemsSelector +
            ", idleTimeout=" + idleTimeout +
            ", isExpress=" + isExpress() +
            '}';
    }
}