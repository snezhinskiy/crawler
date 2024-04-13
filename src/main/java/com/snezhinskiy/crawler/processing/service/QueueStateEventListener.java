package com.snezhinskiy.crawler.processing.service;

public interface QueueStateEventListener {
    void handleEvent(String producerName, Long jobId, Long count);
}
