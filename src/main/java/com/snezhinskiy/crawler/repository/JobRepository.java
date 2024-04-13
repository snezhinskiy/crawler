package com.snezhinskiy.crawler.repository;

import com.snezhinskiy.crawler.domain.Job;
import com.snezhinskiy.crawler.domain.embedded.JobStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findJobByStatus(JobStatus status, Pageable pageable);
}
