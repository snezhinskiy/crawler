package com.snezhinskiy.crawler.processing.service.impl;

import com.snezhinskiy.crawler.processing.service.QueueStateEventListener;
import com.snezhinskiy.crawler.processing.service.QueueStateEventProducer;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseQueueStateEventProducer implements QueueStateEventProducer {
    private List<QueueStateEventListener> listeners = new ArrayList<>();

    protected void notifyListeners(Long jobId, Long count) {
        if (!CollectionUtils.isEmpty(listeners)) {
            final String name = getClass().getSimpleName();

            listeners.forEach(listener -> listener.handleEvent(name, jobId, count));
        }
    }

    @Override
    public void subscribe(QueueStateEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void unsubscribe(QueueStateEventListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }
}
