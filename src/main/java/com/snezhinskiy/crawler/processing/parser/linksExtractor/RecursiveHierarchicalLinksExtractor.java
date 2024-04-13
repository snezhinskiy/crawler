package com.snezhinskiy.crawler.processing.parser.linksExtractor;

import com.snezhinskiy.crawler.domain.embedded.UploadMethod;
import com.snezhinskiy.crawler.processing.model.ParserJob;
import com.snezhinskiy.crawler.processing.parser.utils.UrlUtils;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.snezhinskiy.crawler.domain.embedded.UploadMethod.HIERARCHICAL;

@Component
public class RecursiveHierarchicalLinksExtractor implements LinksExtractor {

    @Override
    public UploadMethod getType() {
        return HIERARCHICAL;
    }

    public List<String> extract(ParserJob parserJob) {
        final Document document = parserJob.getDocument();

        final String baseUrl = UrlUtils.removeTrailingSlash(parserJob.getBase());

        final String hostWithSchema = UrlUtils.getHostWithSchema(baseUrl);
        final String domainName = UrlUtils.getHost(baseUrl);

        return document.getElementsByTag("a").stream()
            .map(el -> el.attr("href").trim())
            .filter(href -> UrlUtils.isDescendantOf(href, baseUrl))
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
