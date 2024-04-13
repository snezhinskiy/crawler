package com.snezhinskiy.crawler.service;

import com.snezhinskiy.crawler.domain.JobConfig;

import java.util.List;

public interface JobConfigService {
    JobConfig save(JobConfig entity);

    JobConfig getById(Long id);

    List<JobConfig> getEnabledList();

    List<JobConfig> findReadyToExecutionConfigs(Integer limit);

    List<JobConfig> findReadyToExecutionConfigs();
}
