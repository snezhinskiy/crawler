package com.snezhinskiy.crawler.processing.scheduled;

import com.snezhinskiy.crawler.processing.scheduled.handler.JobCreateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Log4j2
@Service
@RequiredArgsConstructor
public class ScheduledJobCreator {
    private final JobCreateHandler handler;

    @Scheduled(initialDelay = 0, fixedRate = 3600, timeUnit = TimeUnit.SECONDS)
    public void run() {
        log.debug("ScheduledJobCreator started");
        handler.run();
        log.debug("ScheduledJobCreator finished");
    }
}
