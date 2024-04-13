package com.snezhinskiy.crawler.api.controller;

import com.snezhinskiy.crawler.api.advice.exception.ApiValidationException;
import com.snezhinskiy.crawler.api.dto.ProductDataResponseItem;
import com.snezhinskiy.crawler.api.mapper.ProductDataResponseMapper;
import com.snezhinskiy.crawler.domain.JobConfig;
import com.snezhinskiy.crawler.service.JobConfigService;
import com.snezhinskiy.crawler.service.ProductService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@Log4j2
@RestController
@RequestMapping("/api/v1/product/")
@RequiredArgsConstructor
public class ProductController {
    private final static int MAX_PAGE_SIZE = 100;
    private final static int DEFAULT_PAGE_SIZE = 20;
    private final ProductService productService;
    private final JobConfigService configService;
    private final ProductDataResponseMapper responseMapper;

    @GetMapping(path = "/list-by-config/{configId}/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public Integer getListOfProductsByConfigId(
        @PathVariable @NotNull Long configId, @PathVariable Integer page
    ) {
        JobConfig config = configService.getById(configId);

        if (config == null) {
            throw new ApiValidationException("configId", "ProductController.errors.configId.configNotFound");
        }

        return productService.count(config);
    }

    @GetMapping(
        path = {"/list-by-config/{configId}", "/list-by-config/{configId}/page/{pageId}"},
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<ProductDataResponseItem> getListOfProductsByConfigId(
        @PathVariable @NotNull Long configId,
        @PathVariable(required = false) Integer page,
        @RequestParam(name = "pageSize", required = false) @Positive @Max(MAX_PAGE_SIZE) Integer pageSize
    ) {
        JobConfig config = configService.getById(configId);

        if (config == null) {
            throw new ApiValidationException("configId", "ProductController.errors.configId.configNotFound");
        }

        if (pageSize == null)
            pageSize = DEFAULT_PAGE_SIZE;

        if (page == null)
            page = 0;

        return productService.getListByPageNumber(config, page, pageSize).stream()
            .map(item -> responseMapper.map(item))
            .collect(Collectors.toList());
    }

    @GetMapping(path = {"/sku/{sku}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductDataResponseItem> getListBySku(@PathVariable @NotBlank String sku) {
        return productService.getListBySku(sku).stream()
            .map(item -> responseMapper.map(item))
            .collect(Collectors.toList());
    }
}
