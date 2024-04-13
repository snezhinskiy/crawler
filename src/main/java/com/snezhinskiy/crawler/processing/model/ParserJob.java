package com.snezhinskiy.crawler.processing.model;

import com.snezhinskiy.crawler.domain.embedded.ContentType;
import com.snezhinskiy.crawler.domain.embedded.UploadMethod;
import com.snezhinskiy.crawler.processing.model.map.BaseContentParserMap;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@Builder
public class ParserJob {
    private Long rootJobId;
    private Long jobConfigId;
    private UploadMethod type;
    private ContentType contentType;
    private BaseContentParserMap parserMap;
    private String url;
    private String base;
    private Integer hash;
    private Integer domainHash;
    private Document document;
    private String paginationSelector;
    private String itemsSelector;
    private List<String> links;
    private CompletableFuture<List<ProductData>> runner;

    public boolean isExpress() {
        return runner != null;
    }


    @Override
    public String toString() {
        return "ParserJob{" +
            "rootJobId=" + rootJobId +
            ", jobConfigId=" + jobConfigId +
            ", type=" + type +
            ", contentType=" + contentType +
            ", domainHash=" + domainHash +
            ", hash=" + hash +
            ", url='" + url + '\'' +
            ", paginationSelector=" + paginationSelector +
            ", itemsSelector=" + itemsSelector +
            ", isExpress=" + isExpress() +
            '}';
    }
}
