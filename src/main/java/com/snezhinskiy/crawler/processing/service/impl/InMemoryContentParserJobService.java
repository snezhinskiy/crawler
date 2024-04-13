package com.snezhinskiy.crawler.processing.service.impl;

import com.snezhinskiy.crawler.processing.model.ParserJob;
import com.snezhinskiy.crawler.processing.service.ContentParserJobService;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Log4j2
public class InMemoryContentParserJobService extends BaseQueueStateEventProducer implements ContentParserJobService {
    /**
     * This queue expected to be very small and fast processed, so we put all jobs together
     */
    private LinkedBlockingDeque<ParserJob> queue;
    private ReentrantLock lock;

    public InMemoryContentParserJobService(int initialCapacity) {
        queue = new LinkedBlockingDeque<>(initialCapacity);
        lock = new ReentrantLock();
    }

    @Override
    public ParserJob takeJob() {
        try {
            ParserJob job = queue.take();

            lock.lock();

            try {
                if (!job.isExpress() && job.getRootJobId() != null) {
                    notifyListeners(job.getRootJobId(), getSimilarJobsCount(job));
                }

                return job;
            }finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addJob(ParserJob job) {
        lock.lock();

        try {
            if (job.isExpress()) {
                log.debug("Try to put express job at the beginning of the queue, url {}", job.getUrl());
                queue.putFirst(job);
                log.debug("Job putted successfully, url {}", job.getUrl());
            } else {
                if (queue.offer(job)) {
                    if (job.getRootJobId() != null) {
                        notifyListeners(job.getRootJobId(), getSimilarJobsCount(job));
                    }
                } else {
                    // Avoid a deadlock but lost data.
                    log.warn("Content job queue capacity {}, isn't enough", queue.size());
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    private Long getSimilarJobsCount(ParserJob job) {
        return queue.stream()
            .filter(parserJob -> parserJob.getRootJobId().equals(job.getRootJobId()))
            .count();
    }
}
