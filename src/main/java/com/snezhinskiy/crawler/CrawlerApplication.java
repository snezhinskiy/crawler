package com.snezhinskiy.crawler;

import com.snezhinskiy.crawler.configuration.properties.ContentParserProperties;
import com.snezhinskiy.crawler.configuration.properties.CrawlerProperties;
import com.snezhinskiy.crawler.configuration.properties.LinksParserProperties;
import com.snezhinskiy.crawler.configuration.properties.PerformanceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties({
    LinksParserProperties.class, CrawlerProperties.class,
    LinksParserProperties.class, ContentParserProperties.class, PerformanceProperties.class
})
@EnableScheduling
@SpringBootApplication
public class CrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrawlerApplication.class, args);
    }

}
