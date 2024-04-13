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
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Filter extends DocumentProcessingChainSegment implements StringProducer, ElementsProducer, MapProducer {

    private FilterType type;
    private String filterArgument;

    public Filter(DocumentProcessingChainSegment parent, ParseRule rule) {
        super(parent, rule);
        ChainAssertion.firstArgumentIsNotEmptyString(rule);

        String filterName = ((String) rule.getFirstArgument()).trim().toUpperCase();
        type = FilterType.valueOf(filterName);

        filterArgument = (String) rule.getSecondArgument();
    }

    @Override
    public Elements getElements(Document document) {
        final Elements input = ((ElementsProducer) parent).getElements(document);

        if (input == null) {
            return new Elements();
        }

        final List<Element> filteredElements = input.stream()
            .filter(el -> test(el.html()))
            .collect(Collectors.toList());

        return new Elements(filteredElements);
    }

    @Override
    public String getString(Document document) {
        final String input = getElements(document).html();

        if (input == null || !test(input)) {
            return null;
        }

        return input;
    }

    @Override
    public List<String> getStringList(Document document) {
//        List<String> list = ((ElementsProducer) parent).getStringList(document);
        return ((ElementsProducer) parent).getStringList(document).stream()
            .filter(s -> test(s))
            .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getMap(Document document) {
        return getStringList(document).stream()
            .collect(Collectors.toMap(element -> element, element -> element));
    }

    private boolean test(String input) {
        boolean descision = false;
        switch (type) {
            case EMPTY:
                descision = !StringUtils.hasText(input);
                break;
            case NOT_EMPTY:
                descision = StringUtils.hasText(input);
                break;
            case CONTAINS:
                descision = StringUtils.hasText(input) && input.contains(filterArgument);
                break;
            case NOT_CONTAINS:
                descision = !StringUtils.hasText(input) || !input.contains(filterArgument);
                break;
            case EQUALS:
                descision = StringUtils.hasText(input) && filterArgument.equals(input.trim());
                break;
            case NOT_EQUALS:
                descision = !StringUtils.hasText(input) || !filterArgument.equals(input.trim());
                break;
            case STARTS_WITH:
                descision = StringUtils.hasText(input)
                    && input.startsWith(filterArgument);
                break;
            case NOT_STARTS_WITH:
                descision = !StringUtils.hasText(input)
                    || input.startsWith(filterArgument);
                break;
            case ENDS_WITH:
                descision = StringUtils.hasText(input)
                    && input.endsWith(filterArgument);
                break;
            case NOT_ENDS_WITH:
                descision = !StringUtils.hasText(input)
                    || input.endsWith(filterArgument);
                break;
            default:
                throw new RuntimeException("Unknown filter equation: "+rule.getFirstArgument());
        }

        return descision;
    }
}
