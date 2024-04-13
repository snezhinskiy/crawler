package com.snezhinskiy.crawler.repository;

import com.snezhinskiy.crawler.domain.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends PagingAndSortingRepository<Product, Long>, JpaRepository<Product, Long> {
    Optional<Product> findByDomainHashAndHash(Integer domainHash, Integer hash);

    Optional<Product> findByDomainHashAndHashAndModificationCode(
        Integer domainHash, Integer hash, String modificationCode
    );

    List<Product> findBySkuLikeIgnoreCase(String sku);

    List<Product> findByDomainHash(Integer domainHash, Pageable pageable);

    Integer countByDomainHash(Integer domainHash);
}
