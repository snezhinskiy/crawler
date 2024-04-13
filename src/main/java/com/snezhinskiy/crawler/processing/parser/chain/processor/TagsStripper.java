package com.snezhinskiy.crawler.processing.parser.chain.processor;

import com.snezhinskiy.crawler.processing.model.ParseRule;
import com.snezhinskiy.crawler.processing.parser.chain.DocumentProcessingChainSegment;
import com.snezhinskiy.crawler.processing.parser.chain.StringProducer;
import org.jsoup.nodes.Document;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class TagsStripper extends DocumentProcessingChainSegment implements StringProducer {

    public TagsStripper(DocumentProcessingChainSegment parent, ParseRule rule) {
        super(parent, rule);
        Assert.notNull(parent, "Wrong parser chain sequence");
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
                input = input.replaceAll("<[^>]+>", "");
            } catch (PatternSyntaxException exception) {}
        }

        return input;
    }
}
