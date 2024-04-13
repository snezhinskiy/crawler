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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TagSelector extends DocumentProcessingChainSegment implements ElementsProducer, StringProducer, MapProducer {

    public TagSelector(ParseRule rule) {
        this(null, rule);
    }

    public TagSelector(DocumentProcessingChainSegment parent, ParseRule rule) {
        super(parent, rule);
        ChainAssertion.firstArgumentIsNotEmptyString(rule);
    }

    @Override
    public Elements getElements(Document document) {
        if (parent instanceof ElementsProducer parentElementsProducer) {
            List<Element> elementsList = parentElementsProducer.getElements(document).stream()
                .filter(el -> el != null && rule.getFirstArgument().equals(el.tagName()))
                .collect(Collectors.toList());

            return new Elements(elementsList);
        }

        return document.getElementsByTag((String) rule.getFirstArgument());
    }

    @Override
    public String getString(Document document) {
        return getElements(document).outerHtml();
    }

    @Override
    public List<String> getStringList(Document document) {
        return getElements(document).stream()
            .map(el -> el.outerHtml().trim())
            .filter(str -> rule.filterEmptyValues(str))
            .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getMap(Document document) {
        return getStringList(document).stream()
            .collect(Collectors.toMap(element -> element, element -> element));
    }
}
