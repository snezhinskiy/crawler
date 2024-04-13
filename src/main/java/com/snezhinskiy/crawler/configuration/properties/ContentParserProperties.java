package com.snezhinskiy.crawler.configuration.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "content-parser")
public class ContentParserProperties {
    @Min(1)
    @NotNull
    private Integer maxWorkers = 1;


    public Integer getMaxWorkers() {
        return maxWorkers;
    }

    public void setMaxWorkers(Integer maxWorkers) {
        this.maxWorkers = maxWorkers;
    }
}
