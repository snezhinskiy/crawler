package com.snezhinskiy.crawler.processing.parser.chain.processor;

import com.snezhinskiy.crawler.processing.model.ParseRule;
import com.snezhinskiy.crawler.processing.parser.chain.DocumentProcessingChainSegment;
import com.snezhinskiy.crawler.processing.parser.chain.ElementsProducer;
import com.snezhinskiy.crawler.processing.parser.chain.MapProducer;
import com.snezhinskiy.crawler.processing.parser.chain.StringProducer;
import com.snezhinskiy.crawler.processing.parser.utils.ChainAssertion;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CssSelector extends DocumentProcessingChainSegment implements ElementsProducer, StringProducer, MapProducer {

    public static final String TEXT = "TEXT";
    public static final String HTML = "HTML";
    public static final String OUTER_HTML = "OUTER_HTML";

    private Function<Element, String> elementRenderMethod = e -> e.html().trim();
    private Function<Elements, String> elementListRenderMethod = e -> e.html().trim();
    private String selector;

    public CssSelector(ParseRule rule) {
        this(null, rule);
    }

    public CssSelector(DocumentProcessingChainSegment parent, ParseRule rule) {
        super(parent, rule);
        ChainAssertion.firstArgumentIsNotEmptyString(rule);

        this.selector = (String) rule.getFirstArgument();

        if (rule.getSecondArgument() instanceof String renderMethodName
            && StringUtils.hasText(renderMethodName)
        ) {
            switch (renderMethodName.trim().toUpperCase()) {
                case TEXT:
                    elementRenderMethod = e -> e.text().trim();
                    elementListRenderMethod = e -> e.text().trim();
                    break;
                case HTML:
                    elementRenderMethod = e -> e.html().trim();
                    elementListRenderMethod = e -> e.html().trim();
                    break;
                case OUTER_HTML:
                    elementRenderMethod = e -> e.outerHtml().trim();
                    elementListRenderMethod = e -> e.outerHtml().trim();
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected render method name");
            }
        }
    }

    @Override
    public Elements getElements(Document document) {
        if (parent == null) {
            return document.select(selector);
        }

        if (parent instanceof ElementsProducer parentSelector) {
            return parentSelector.getElements(document)
                .select(selector);
        }

        throw new IllegalArgumentException("CssSelector must be first or must located only after ElementsProducer");
    }

    @Override
    public String getString(Document document) {
        List<String> list = getStringList(document);

        if (!CollectionUtils.isEmpty(list)) {
            StringBuilder stringBuilder = new StringBuilder();

            for (String row : list) {
                stringBuilder.append(row);
            }
            return stringBuilder.toString();
        }

        return null;
    }

    @Override
    public List<String> getStringList(Document document) {
        if (parent == null || parent instanceof ElementsProducer) {
            return getElements(document).stream()
                .map(el -> elementRenderMethod.apply(el))
                .filter(str -> rule.filterEmptyValues(str))
                .collect(Collectors.toList());
        }

        return ((StringProducer) parent).getStringList(document);
    }

    @Override
    public Map<String, Object> getMap(Document document) {
        return getStringList(document).stream()
            .collect(Collectors.toMap(element -> element, element -> element));
    }
}
