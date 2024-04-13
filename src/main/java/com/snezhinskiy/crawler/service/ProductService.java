package com.snezhinskiy.crawler.service;

import com.snezhinskiy.crawler.domain.JobConfig;
import com.snezhinskiy.crawler.domain.Product;

import java.util.List;

public interface ProductService {
    Product save(Product entity);

    void save(List<Product> entityList);

    List<Product> getListByPageNumber(JobConfig config, int page, int pageSize);

    int count(JobConfig config);

    List<Product> getListBySku(String sku);
}
