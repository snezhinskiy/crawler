package com.snezhinskiy.crawler.processing.parser.chain.processor;

import com.snezhinskiy.crawler.processing.model.ParseRule;
import com.snezhinskiy.crawler.processing.parser.chain.*;
import com.snezhinskiy.crawler.processing.parser.utils.ChainAssertion;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ElementsCombiner extends DocumentProcessingChainSegment implements MapProducer {

    private DocumentProcessingChain keySource;
    private DocumentProcessingChain valueSource;

    private FakeElementProducer fakeElementProducer;

    private ReentrantLock lock = new ReentrantLock();

    public ElementsCombiner(DocumentProcessingChainSegment parent, ParseRule rule) {
        super(parent, rule);
        Assert.notNull(parent, "Wrong parser chain sequence");
        ChainAssertion.firstArgumentIsDefined(rule);
        ChainAssertion.secondArgumentIsDefined(rule);

//        Assert.isTrue(
//            rule.getArguments().get(0) instanceof ElementsProducer,
//            "Expected first argument will be ElementsProducer: " + rule
//        );
//
//        Assert.isTrue(
//            rule.getArguments().get(1) instanceof ElementsProducer,
//            "Expected second argument will be ElementsProducer: " + rule
//        );

        fakeElementProducer = new FakeElementProducer(null);

        keySource = new DocumentProcessingChain((List<ParseRule>)rule.getArguments().get(0), fakeElementProducer);
        valueSource = new DocumentProcessingChain((List<ParseRule>)rule.getArguments().get(1), fakeElementProducer);

    }

    @Override
    public Map<String, Object> getMap(Document document) {
        Elements input = ((ElementsProducer)parent).getElements(document);

        if (CollectionUtils.isEmpty(input))
            return Collections.emptyMap();

        Map<String, Object> result = new HashMap<>();

        lock.lock();

        try {
            for (Element element: input) {
                fakeElementProducer.setInputElement(element);
                List<String> keys = (List<String>)keySource.toList(null);
                List<Object> values = (List<Object>)valueSource.toList(null);

                if (keys.size() > 0 && keys.size() == values.size()) {
                    for (int i = 0; i < keys.size(); i++) {
                        result.put(keys.get(i), values.get(i));
                    }
                }
            }

            fakeElementProducer.reset();
        } finally {
            lock.unlock();
        }

        return result;
    }

    private static class FakeElementProducer extends DocumentProcessingChainSegment implements ElementsProducer {
        private Element inputElement;

        public FakeElementProducer(DocumentProcessingChainSegment parent) {
            super(parent, null);
        }

        public void setInputElement(Element inputElement) {
            this.inputElement = inputElement;
        }

        public void reset() {
            inputElement = null;
        }

        @Override
        public Elements getElements(Document document) {
            return new Elements(Arrays.asList(inputElement));
        }

        @Override
        public String getString(Document document) {
            return inputElement.outerHtml();
        }

        @Override
        public List<String> getStringList(Document document) {
            return getElements(document).stream().map(el -> el.outerHtml()).collect(Collectors.toList());
//            if (parent instanceof StringProducer) {
//                return ((StringProducer) parent).getStringList(document);
//            }
//
//            throw new IllegalArgumentException("Parent expected to be StringSupplier");
        }
    }
}
