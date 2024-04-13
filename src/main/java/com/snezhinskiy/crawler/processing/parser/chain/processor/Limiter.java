package com.snezhinskiy.crawler.processing.parser.chain.processor;

import com.snezhinskiy.crawler.processing.model.FilterType;
import com.snezhinskiy.crawler.processing.model.ParseRule;
import com.snezhinskiy.crawler.processing.parser.chain.DocumentProcessingChainSegment;
import com.snezhinskiy.crawler.processing.parser.chain.ElementsProducer;
import com.snezhinskiy.crawler.processing.parser.chain.MapProducer;
import com.snezhinskiy.crawler.processing.parser.chain.StringProducer;
import com.snezhinskiy.crawler.processing.parser.utils.ChainAssertion;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class Limiter extends DocumentProcessingChainSegment implements StringProducer, ElementsProducer {

    private int limit;

    public Limiter(DocumentProcessingChainSegment parent, ParseRule rule) {
        super(parent, rule);
        ChainAssertion.firstArgumentIsNotEmptyString(rule);

        limit = Integer.parseInt(rule.getFirstArgument().toString());
    }

    @Override
    public Elements getElements(Document document) {
        final Elements input = ((ElementsProducer) parent).getElements(document);

        if (input == null) {
            return new Elements();
        }

        final List<Element> filteredElements = input.stream()
            .limit(limit)
            .collect(Collectors.toList());

        return new Elements(filteredElements);
    }

    @Override
    public String getString(Document document) {
        if (parent instanceof ElementsProducer) {
            return getElements(document).stream()
                .limit(limit)
                .map(el -> el.html())
                .collect(Collectors.joining());
        }

        return getStringList(document).stream()
            .limit(limit)
            .collect(Collectors.joining());
    }

    @Override
    public List<String> getStringList(Document document) {
        return ((StringProducer) parent).getStringList(document).stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
}
