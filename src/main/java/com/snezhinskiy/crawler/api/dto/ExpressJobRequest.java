package com.snezhinskiy.crawler.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class ExpressJobRequest {
    @NotNull
    @Positive
    private Long mapId;

    @URL
    private String url;
}
