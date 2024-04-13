package com.snezhinskiy.crawler.processing.scheduled;

import com.snezhinskiy.crawler.configuration.properties.PerformanceProperties;
import com.snezhinskiy.crawler.domain.Job;
import com.snezhinskiy.crawler.processing.service.impl.JobRunningService;
import com.snezhinskiy.crawler.processing.service.impl.JobStateObserver;
import com.snezhinskiy.crawler.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
@RequiredArgsConstructor
public class ScheduledJobRunner {
    private final PerformanceProperties properties;
    private final JobService jobService;
    private final JobRunningService jobRunningService;
    private final JobStateObserver jobStateObserver;

    @Scheduled(initialDelay = 0, fixedRate = 600, timeUnit = TimeUnit.SECONDS)
    public void run() {
        log.debug("ScheduledJobRunner started");

        final int acceptable = properties.getMaxSimultaneousJobs() - jobStateObserver.getJobsCount();

        if (acceptable <= 0) {
            log.debug("There are no available slots. Exit.");
            return;
        }

        List<Job> rootJobs = jobService.getReadyList(properties.getMaxSimultaneousJobs());

        log.debug("Jobs found: {}", rootJobs.size());

        if (rootJobs.size() > 0) {
            for (Job job: rootJobs) {
                jobRunningService.run(job);
            }
        }

        log.debug("ScheduledJobRunner finished");
    }
}
