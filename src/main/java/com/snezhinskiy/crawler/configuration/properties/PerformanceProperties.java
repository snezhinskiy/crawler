package com.snezhinskiy.crawler.configuration.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "performance-settings")
public class PerformanceProperties {
    @Min(1)
    @NotNull
    private Integer maxSimultaneousJobs = 2;

    public Integer getMaxSimultaneousJobs() {
        return maxSimultaneousJobs;
    }

    public void setMaxSimultaneousJobs(Integer maxSimultaneousJobs) {
        this.maxSimultaneousJobs = maxSimultaneousJobs;
    }
}
