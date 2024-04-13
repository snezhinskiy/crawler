package com.snezhinskiy.crawler.processing.parser.chain.processor;

import com.snezhinskiy.crawler.processing.model.ParseRule;
import com.snezhinskiy.crawler.processing.parser.chain.DocumentProcessingChainSegment;
import com.snezhinskiy.crawler.processing.parser.chain.MapProducer;
import com.snezhinskiy.crawler.processing.parser.chain.StringProducer;
import com.snezhinskiy.crawler.processing.parser.utils.ChainAssertion;
import org.jsoup.nodes.Document;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class MapFlattener extends DocumentProcessingChainSegment implements StringProducer {

    public static final String DELIMITER = ";";
    public static final String KEY = "KEYS";
    public static final String VALUE = "VALUES";

    public static boolean fromkeys = false;

    public MapFlattener(DocumentProcessingChainSegment parent, ParseRule rule) {
        super(parent, rule);
        ChainAssertion.firstArgumentIsNotEmptyString(rule);
        Assert.notNull(parent, "Wrong parser chain sequence");
        Assert.isTrue(parent instanceof JsonToMapParser, "Expected parent MapProducer");

        switch (((String)rule.getFirstArgument()).trim().toUpperCase()) {
            case KEY:
                fromkeys = true;
                break;
            case VALUE:
                fromkeys = false;
                break;
            default:
                throw new IllegalArgumentException("Unexpected value of first argument: "+rule);
        }
    }

    @Override
    public String getString(Document document) {
        List<String> input =  getStringList(document);

        if (CollectionUtils.isEmpty(input))
            return null;

        StringJoiner joiner = new StringJoiner(DELIMITER);
        for (String item : input) {
            joiner.add(item);
        }
        return joiner.toString();
    }

    @Override
    public List<String> getStringList(Document document) {
        Map<String, Object> input =  ((MapProducer)parent).getMap(document);

        if (CollectionUtils.isEmpty(input))
            return Collections.emptyList();

        return input.entrySet().stream()
            .map(entry -> entryToString(entry))
            .filter(row -> rule.filterEmptyValues(row))
            .collect(Collectors.toList());
    }

    private String entryToString(Map.Entry<String, Object> entry) {
        if (fromkeys) {
            return entry.getKey();
        } else if (entry.getValue() != null) {
            return String.valueOf(entry.getValue());
        }

        return null;
    }

}
