package com.snezhinskiy.crawler.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableScheduling
public class ExecutorsConfiguration implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(
            Executors.newScheduledThreadPool(0, Thread.ofVirtual().factory())
        );
    }

    @Bean(name = "schedulerExecutor")
    @Primary
    public ScheduledExecutorService schedulerExecutor() {
        return Executors.newScheduledThreadPool(5);
    }


    @Bean(name = "taskExecutor")
    public Executor taskScheduler() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
