package com.snezhinskiy.crawler.processing.service;

public interface QueueStateEventProducer {
    void subscribe(QueueStateEventListener listener);
    void unsubscribe(QueueStateEventListener listener);
}
