package com.snezhinskiy.crawler.processing.parser.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.snezhinskiy.crawler.processing.model.ParseRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.snezhinskiy.crawler.processing.model.ParseRuleType.CSS_SELECTOR;
import static com.snezhinskiy.crawler.processing.model.ParseRuleType.TAG_SELECTOR;
import static org.junit.jupiter.api.Assertions.*;

class ParseRuleDeserializerTest {
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ParseRule.class, new ParseRuleDeserializer());
        mapper.registerModule(module);
    }

    @Test
    void deserialize_withMixedArguments_test() throws JsonProcessingException {
        String json = "{\"type\":\"CSS_SELECTOR\", \"arguments\": [" +
            "\"arg.0.0\", " +
            "{\"type\":\"TAG_SELECTOR\", \"arguments\": [\"arg.1.0\", \"arg.1.1\"]}" +
            "]}";

        ParseRule rule = mapper.readValue(json, ParseRule.class);

        assertEquals(CSS_SELECTOR, rule.getType());
        assertEquals("arg.0.0", rule.getArguments().get(0));

        ParseRule childRule = (ParseRule) rule.getArguments().get(1);
        assertEquals(TAG_SELECTOR, childRule.getType());
        assertEquals("arg.1.0", childRule.getArguments().get(0));
        assertEquals("arg.1.1", childRule.getArguments().get(1));
    }

    @Test
    void deserialize_withInnerArrayOfRules_test() throws JsonProcessingException {
        String json = "{\"type\":\"CSS_SELECTOR\", \"arguments\": [" +
            "[{\"type\":\"TAG_SELECTOR\", \"arguments\": [\"arg.0.0\", \"arg.0.1\"]}], " +
            "[{\"type\":\"TAG_SELECTOR\", \"arguments\": [\"arg.1.0\", \"arg.1.1\"]}] " +
            "]}";

        ParseRule rule = mapper.readValue(json, ParseRule.class);

        assertEquals(CSS_SELECTOR, rule.getType());
        List<Object> parentArguments = rule.getArguments();
        assertEquals(2, parentArguments.size());

        List<ParseRule> firstRulesList = (List<ParseRule>) parentArguments.get(0);
        assertEquals(TAG_SELECTOR, firstRulesList.get(0).getType());
        assertEquals("arg.0.0", firstRulesList.get(0).getArguments().get(0));
        assertEquals("arg.0.1", firstRulesList.get(0).getArguments().get(1));

        List<ParseRule> secondRulesList = (List<ParseRule>) parentArguments.get(1);
        assertEquals(TAG_SELECTOR, secondRulesList.get(0).getType());
        assertEquals("arg.1.0", secondRulesList.get(0).getArguments().get(0));
        assertEquals("arg.1.1", secondRulesList.get(0).getArguments().get(1));
    }
}