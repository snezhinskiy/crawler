package com.snezhinskiy.crawler.processing.model;

import com.snezhinskiy.crawler.domain.embedded.ContentType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@Builder
public class ParsedContent {
    private Long rootJobId;
    private Long jobConfigId;
    private ContentType contentType;
    private String url;
    private String base;
    private int hash;
    private int domainHash;
    private List<ProductData> data;
    private CompletableFuture<List<ProductData>> runner;

    public boolean isExpress() {
        return runner != null;
    }
}
