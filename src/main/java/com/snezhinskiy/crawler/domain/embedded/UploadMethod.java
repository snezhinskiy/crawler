package com.snezhinskiy.crawler.domain.embedded;

public enum UploadMethod {
    // for single page
    SINGLE(false),

    // for deep web resource crawling by url hierarchy
    HIERARCHICAL(true),

    // for traveling by pages
    SELECTIVELY(true);

    private boolean recursive;

    UploadMethod(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isRecursive() {
        return recursive;
    }
}
