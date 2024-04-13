package com.snezhinskiy.crawler.api.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductDataResponseItem {
    private String sku;
    private String name;
    private String modificationCode;
    private String modificationName;
    private Double price;
    private String description;
    private String url;
    private String previewUrl;
    private Integer stock;
}
