package com.snezhinskiy.crawler.processing.parser.chain.processor;

import com.snezhinskiy.crawler.processing.model.ParseRule;
import com.snezhinskiy.crawler.processing.parser.chain.DocumentProcessingChainSegment;
import com.snezhinskiy.crawler.processing.parser.chain.MapProducer;
import com.snezhinskiy.crawler.processing.parser.chain.StringProducer;
import com.snezhinskiy.crawler.processing.parser.utils.ChainAssertion;
import org.jsoup.nodes.Document;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class Replacer extends DocumentProcessingChainSegment implements StringProducer, MapProducer {

    private String pattern;
    private String replacement;
    public Replacer(DocumentProcessingChainSegment parent, ParseRule rule) {
        super(parent, rule);
        Assert.notNull(parent, "Wrong parser chain sequence");
        ChainAssertion.firstArgumentIsNotEmptyString(rule);

        pattern = (String) rule.getFirstArgument();
        replacement = (String) rule.getSecondArgument();
    }

    @Override
    public String getString(Document document) {
        String input =  ((StringProducer)parent).getString(document);
        return process(input);
    }

    @Override
    public List<String> getStringList(Document document) {
        List<String> input = ((StringProducer)parent).getStringList(document);

        return input.stream()
            .map(item -> process(item))
            .filter(row -> rule.filterEmptyValues(row))
            .collect(Collectors.toList());
    }

    private String process(String input) {
        if (StringUtils.hasText(input)) {
            try {
                input = input.replaceFirst(pattern, replacement);
            } catch (PatternSyntaxException exception) {
                input = null;
            }
        }

        return input;
    }

    @Override
    public Map<String, Object> getMap(Document document) {
        return getStringList(document).stream()
            .collect(Collectors.toMap(element -> element, element -> element));
    }
}
