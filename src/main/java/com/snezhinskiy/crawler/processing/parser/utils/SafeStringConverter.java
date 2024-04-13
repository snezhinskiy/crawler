package com.snezhinskiy.crawler.processing.parser.utils;


import org.springframework.util.StringUtils;

public class SafeStringConverter {
    public static Integer toInteger(String input) {
        if (StringUtils.hasText(input)) {
            try {
                return Integer.valueOf(input);
            } catch (NumberFormatException e) {
                // do nothing
            }
        }

        return null;
    }

    public static Float toFloat(String input) {
        if (StringUtils.hasText(input)) {
            try {
                return Float.valueOf(input);
            } catch (NumberFormatException e) {
                // do nothing
            }
        }

        return null;
    }

    public static Double toDouble(String input) {
        if (StringUtils.hasText(input)) {
            try {
                return Double.valueOf(input);
            } catch (NumberFormatException e) {
                // do nothing
            }
        }

        return null;
    }
}
