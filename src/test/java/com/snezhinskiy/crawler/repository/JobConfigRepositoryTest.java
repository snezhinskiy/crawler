package com.snezhinskiy.crawler.repository;

import com.snezhinskiy.crawler.domain.Job;
import com.snezhinskiy.crawler.domain.JobConfig;
import com.snezhinskiy.crawler.domain.SourceParserMap;
import com.snezhinskiy.crawler.domain.embedded.ContentType;
import com.snezhinskiy.crawler.domain.embedded.JobStatus;
import com.snezhinskiy.crawler.domain.embedded.UploadMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class JobConfigRepositoryTest {
    @Autowired
    JobConfigRepository configRepository;

    @Autowired
    SourceParserMapRepository mapRepository;

    @Autowired
    JobRepository jobRepository;

    @AfterEach
    void tearDown() {
        jobRepository.deleteAll();
        configRepository.deleteAll();
    }

    private static Stream<Object> argumentsProvider() {
        return Stream.of(
            // Expected to be found. Previously created - 7 days and 1 min from now
            Arguments.of(7, true, LocalDateTime.now().minusDays(7).minusMinutes(1), getTime(-1), 1),

            // Expected to be found. Previously created - 1 year 7 days and 1 min from now
            Arguments.of(7, true, LocalDateTime.now().minusYears(1).minusDays(7).plusMinutes(1), getTime(-1), 1),

            // Expected NOT be found. Previously created - less than 7 days ago
            Arguments.of(7, true, LocalDateTime.now().minusDays(7).plusMinutes(1), getTime(-1), 0),

            // Expected NOT be found. Record in future found
            Arguments.of(7, true, LocalDateTime.now().plusDays(1), getTime(-1), 1),

            // Expected NOT be found. Scheduled time is in future
            Arguments.of(7, true, LocalDateTime.now().minusDays(7).minusMinutes(1), getTime(1), 0),

            // Expected to be found. Config disabled
            Arguments.of(7, false, LocalDateTime.now().minusDays(7).minusMinutes(1), getTime(-1), 0)
        );
    }

    @MethodSource("argumentsProvider")
    @ParameterizedTest
    void findReadyToExecutionConfigs(
        int interval, boolean configEnabled, LocalDateTime jobCreatedAt, Time scheduleTime, int found
    ) {
        JobConfig config = createConfig(interval, configEnabled, scheduleTime);
        createJob(config, jobCreatedAt);

        List<JobConfig> result = configRepository.findReadyToExecutionConfigs(null);

        assertEquals(found, result.size());
    }

    private static Time getTime(int minShift) {
        return new Time(System.currentTimeMillis() + minShift * 60 * 1000);
    }

    private Job createJob(JobConfig config, LocalDateTime createdAt) {
        Job job = Job.builder()
            .config(config)
            .status(JobStatus.READY)
            .createdAt(createdAt)
            .build();

        jobRepository.save(job);
        job.setCreatedAt(createdAt);

        return jobRepository.save(job);
    }

    private JobConfig createConfig(int interval, boolean enabled, Time scheduledTime) {
        SourceParserMap map = SourceParserMap.builder()
            .name("test")
            .contentType(ContentType.PRODUCT_WITH_MODIFICATIONS)
            .body("{}")
            .build();

        mapRepository.save(map);

        JobConfig config = new JobConfig();
        config.setName("test-"+interval);
        config.setEnabled(enabled);
        config.setScheduleInterval(interval);
        config.setScheduleTime(scheduledTime);
        config.setUrl("empty-url");
        config.setUploadMethod(UploadMethod.HIERARCHICAL);
        config.setParserMap(map);
        return configRepository.save(config);
    }
}