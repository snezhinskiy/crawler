package com.snezhinskiy.crawler.processing.scheduled.handler;

import com.snezhinskiy.crawler.domain.Job;
import com.snezhinskiy.crawler.domain.JobConfig;
import com.snezhinskiy.crawler.domain.embedded.JobStatus;
import com.snezhinskiy.crawler.service.JobConfigService;
import com.snezhinskiy.crawler.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class JobCreateHandler {
    private static final int BATCH_LIMIT = 2;

    private final JobConfigService jobConfigService;
    private final JobService jobService;

    public void run() {
        List<JobConfig> configList =  jobConfigService.findReadyToExecutionConfigs(BATCH_LIMIT);

        if (CollectionUtils.isEmpty(configList)) {
            log.debug("No ready to execution configs found");
            return;
        }

        log.debug("Found ready to execution configs: {}", configList.size());

        for (JobConfig config: configList) {
            log.info("Start process config id:{} , name:{}", config.getId(), config.getName());

            try {
                Job job = jobService.createJob(config);
                log.debug("For config: {}, job created: {}", config.getId(), job);
            } catch (IllegalArgumentException e) {
                log.error("Unable to create job for config: {}. Config will be disabled", config.getId(), e);
                config.setEnabled(false);
                jobConfigService.save(config);
            } catch (RuntimeException e) {
                log.error("Config process error", e);
            }
        }
    }
}
