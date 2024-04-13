package com.snezhinskiy.crawler.processing.parser.utils;

import com.snezhinskiy.crawler.processing.model.ParseRule;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;

public class ChainAssertion {
    public static void ruleTypeIsNotNull(ParseRule rule) {
        Assert.notNull(rule, "Rule must be not null");
        Assert.isTrue(rule.getType() != null, "Rule type must be not null");
    }

    public static void firstArgumentIsNotEmptyString(ParseRule rule) {
        firstArgumentIsDefined(rule);
        final List<Object> args = rule.getArguments();

        Assert.isInstanceOf(
            String.class, args.get(0), "First argument is expected to be a String :" + rule
        );

        Assert.isTrue(
            StringUtils.hasText((String) args.get(0)),
            "First argument is expected to be not empty String: " + rule
        );
    }

    public static void secondArgumentIsNotEmptyString(ParseRule rule) {
        secondArgumentIsDefined(rule);

        final List<Object> args = rule.getArguments();

        Assert.isInstanceOf(
            String.class, args.get(1), "Second argument is expected to be a String: " + rule
        );

        Assert.isTrue(
            StringUtils.hasText((String) args.get(1)),
            "Second argument is expected to be not empty String: " + rule
        );
    }

    public static void firstArgumentIsDefined(ParseRule rule) {
        final List<Object> args = rule.getArguments();

        Assert.isTrue(
            args != null && args.size() >= 1 && args.get(0) != null,
            "Rule first argument is required but not defined: " + rule
        );
    }

    public static void secondArgumentIsDefined(ParseRule rule) {
        final List<Object> args = rule.getArguments();

        Assert.isTrue(
            args != null && args.size() >= 2 && args.get(1) != null,
            "Rule second argument is required but not defined: " + rule
        );
    }
}
