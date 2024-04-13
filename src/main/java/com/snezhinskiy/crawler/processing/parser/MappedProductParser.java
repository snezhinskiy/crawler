package com.snezhinskiy.crawler.processing.parser;

import com.snezhinskiy.crawler.processing.model.ParserJob;
import com.snezhinskiy.crawler.processing.model.ProductData;

import java.util.List;

public interface MappedProductParser {
    List<ProductData> parse(ParserJob parserJob);
}
