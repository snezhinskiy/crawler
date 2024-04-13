package com.snezhinskiy.crawler.processing.parser.chain.processor;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snezhinskiy.crawler.processing.model.ParseRule;
import com.snezhinskiy.crawler.processing.parser.chain.DocumentProcessingChainSegment;
import com.snezhinskiy.crawler.processing.parser.chain.MapProducer;
import com.snezhinskiy.crawler.processing.parser.chain.StringProducer;
import com.snezhinskiy.crawler.processing.parser.utils.ChainAssertion;
import org.jsoup.nodes.Document;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonToMapParser extends DocumentProcessingChainSegment implements MapProducer {
    private ObjectMapper mapper;
    private TypeReference typeRef = new TypeReference<Map<String, Object>>(){};
    private boolean fromList;
    private ParsePathSettings pathSettings;

    public JsonToMapParser(DocumentProcessingChainSegment parent, ParseRule rule) {
        super(parent, rule);
        Assert.notNull(parent, "Wrong parser chain sequence");
        Assert.isTrue(parent instanceof StringProducer, "Expected that parent is StringProducer");
        ChainAssertion.firstArgumentIsNotEmptyString(rule);

        pathSettings = new ParsePathSettings((String) rule.getFirstArgument());

        String secondArgument = ((String) rule.getSecondArgument());
        fromList = "FROM_LIST".equals(secondArgument.trim().toUpperCase());

        mapper = new ObjectMapper();
        mapper.enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES.mappedFeature());
        mapper.enable(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature());
        mapper.enable(JsonReadFeature.ALLOW_MISSING_VALUES.mappedFeature());
    }

    public Map<String, Object> getMap(Document document) {
        if (rule.getSecondArgument() != null && fromList) {
            final List<String> input = ((StringProducer)parent).getStringList(document);
            Map<String, Object> result = new HashMap<>();

            input.forEach(row -> {
                Map<String, Object> sub = process(row);

                if (!CollectionUtils.isEmpty(sub)) {
                    result.putAll(sub);
                }
            });
        }

        final String input = ((StringProducer)parent).getString(document);
        return  process(input);
    }

    private Map<String, Object> process(String input) {
        if (StringUtils.hasText(input)) {
            try {
                Map<String, Object> parsedMap = (Map<String, Object>) mapper.readValue(input, typeRef);

                Map<String, Object> result = new HashMap<>();

                for (String key: parsedMap.keySet()) {
                    Object value = getValueByPaths((Map<String, Object>)parsedMap.get(key), pathSettings.getValuePaths(), 0);

                    if (pathSettings.hasKeyPaths()) {
                        key = String.valueOf(getValueByPaths((Map<String, Object>)parsedMap.get(key), pathSettings.getKeyPaths(), 0));
                    }

                    result.put(key, value);
                }

                return result;
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        return Collections.emptyMap();
    }

    private Object getValueByPaths(Map<String, ?> map, String[] path, int index) {
        if (index == path.length - 1) {
            // Достигли конечного ключа, возвращаем значение
            return map.get(path[index]);
        } else {
            // Продолжаем рекурсивный вызов для вложенной Map
            Object nextMap = map.get(path[index]);
            if (nextMap instanceof Map) {
                return getValueByPaths((Map<String, ?>) nextMap, path, index + 1);
            } else {
                return "";
            }
        }
    }

    public static class ParsePathSettings {
        private String[] keyPaths;
        private String[] valuePaths;

        public ParsePathSettings(String settings) {
            String keyPathStr = "";
            String valuePathStr = "";

            String[] parts = settings.split(",");

            for (int i = 0; i < parts.length; i++) {
                String[] subParts = parts[i].split(":");

                if (subParts.length == 2) {
                    if ("key".equals(subParts[0].toLowerCase())) {
                        Assert.isTrue(keyPathStr != null, "Key path defined more then ONE time?");
                        keyPathStr = subParts[1];
                    } else if ("value".equals(subParts[0].toLowerCase())) {
                        Assert.isTrue(valuePathStr != null, "Value path defined more then ONE time?");
                        valuePathStr = subParts[1];
                    } else {
                        throw new IllegalArgumentException("Unknown keyword");
                    }
                } else if (i == 0) {
                    Assert.isTrue(keyPathStr != null, "Key path defined more then ONE time?");
                    keyPathStr = parts[i];
                } else {
                    Assert.isTrue(valuePathStr != null, "Value path defined more then ONE time?");
                    valuePathStr = parts[i];
                }
            }

            if (StringUtils.hasText(keyPathStr)) {
                keyPaths = keyPathStr.split("\\.");
            }

            if (StringUtils.hasText(valuePathStr)) {
                valuePaths = valuePathStr.split("\\.");
            }
        }

        public boolean hasKeyPaths() {
            return keyPaths != null;
        }

        public boolean hasValuePaths() {
            return valuePaths != null;
        }

        public String[] getKeyPaths() {
            return keyPaths;
        }

        public String[] getValuePaths() {
            return valuePaths;
        }
    }
}
