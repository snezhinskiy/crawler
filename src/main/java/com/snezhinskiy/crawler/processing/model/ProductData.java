package com.snezhinskiy.crawler.processing.model;

import lombok.Data;

@Data
public class ProductData {
    private Integer domainHash;
    private Integer hash;
    private String sku;
    private String name;
    private Double price;
    private String modificationCode;
    private String modificationName;
    private String description;
    private String previewUrl;
    private Integer stock;
    private String url;
}
