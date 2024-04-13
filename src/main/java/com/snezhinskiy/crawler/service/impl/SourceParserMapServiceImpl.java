package com.snezhinskiy.crawler.service.impl;

import com.snezhinskiy.crawler.domain.SourceParserMap;
import com.snezhinskiy.crawler.repository.SourceParserMapRepository;
import com.snezhinskiy.crawler.service.SourceParserMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SourceParserMapServiceImpl implements SourceParserMapService {
    private final SourceParserMapRepository repository;

    @Override
    public SourceParserMap save(SourceParserMap entity) {
        return repository.save(entity);
    }

    @Override
    public SourceParserMap getById(Long id) {
        return repository.findById(id).orElse(null);
    }
}
