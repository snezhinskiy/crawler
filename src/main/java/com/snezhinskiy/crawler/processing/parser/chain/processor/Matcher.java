package com.snezhinskiy.crawler.processing.parser.chain.processor;

import com.snezhinskiy.crawler.processing.model.ParseRule;
import com.snezhinskiy.crawler.processing.parser.chain.DocumentProcessingChainSegment;
import com.snezhinskiy.crawler.processing.parser.chain.MapProducer;
import com.snezhinskiy.crawler.processing.parser.chain.StringProducer;
import com.snezhinskiy.crawler.processing.parser.utils.ChainAssertion;
import org.jsoup.nodes.Document;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class Matcher extends DocumentProcessingChainSegment implements StringProducer, MapProducer {
    private Pattern valuesPattern;
    private Pattern keysPattern;

    public Matcher(DocumentProcessingChainSegment parent, ParseRule rule) {
        super(parent, rule);
        Assert.notNull(parent, "Wrong parser chain sequence");
        ChainAssertion.firstArgumentIsNotEmptyString(rule);

        valuesPattern = Pattern.compile((String) rule.getFirstArgument());

        if (rule.getSecondArgument() instanceof String secondArgument
            && StringUtils.hasText(secondArgument)
        ) {
            keysPattern = Pattern.compile(secondArgument);
        }
    }

    @Override
    public String getString(Document document) {
        String input =  ((StringProducer)parent).getString(document);

        return match(input, valuesPattern);
    }

    @Override
    public List<String> getStringList(Document document) {
        List<String> input = ((StringProducer)parent).getStringList(document);

        return input.stream()
            .map(item -> match(item, valuesPattern))
            .filter(row -> rule.filterEmptyValues(row))
            .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getMap(Document document) {
        if (keysPattern == null)
            return Collections.emptyMap();

        List<String> input = ((StringProducer)parent).getStringList(document);

        Map<String, Object> result = new HashMap<>();

        for (String item: input) {
            String key = match(item, keysPattern);
            String value = match(item, valuesPattern);

            if (StringUtils.hasText(key) && StringUtils.hasText(value)) {
                result.put(key.trim(), value.trim());
            }
        }

        return result;
    }

    private String match(String input, Pattern pattern) {
        if (StringUtils.hasText(input)) {
            try {
                java.util.regex.Matcher matcher = pattern.matcher(input);

                if (matcher.find()) {
                    final String group = matcher.group();
                    return group;
                }
            } catch (PatternSyntaxException exception) {}
        }

        return null;
    }
}
