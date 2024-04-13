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

public class Concatenator extends DocumentProcessingChainSegment implements StringProducer {

    private String left;
    private String right;

    public Concatenator(DocumentProcessingChainSegment parent, ParseRule rule) {
        super(parent, rule);
        Assert.notNull(parent, "Wrong parser chain sequence");
        ChainAssertion.firstArgumentIsNotEmptyString(rule);

        left = (String) rule.getFirstArgument();
        right = (String) rule.getSecondArgument();
    }

    @Override
    public String getString(Document document) {
        String input =  ((StringProducer)parent).getString(document);
        return left + input.trim() + right;
    }

    @Override
    public List<String> getStringList(Document document) {
        List<String> input = ((StringProducer)parent).getStringList(document);

        return input.stream()
            .map(str -> left + str.trim() + right)
            .collect(Collectors.toList());
    }
}
