package com.snezhinskiy.crawler.processing.service;

public interface OnDemandNotifier {
    long getIdleTimeOrNotify(Runnable listener);
}
