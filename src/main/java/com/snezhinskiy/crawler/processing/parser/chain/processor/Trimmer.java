package com.snezhinskiy.crawler.processing.parser.chain.processor;

import com.snezhinskiy.crawler.processing.model.ParseRule;
import com.snezhinskiy.crawler.processing.parser.chain.DocumentProcessingChainSegment;
import com.snezhinskiy.crawler.processing.parser.chain.StringProducer;
import com.snezhinskiy.crawler.processing.parser.utils.ChainAssertion;
import org.jsoup.nodes.Document;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class Trimmer extends DocumentProcessingChainSegment implements StringProducer {
    private Character trmChar;
    public Trimmer(DocumentProcessingChainSegment parent, ParseRule rule) {
        super(parent, rule);
        Assert.notNull(parent, "Wrong parser chain sequence");
        ChainAssertion.firstArgumentIsNotEmptyString(rule);

        String symbol = rule.getFirstArgument().toString().trim();

        Assert.isTrue(symbol.length() == 1, "Expected exactly one symbol");

        trmChar = symbol.charAt(0);
    }

    @Override
    public String getString(Document document) {
        return trim(
            ((StringProducer)parent).getString(document)
        );
    }

    @Override
    public List<String> getStringList(Document document) {
        List<String> input = ((StringProducer)parent).getStringList(document);

        return input.stream()
            .map(item -> trim(item))
            .filter(row -> rule.filterEmptyValues(row))
            .collect(Collectors.toList());
    }

    private String trim(String input) {
        if (StringUtils.hasText(input)) {
            input = input.trim();
            int begin = 0;
            int end = input.length() - 1;

            while (begin < end) {
                if (input.charAt(begin) == trmChar) {
                    begin++;
                } else if (input.charAt(end) == trmChar) {
                    end--;
                } else {
                    break;
                }
            }

            return input.substring(begin, end + 1);
        }

        return input;
    }
}
