package com.snezhinskiy.crawler.service.impl;

import com.snezhinskiy.crawler.domain.JobConfig;
import com.snezhinskiy.crawler.repository.JobConfigRepository;
import com.snezhinskiy.crawler.service.JobConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobConfigServiceImpl implements JobConfigService {
    private final JobConfigRepository repository;

    @Override
    public JobConfig save(JobConfig entity) {
        return repository.save(entity);
    }

    @Override
    public JobConfig getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public List<JobConfig> getEnabledList() {
        return repository.findAllByEnabledIsTrue();
    }

    @Override
    public List<JobConfig> findReadyToExecutionConfigs(Integer limit) {
        return repository.findReadyToExecutionConfigs(limit);
    }

    @Override
    public List<JobConfig> findReadyToExecutionConfigs() {
        return repository.findReadyToExecutionConfigs(null);
    }
}
