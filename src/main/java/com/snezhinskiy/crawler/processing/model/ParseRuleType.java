package com.snezhinskiy.crawler.processing.model;

public enum ParseRuleType {
    CSS_SELECTOR,
    TAG_SELECTOR,

    MATCHER,
    REPLACER,
    FILTER,
    TRIMMER,
    LIMITER,
    FLATTENER,
    CONCATENATOR,
    SPLITTER,
    TAGS_STRIPPER,

    JSON_TO_MAP_PARSER,
    MAP_FLATTENER,
    ELEMENTS_COMBINER
}
