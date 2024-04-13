package com.snezhinskiy.crawler.processing.service.impl;

import com.snezhinskiy.crawler.processing.service.QueueStateEventListener;
import com.snezhinskiy.crawler.processing.service.QueueStateEventProducer;
import com.snezhinskiy.crawler.service.JobService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Log4j2
@Service
public class JobStateObserver implements QueueStateEventListener {
    private final Map<Long, Map<String, Long>> queuesStateMap;
    private final JobService jobService;
    private ReentrantLock lock;


    public JobStateObserver(List<QueueStateEventProducer> eventProducers, JobService jobService) {
        Assert.isTrue(!CollectionUtils.isEmpty(eventProducers), "Not empty producers list expected");

        this.queuesStateMap = new HashMap<>();
        this.jobService = jobService;
        this.lock = new ReentrantLock();

        eventProducers.forEach(p -> p.subscribe(this));
    }

    @Override
    public void handleEvent(String producerName, Long jobId, Long count) {
        Assert.isTrue(count != null && count >= 0, "Unexpected count value");

        lock.lock();

        try {
            queuesStateMap.compute(jobId, (jobIdKey, innerMap) -> {
                if (innerMap == null) {
                    innerMap = new HashMap<>();
                }

                innerMap.putIfAbsent(producerName, count);
                return innerMap;
            });

            if (getCountForJobId(jobId) == 0) {
                queuesStateMap.remove(jobId);

                CompletableFuture.runAsync(() -> {
                    jobService.finishJob(jobId);
                }).exceptionally(ex -> {
                    log.error("Got error during job finish attempt", ex);
                    return null;
                });
            }
        } finally {
            lock.unlock();
        }
    }

    public long getCountForJobId(Long jobId) {
        lock.lock();

        try {
            return queuesStateMap.getOrDefault(jobId, Collections.emptyMap()).values().stream()
                .collect(Collectors.summingLong(v -> v));
        } finally {
            lock.unlock();
        }
    }

    public int getJobsCount() {
        lock.lock();

        try {
            return queuesStateMap.keySet().size();
        } finally {
            lock.unlock();
        }
    }
}
