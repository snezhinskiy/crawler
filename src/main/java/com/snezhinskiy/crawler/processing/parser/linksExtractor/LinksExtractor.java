package com.snezhinskiy.crawler.processing.parser.linksExtractor;

import com.snezhinskiy.crawler.domain.embedded.UploadMethod;
import com.snezhinskiy.crawler.processing.model.ParserJob;

import java.util.List;

public interface LinksExtractor {
    UploadMethod getType();
    List<String> extract(ParserJob parserJob);
}
