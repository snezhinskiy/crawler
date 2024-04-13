package com.snezhinskiy.crawler.api.advice.exception;

public class ApiValidationException extends RuntimeException {

    protected String fieldName;
    protected String errorMessageCode;
    protected Object[] arguments;

    public ApiValidationException(String fieldName, String errorMessageCode) {
        this(fieldName, errorMessageCode, new Object[0]);
    }

    public ApiValidationException(String fieldName, String errorMessageCode, Object... arguments) {
        this.fieldName = fieldName;
        this.errorMessageCode = errorMessageCode;
        this.arguments = arguments;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getErrorMessageCode() {
        return errorMessageCode;
    }

    public Object[] getArguments() {
        return arguments;
    }
}
