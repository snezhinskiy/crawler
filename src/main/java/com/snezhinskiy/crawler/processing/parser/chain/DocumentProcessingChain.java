package com.snezhinskiy.crawler.processing.parser.chain;

import com.snezhinskiy.crawler.processing.model.ParseRule;
import com.snezhinskiy.crawler.processing.parser.chain.processor.*;
import com.snezhinskiy.crawler.processing.parser.utils.ChainAssertion;
import com.snezhinskiy.crawler.processing.parser.utils.SafeStringConverter;
import org.jsoup.nodes.Document;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DocumentProcessingChain {
    private static DocumentProcessingChain emptyChain = new DocumentProcessingChain();
    private DocumentProcessingChainSegment lastSegment;

    public DocumentProcessingChain(){}

    public DocumentProcessingChain(List<ParseRule> rules) {
        this(rules, null);
    }

    public DocumentProcessingChain(List<ParseRule> rules, DocumentProcessingChainSegment parent) {

        if (!CollectionUtils.isEmpty(rules)) {
            for (ParseRule rule : rules) {
                parent = createSegment(parent, rule);
            }
        }

        lastSegment = parent;
    }

    public static DocumentProcessingChain emptyChain() {
        return emptyChain;
    }

    private DocumentProcessingChainSegment createSegment(DocumentProcessingChainSegment parent, ParseRule rule) {
        ChainAssertion.ruleTypeIsNotNull(rule);

        switch (rule.getType()) {
            case CSS_SELECTOR:
                return new CssSelector(parent, rule);
            case TAG_SELECTOR:
                return new TagSelector(parent, rule);
            case FILTER:
                return new Filter(parent, rule);
            case LIMITER:
                return new Limiter(parent, rule);
            case MATCHER:
                return new Matcher(parent, rule);
            case REPLACER:
                return new Replacer(parent, rule);
            case TRIMMER:
                return new Trimmer(parent, rule);
            case TAGS_STRIPPER:
                return new TagsStripper(parent, rule);
            case CONCATENATOR:
                return new Concatenator(parent, rule);
            case SPLITTER:
                return new Splitter(parent, rule);
            case FLATTENER:
                return new Flattener(parent, rule);
            case JSON_TO_MAP_PARSER:
                return new JsonToMapParser(parent, rule);
            case MAP_FLATTENER:
                return new MapFlattener(parent, rule);
            case ELEMENTS_COMBINER:
                return new ElementsCombiner(parent, rule);
            default:
                throw new IllegalArgumentException("Unknown rule type");
        }
    }

    public String toString(Document document) {
        if (lastSegment instanceof StringProducer stringProducer) {
            return stringProducer.getString(document);
        } else if (lastSegment instanceof ElementsProducer elementsProducer) {
            return elementsProducer.getElements(document).html();
        }

        return null;
    }

    public Integer toInteger(Document document) {
        return SafeStringConverter.toInteger(toString(document));
    }

    public Float toFloat(Document document) {
        return SafeStringConverter.toFloat(toString(document));
    }

    public Double toDouble(Document document) {
        return SafeStringConverter.toDouble(toString(document));
    }

    public List<?> toList(Document document) {
        if (lastSegment instanceof StringProducer stringProducer) {
            return stringProducer.getStringList(document);
        } else if (lastSegment instanceof ElementsProducer elementsProducer) {
            return elementsProducer.getElements(document).stream()
                .map(e -> e.html().trim())
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public Map<String, Object> toMap(Document document) {
        if (lastSegment instanceof MapProducer mapProducer) {
            return mapProducer.getMap(document);
        }

        return Collections.emptyMap();
    }
}
