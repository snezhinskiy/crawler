package com.snezhinskiy.crawler.service.impl;

import com.snezhinskiy.crawler.domain.Product;
import com.snezhinskiy.crawler.processing.model.ParsedContent;
import com.snezhinskiy.crawler.processing.service.ContentWriteService;
import com.snezhinskiy.crawler.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class ContentWriteServiceImpl implements ContentWriteService {

    private final ProductService productService;

    public ContentWriteServiceImpl(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void save(ParsedContent content) {
        if (CollectionUtils.isEmpty(content.getData())) {
            log.debug("Empty data for job: {}, hash: {}", content.getRootJobId(), content.getHash());
            return;
        }

        if (content.isExpress()) {
            log.debug("Express job done, hash: {}", content.getHash());
            content.getRunner().complete(content.getData());
            return;
        }

        List<Product> products = content.getData().stream()
            .filter(data -> data != null)
            .map(data -> {
                Product product = new Product();
                product.setName(data.getName());
                product.setSku(data.getSku());
                product.setHash(data.getHash());
                product.setPrice(data.getPrice());
                product.setCreatedAt(LocalDateTime.now());
                product.setDomainHash(data.getDomainHash());
                product.setModificationCode(data.getModificationCode());
                product.setModificationName(data.getModificationName());
                product.setDescription(data.getDescription());
                product.setPreviewUrl(data.getPreviewUrl());
                product.setStock(data.getStock());
                product.setUrl(data.getUrl());
                return product;
            })
            .collect(Collectors.toList());

        if (!products.isEmpty()) {
            productService.save(products);

            log.debug("Number of persisted products: {}, for job: {}", products.size(), content.getRootJobId());
        }
    }
}
