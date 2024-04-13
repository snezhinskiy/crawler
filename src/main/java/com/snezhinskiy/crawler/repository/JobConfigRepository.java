package com.snezhinskiy.crawler.repository;


import com.snezhinskiy.crawler.domain.JobConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface JobConfigRepository extends JpaRepository<JobConfig, Long> {
    List<JobConfig> findAllByEnabledIsTrue();

    @Query(value = "select jc.* " +
        "from job_config as jc left join job " +
        "    on (" +
        "        job.config_id = jc.id " +
        "        AND job.created_at >= (now() - make_interval(days => schedule_interval)) " +
        "        AND job.created_at <= NOW() " +
        "       ) " +
        "where jc.enabled is true and job.id is null and jc.schedule_time <= CURRENT_TIME " +
        "LIMIT :limit", nativeQuery = true)
    @Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
    List<JobConfig> findReadyToExecutionConfigs(@Param("limit") Integer limit);
}
