package com.snezhinskiy.crawler.processing.parser.chain.processor;

import com.snezhinskiy.crawler.processing.model.ParseRule;
import com.snezhinskiy.crawler.processing.parser.chain.DocumentProcessingChainSegment;
import com.snezhinskiy.crawler.processing.parser.chain.StringProducer;
import com.snezhinskiy.crawler.processing.parser.utils.ChainAssertion;
import org.jsoup.nodes.Document;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.StringJoiner;

public class Flattener extends DocumentProcessingChainSegment implements StringProducer {

    public static final String DEFAULT_DELIMITER = ";";
    private String type;
    private String delimiter;

    public Flattener(DocumentProcessingChainSegment parent, ParseRule rule) {
        super(parent, rule);
        Assert.notNull(parent, "Wrong parser chain sequence");
        ChainAssertion.firstArgumentIsNotEmptyString(rule);

        type = ((String) rule.getFirstArgument()).trim().toUpperCase();
        delimiter = StringUtils.hasText((String) rule.getSecondArgument())
            ? ((String) rule.getSecondArgument())
            : DEFAULT_DELIMITER;
    }

    @Override
    public String getString(Document document) {
        List<String> input =  ((StringProducer)parent).getStringList(document);

        if (!CollectionUtils.isEmpty(input)) {
            switch (type) {
                case "FIRST":
                    return input.getFirst();
                case "LAST":
                    return input.getLast();
                case "JOIN":
                    StringJoiner joiner = new StringJoiner(delimiter);
                    for (String element : input) {
                        joiner.add(element);
                    }
                    return joiner.toString();
            }
        }

        return null;
    }

    @Override
    public List<String> getStringList(Document document) {
        throw new RuntimeException("Unimplemented method");
    }
}
