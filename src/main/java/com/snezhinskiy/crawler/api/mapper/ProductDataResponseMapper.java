package com.snezhinskiy.crawler.api.mapper;

import com.snezhinskiy.crawler.api.dto.ProductDataResponseItem;
import com.snezhinskiy.crawler.domain.Product;
import com.snezhinskiy.crawler.processing.model.ProductData;
import org.springframework.stereotype.Component;

@Component
public class ProductDataResponseMapper {
    public ProductDataResponseItem map(ProductData itemData) {
        return ProductDataResponseItem.builder()
            .sku(itemData.getSku())
            .name(itemData.getName())
            .modificationCode(itemData.getModificationCode())
            .modificationName(itemData.getModificationName())
            .price(itemData.getPrice())
            .description(itemData.getDescription())
            .url(itemData.getUrl())
            .previewUrl(itemData.getPreviewUrl())
            .stock(itemData.getStock())
            .build();
    }

    public ProductDataResponseItem map(Product product) {
        return ProductDataResponseItem.builder()
            .sku(product.getSku())
            .name(product.getName())
            .modificationCode(product.getModificationCode())
            .modificationName(product.getModificationName())
            .price(product.getPrice())
            .description(product.getDescription())
            .url(product.getUrl())
            .previewUrl(product.getPreviewUrl())
            .stock(product.getStock())
            .build();
    }
}
