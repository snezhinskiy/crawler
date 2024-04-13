package com.snezhinskiy.crawler.service.impl;

import com.snezhinskiy.crawler.domain.JobConfig;
import com.snezhinskiy.crawler.domain.Product;
import com.snezhinskiy.crawler.processing.model.ParsedContent;
import com.snezhinskiy.crawler.processing.parser.utils.UrlUtils;
import com.snezhinskiy.crawler.processing.service.ContentWriteService;
import com.snezhinskiy.crawler.repository.ProductRepository;
import com.snezhinskiy.crawler.service.ProductService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
	private final ProductRepository repository;

	@Transactional
	@Override
	public Product save(Product entity) {
		Product old = findProduct(entity);

		if (old != null) {
			BeanUtils.copyProperties(entity, old, "id");
			return repository.save(old);
		}

		return repository.save(entity);
	}

	@Transactional
	@Override
	public void save(List<Product> entityList) {
		if (!CollectionUtils.isEmpty(entityList)) {
			for (Product entity: entityList) {
				save(entity);
			}
		}
	}

	@Transactional(readOnly = true)
	@Override
	public List<Product> getListByPageNumber(JobConfig config, int page, int pageSize) {
		final String rootUrl = config.getUrl();
		final Integer domainHash = UrlUtils.getHost(rootUrl).hashCode();
		PageRequest pageable = PageRequest.of(page, pageSize, Sort.by("id").ascending());

		return repository.findByDomainHash(domainHash, pageable);
	}

	@Transactional(readOnly = true)
	@Override
	public int count(JobConfig config) {
		final String rootUrl = config.getUrl();
		final Integer domainHash = UrlUtils.getHost(rootUrl).hashCode();
		return repository.countByDomainHash(domainHash);
	}

	@Transactional(readOnly = true)
	@Override
	public List<Product> getListBySku(String sku) {
		return repository.findBySkuLikeIgnoreCase(sku);
	}

	@Transactional(readOnly = true)
	protected Product findProduct(Product entity) {
		if (entity.getId() != null) {
			return repository.findById(entity.getId()).orElse(null);
		}

		if (entity.getModificationCode() != null) {
			return repository.findByDomainHashAndHashAndModificationCode(
				entity.getDomainHash(), entity.getHash(), entity.getModificationCode()
			).orElse(null);
		}

		return repository.findByDomainHashAndHash(entity.getDomainHash(), entity.getHash()).orElse(null);
	}
}
