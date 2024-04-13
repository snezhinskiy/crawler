package com.snezhinskiy.crawler.api.advice;

import com.snezhinskiy.crawler.api.advice.exception.ApiValidationException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerValidationExceptionHandler {
    protected final MessageSource msgSource;

    @ExceptionHandler(ApiValidationException.class)
    public ResponseEntity<?> handle(ApiValidationException ex, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        Map<String, String> errors = new HashMap<>();

        if (StringUtils.hasText(ex.getErrorMessageCode())) {
            errors.put(
                ex.getFieldName(),
                msgSource.getMessage(ex.getErrorMessageCode(), ex.getArguments(), LocaleContextHolder.getLocale())
            );
        }

        return ResponseEntity.badRequest().body(Map.of("errors", errors));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<?> handleValidationErrors(
        HandlerMethodValidationException ex
    ) {
        Map<String, String> errors = ex.getAllValidationResults().stream()
            .collect(Collectors.toMap(
                validation -> validation.getMethodParameter().getParameterName(),
                validation -> validation.getResolvableErrors().get(0).getDefaultMessage()
            ));

        return ResponseEntity.badRequest().body(Map.of("errors", errors));
    }
}
