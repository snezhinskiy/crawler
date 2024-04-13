package com.snezhinskiy.crawler.processing.service.impl;

import com.snezhinskiy.crawler.processing.model.ParserJob;
import com.snezhinskiy.crawler.processing.service.LinksParserJobService;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.Assert;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
public class InMemoryLinksParserJobService extends BaseQueueStateEventProducer implements LinksParserJobService {
    /**
     * This queue expected to be very small and fast processed, so we put all jobs together
     */
    private LinkedBlockingDeque<ParserJob> queue;
    private ReentrantLock lock;

    public InMemoryLinksParserJobService(int initialCapacity) {
        Assert.isTrue(initialCapacity > 0, "Expected capacity will me greater than 0");
        queue = new LinkedBlockingDeque<>(initialCapacity);
        lock = new ReentrantLock();
    }

    @Override
    public ParserJob takeJob() {
        try {
            ParserJob job = queue.takeFirst();
            lock.lock();

            try {
                if (job.isExpress() == false && job.getRootJobId() != null) {
                    notifyListeners(job.getRootJobId(), getSimilarJobsCount(job));
                }

                return job;
            } finally {
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
                if (queue.offer(job) == true) {
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
