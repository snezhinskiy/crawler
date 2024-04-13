package com.snezhinskiy.crawler.processing.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@ToString
public class ParseRule {
    private ParseRuleType type;
    private List<Object> arguments;
    private boolean acceptEmptyValues;

    public ParseRule() {}

    public ParseRule(ParseRuleType type, Object... arguments) {
        this.type = type;
        this.arguments = Arrays.asList(arguments);
        this.acceptEmptyValues = false;
    }

    /**
     * @todo Move it out of there
     */
    public boolean filterEmptyValues(String input) {
        return acceptEmptyValues ? true : StringUtils.hasText(input);
    }


    public Object getFirstArgument(){
        return arguments != null && arguments.size() > 0 ? arguments.get(0) : "";
    }

    public Object getSecondArgument(){
        return arguments != null && arguments.size() > 1 ? arguments.get(1) : "";
    }
}
