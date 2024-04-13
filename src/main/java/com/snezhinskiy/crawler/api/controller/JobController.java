package com.snezhinskiy.crawler.api.controller;

import com.snezhinskiy.crawler.api.advice.exception.ApiValidationException;
import com.snezhinskiy.crawler.api.dto.ExpressJobRequest;
import com.snezhinskiy.crawler.api.dto.ProductDataResponseItem;
import com.snezhinskiy.crawler.api.mapper.ProductDataResponseMapper;
import com.snezhinskiy.crawler.domain.SourceParserMap;
import com.snezhinskiy.crawler.processing.service.impl.JobRunningService;
import com.snezhinskiy.crawler.service.SourceParserMapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Log4j2
@RestController
@RequestMapping("/api/v1/job")
@RequiredArgsConstructor
public class JobController {
    private final SourceParserMapService mapService;
    private final JobRunningService jobRunningService;
    private final ProductDataResponseMapper responseMapper;

    @PostMapping(path = "/express-execute", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<List<ProductDataResponseItem>> expressExecute(
        @RequestBody @Valid ExpressJobRequest request
    ) {
        SourceParserMap parserMap = mapService.getById(request.getMapId());

        if (parserMap == null) {
            throw new ApiValidationException("mapId", "JobController.errors.parserMap.notFound");
        }

        return jobRunningService.runAsync(parserMap, request.getUrl())
            .thenApply((list) ->
                list.stream()
                    .map(item -> responseMapper.map(item))
                    .collect(Collectors.toList())
            );
    }
}
