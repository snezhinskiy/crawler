package com.snezhinskiy.crawler.processing.parser.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.snezhinskiy.crawler.processing.model.ParseRule;
import com.snezhinskiy.crawler.processing.model.ParseRuleType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ParseRuleDeserializer extends JsonDeserializer<ParseRule> {

    @Override
    public ParseRule deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        return deserializeNode(node);
    }

    private ParseRule deserializeNode(JsonNode node) {
        if (node.has("type")
            && node.has("arguments")
            && node.get("arguments") instanceof ArrayNode argumentsNone
        ) {
            ParseRuleType type = ParseRuleType.valueOf(node.get("type").asText());

            if (type == null)
                return null;

            List<Object> arguments = StreamSupport.stream(argumentsNone.spliterator(), false)
                .map(childNode -> {

                    if (childNode.isArray()) {
                        if (childNode.size() == 0)
                            return Collections.emptyList();

                        List<Object> innerArguments = new ArrayList<>();

                        for (JsonNode _node: childNode) {
                            Object _argument = deserializeNode(_node);

                            if (_argument != null) {
                                innerArguments.add(_argument);
                            }
                        }

                        return innerArguments;
                    }

                    ParseRule rule = deserializeNode(childNode);
                    if (rule != null)
                        return rule;

                    return childNode.asText();
                })
                .collect(Collectors.toList());

            ParseRule rule = new ParseRule();
            rule.setType(type);
            rule.setArguments(arguments);
            return rule;

        }

        return null;
    }
}