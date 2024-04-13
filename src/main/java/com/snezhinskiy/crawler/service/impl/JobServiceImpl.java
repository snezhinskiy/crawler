package com.snezhinskiy.crawler.service.impl;

import com.snezhinskiy.crawler.domain.Job;
import com.snezhinskiy.crawler.domain.JobConfig;
import com.snezhinskiy.crawler.domain.embedded.JobStatus;
import com.snezhinskiy.crawler.repository.JobRepository;
import com.snezhinskiy.crawler.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {
    private final JobRepository repository;

    @Override
    public Job save(Job entity) {
        return repository.save(entity);
    }

    @Override
    public Job getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public List<Job> getReadyList(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return repository.findJobByStatus(JobStatus.READY, pageable);
    }

    @Override
    public Job createJob(JobConfig config) {
        Assert.notNull(config.getParserMap(), "parserMap must be not defined");

        Job job = Job.builder()
            .status(JobStatus.READY)
            .config(config)
            .uploadCounter(0)
            .documentCounter(0)
            .startedAt(LocalDateTime.now())
            .build();

        return repository.save(job);
    }

    @Override
    public void finishJob(Long jobId) {
        repository.findById(jobId).ifPresent(job -> {
            job.setFinishedAt(LocalDateTime.now());
            job.setStatus(JobStatus.FINISHED);
            repository.save(job);
        });
    }
}
