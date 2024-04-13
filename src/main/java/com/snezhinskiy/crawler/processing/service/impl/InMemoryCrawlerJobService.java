package com.snezhinskiy.crawler.processing.service.impl;

import com.snezhinskiy.crawler.processing.model.CrawlerJob;
import com.snezhinskiy.crawler.processing.service.CrawlerJobService;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Log4j2
public class InMemoryCrawlerJobService extends BaseQueueStateEventProducer implements CrawlerJobService {
    private static final long EXPRESS_QUEUE_ID = 0;
    private Map<Long, LinkedBlockingQueue<CrawlerJob>> queueMap;
    private Map<Long, Integer> idleTimeoutMap;
    private Map<Long, Long> idleTimerMap;
    private ReadWriteLock lock;
    private final int initialQueueCapacity;
    private final int defaultIdleTimeout;

    /**
     * Expected that it may be only one listener
     */
    private Runnable onExpressJobAddListener;

    public InMemoryCrawlerJobService(int initialCapacity, int defaultIdleTimeout) {
        Assert.isTrue(initialCapacity > 0, "Expected capacity will me greater than 0");
        this.initialQueueCapacity = initialCapacity;
        this.defaultIdleTimeout = defaultIdleTimeout;
        this.queueMap = new ConcurrentHashMap<>();
        this.idleTimeoutMap = new ConcurrentHashMap<>();
        this.idleTimerMap = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public CrawlerJob getJob() {
        lock.writeLock().lock();

        try {
            for (Long queueId: queueMap.keySet()) {
                if (isJobAvailable(queueId)) {
                    final LinkedBlockingQueue<CrawlerJob> queue = queueMap.get(queueId);
                    final CrawlerJob job = queue.poll();

                    if (job != null) {
                        notifyListeners(queueId);
                    }

                    if (queue.isEmpty()) {
                        // if more jobs for this rootId, then remove this Id from all maps
                        queueMap.remove(queueId);
                        idleTimeoutMap.remove(queueId);
                        idleTimerMap.remove(queueId);
                    } else {
                        final int idleTimeout = idleTimeoutMap.get(queueId);
                        long nextTimer = System.currentTimeMillis() + idleTimeout;
                        idleTimerMap.put(queueId, nextTimer);
                    }

                    return job;
                }
            }
        } finally {
            lock.writeLock().unlock();
        }

        return null;
    }

    @Override
    public void addJob(CrawlerJob job) {
        Long queueId = job.isExpress() ? EXPRESS_QUEUE_ID : job.getRootJobId();
        Assert.notNull(queueId, "Missconfigured job");

        lock.writeLock().lock();

        try {
            if (!queueMap.containsKey(queueId)) {
                queueMap.put(queueId, new LinkedBlockingQueue<CrawlerJob>(initialQueueCapacity));
            }

            LinkedBlockingQueue<CrawlerJob> queue = queueMap.get(queueId);

            if (queueMap.get(queueId).offer(job) == true) {
                log.debug("Crawler in-memory queue remaining capacity: {}", queue.remainingCapacity());

                notifyListeners(queueId);

                if (!job.isExpress() && !idleTimeoutMap.containsKey(queueId)) {
                    // At leas 100 m.sec
                    int idleTimeout = job.getIdleTimeout() != null && job.getIdleTimeout() > 99
                        ? job.getIdleTimeout()
                        : defaultIdleTimeout;

                    idleTimeoutMap.put(queueId, idleTimeout);
                }

                if (job.isExpress() && onExpressJobAddListener != null) {
                    this.onExpressJobAddListener.run();
                    this.onExpressJobAddListener = null;
                }
            } else {
                // Avoid a deadlock but lost data.
                log.warn("Crawler job capacity {}, isn't enough", queue.size());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public long getIdleTimeOrNotify(Runnable listener) {
        lock.readLock().lock();
        this.onExpressJobAddListener = listener;

        try {
            Long minIdleTime = null;

            for (Long queueId: queueMap.keySet()) {
                if (!queueMap.get(queueId).isEmpty()) {
                    if (!idleTimerMap.containsKey(queueId)) {
                        return 0;
                    } else if (minIdleTime == null || idleTimerMap.get(queueId) < minIdleTime) {
                        minIdleTime = idleTimerMap.get(queueId);
                    }
                }
            }

            return minIdleTime == null
                ? defaultIdleTimeout
                : minIdleTime - System.currentTimeMillis();

        } finally {
            lock.readLock().unlock();
        }
    }

    private boolean isJobAvailable(Long queueId) {
        if (!queueMap.containsKey(queueId) || queueMap.get(queueId).isEmpty()) {
            return false;
        }

        return !idleTimerMap.containsKey(queueId)
            || idleTimerMap.get(queueId) < System.currentTimeMillis();
    }

    private void notifyListeners(Long queueId) {
        if (EXPRESS_QUEUE_ID == queueId)
            return;

        if (queueId != null && queueMap.containsKey(queueId)) {
            long count = (long) queueMap.get(queueId).size();
            notifyListeners(queueId, count);
        }
    }
}
