package com.snezhinskiy.crawler.processing.parser.utils;

public class SafeNumbersConverter {
    public static Integer toInteger(Object object) {
        if (object instanceof Number casted) {
            return casted.intValue();
        } else if (object instanceof String casted) {
            return SafeStringConverter.toInteger(casted);
        }

        return null;
    }

    public static Double toDouble(Object object) {
        if (object instanceof Number casted) {
            return casted.doubleValue();
        } else if (object instanceof String casted) {
            return SafeStringConverter.toDouble(casted);
        }

        return null;
    }
}
