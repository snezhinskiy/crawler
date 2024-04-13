package com.snezhinskiy.crawler.service;

import com.snezhinskiy.crawler.domain.Job;
import com.snezhinskiy.crawler.domain.JobConfig;

import java.util.List;

public interface JobService {
    Job save(Job entity);

    Job getById(Long id);

    List<Job> getReadyList(int limit);

    Job createJob(JobConfig config);

    void finishJob(Long jobId);
}
