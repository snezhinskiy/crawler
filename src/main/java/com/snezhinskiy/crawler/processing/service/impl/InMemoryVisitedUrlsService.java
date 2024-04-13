package com.snezhinskiy.crawler.processing.service.impl;

import com.snezhinskiy.crawler.processing.service.VisitedUrlsService;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static com.snezhinskiy.crawler.processing.parser.utils.UrlUtils.*;

public class InMemoryVisitedUrlsService implements VisitedUrlsService {
    private Map<Integer, Set<Integer>> cache;
    private ReentrantLock lock;

    public InMemoryVisitedUrlsService() {
        cache = new ConcurrentHashMap<>();
        lock = new ReentrantLock();
    }

    @Override
    public boolean isVisited(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }

        if (isMediaFileUrl(url)) {
            /**
             * We are not interested in media files, so we
             * pretend that we have already visited them
             */
            return true;
        }

        final String host = getHost(url);

        if (!StringUtils.hasText(host)) {
            /**
             * Prevent adding wrong url
             */
            return true;
        }

        final int hostHashCode = host.hashCode();
        final int pathHashCode = calcAbsolutePathAndQueryHash(url);
        final Set<Integer> hostQueue = getHostQueue(hostHashCode);

        return hostQueue.contains(pathHashCode);
    }

    @Override
    public boolean addIfNotVisited(String url) {
        if (isMediaFileUrl(url)) {
            // Short way for media files. Always skip media
            return true;
        }

        final String host = getHost(url);

        if (!StringUtils.hasText(host)) {
            /**
             * Prevent adding wrong url
             */
            return true;
        }

        final int hostHashCode = host.hashCode();
        final int pathHashCode = calcAbsolutePathAndQueryHash(url);
        final Set<Integer> hostQueue = getHostQueue(hostHashCode);

        if (!hostQueue.contains(pathHashCode)) {
            hostQueue.add(pathHashCode);
            return true;
        }

        return false;
    }

    @Override
    public void resetAllForHost(String url) {
        lock.lock();

        try {
            int hostHashCode = getHost(url).hashCode();

            if (cache.containsKey(hostHashCode)) {
                cache.remove(hostHashCode);
            }
        } finally {
            lock.unlock();
        }
    }

    private Set<Integer> getHostQueue(Integer hostHashCode) {
        lock.lock();

        try {
            if (!cache.containsKey(hostHashCode)) {
                cache.put(hostHashCode, Collections.synchronizedSet(new HashSet<Integer>()));
            }

            return cache.get(hostHashCode);
        } finally {
            lock.unlock();
        }
    }
}
