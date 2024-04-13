package com.snezhinskiy.crawler.processing.parser.chain.processor;

import com.snezhinskiy.crawler.processing.model.ParseRule;
import com.snezhinskiy.crawler.processing.parser.chain.DocumentProcessingChainSegment;
import com.snezhinskiy.crawler.processing.parser.chain.MapProducer;
import com.snezhinskiy.crawler.processing.parser.chain.StringProducer;
import com.snezhinskiy.crawler.processing.parser.utils.ChainAssertion;
import org.jsoup.nodes.Document;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Splitter extends DocumentProcessingChainSegment implements StringProducer, MapProducer {

    public static final String DEFAULT_DELIMITER = ";";
    private String delimiter;

    public Splitter(DocumentProcessingChainSegment parent, ParseRule rule) {
        super(parent, rule);
        Assert.notNull(parent, "Wrong parser chain sequence");
        ChainAssertion.firstArgumentIsNotEmptyString(rule);

        delimiter = StringUtils.hasText((String) rule.getFirstArgument())
            ? ((String) rule.getFirstArgument())
            : DEFAULT_DELIMITER;
    }

    @Override
    public String getString(Document document) {
        throw new RuntimeException("Unimplemented method");
    }

    @Override
    public List<String> getStringList(Document document) {
        if (parent instanceof Splitter) {
            return ((Splitter) parent).getStringList(document).stream()
                .filter(string -> StringUtils.hasText(string))
                .flatMap(string -> Arrays.asList(string.split(delimiter)).stream()
                    .filter(row -> rule.filterEmptyValues(row))
                )
                .collect(Collectors.toList());
        }

        String input = ((StringProducer)parent).getString(document);

        if (StringUtils.hasText(input)) {
            return Arrays.asList(input.split(delimiter)).stream()
                .filter(row -> rule.filterEmptyValues(row))
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> getMap(Document document) {
        return getStringList(document).stream()
            .collect(Collectors.toMap(element -> element, element -> element));
    }
}
