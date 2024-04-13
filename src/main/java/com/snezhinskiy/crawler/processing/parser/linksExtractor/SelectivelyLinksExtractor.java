package com.snezhinskiy.crawler.processing.parser.linksExtractor;

import com.snezhinskiy.crawler.domain.embedded.UploadMethod;
import com.snezhinskiy.crawler.processing.model.ParserJob;
import com.snezhinskiy.crawler.processing.parser.utils.UrlUtils;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.snezhinskiy.crawler.domain.embedded.UploadMethod.SELECTIVELY;

@Component
public class SelectivelyLinksExtractor implements LinksExtractor {

    @Override
    public UploadMethod getType() {
        return SELECTIVELY;
    }

    public List<String> extract(ParserJob parserJob) {
        Assert.hasText(parserJob.getPaginationSelector(), "Expected not empty paginationSelector");
        Assert.hasText(parserJob.getItemsSelector(), "Expected not empty itemsSelector");

        final Document document = parserJob.getDocument();
        final String baseUrl = UrlUtils.removeTrailingSlash(parserJob.getBase());

        final String hostWithSchema = UrlUtils.getHostWithSchema(baseUrl);
        final String domainName = UrlUtils.getHost(baseUrl.toLowerCase());

        return Stream.concat(
                document.select(parserJob.getPaginationSelector()).stream(),
                document.select(parserJob.getItemsSelector()).stream()
            )
            .filter(el -> el.tagName().toLowerCase().equals("a"))
            .map(el -> el.attr("href").trim())
            .filter(href -> UrlUtils.isSameSchemaAndHost(href, hostWithSchema))
            .map(href -> {
                if (href.startsWith("/")) {
                    return hostWithSchema + href;
                } else if (href.startsWith(domainName)) {
                    return hostWithSchema + href.substring(domainName.length(), href.length());
                }

                return href;
            })
            .collect(Collectors.toList());
    }
}
