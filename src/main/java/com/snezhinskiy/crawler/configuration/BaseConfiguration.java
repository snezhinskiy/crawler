package com.snezhinskiy.crawler.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.snezhinskiy.crawler.processing.model.ParseRule;
import com.snezhinskiy.crawler.processing.parser.utils.ParseRuleDeserializer;
import com.snezhinskiy.crawler.processing.service.*;
import com.snezhinskiy.crawler.processing.service.impl.InMemoryContentParserJobService;
import com.snezhinskiy.crawler.processing.service.impl.InMemoryCrawlerJobService;
import com.snezhinskiy.crawler.processing.service.impl.InMemoryLinksParserJobService;
import com.snezhinskiy.crawler.processing.service.impl.InMemoryVisitedUrlsService;
import com.snezhinskiy.crawler.service.ProductService;
import com.snezhinskiy.crawler.service.impl.ContentWriteServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration(proxyBeanMethods = false)
public class BaseConfiguration {

    @Bean
    @ConditionalOnMissingBean(CrawlerJobService.class)
    public CrawlerJobService crawlerJobService(
        @Value("${defaults.in-memory.crawler-queue-capacity:5000}") int initialCapacity,
        @Value("${defaults.in-memory.crawler-idle-timeout:3000}") int idleTimeout
    ) {
        return new InMemoryCrawlerJobService(initialCapacity, idleTimeout);
    }

    @Bean
    @ConditionalOnMissingBean(LinksParserJobService.class)
    public LinksParserJobService urlsParserJobService(
        @Value("${defaults.in-memory.links-parser-queue-capacity:10}") int initialCapacity
    ) {
        return new InMemoryLinksParserJobService(initialCapacity);
    }

    @Bean
    @ConditionalOnMissingBean(ContentParserJobService.class)
    public ContentParserJobService contentParserJobService(
        @Value("${defaults.in-memory.content-parser-queue-capacity:10}") int initialCapacity
    ) {
        return new InMemoryContentParserJobService(initialCapacity);
    }

    @Bean
    @ConditionalOnMissingBean(VisitedUrlsService.class)
    public InMemoryVisitedUrlsService inMemoryVisitedUrlsService() {
        return new InMemoryVisitedUrlsService();
    }

    @Bean
    @ConditionalOnMissingBean(ContentWriteService.class)
    public ContentWriteService contentWriteService(ProductService productService) {
        return new ContentWriteServiceImpl(productService);
    }

    @Bean("objectMapperWithParseRuleModule")
    public ObjectMapper objectMapperWithParseRuleModule() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ParseRule.class, new ParseRuleDeserializer());
        mapper.registerModule(module);
        return mapper;
    }

    @Bean("virtualThreadExecutor")
    public ExecutorService virtualExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
