package com.andreiromila.vetl.responses;

import org.springframework.validation.FieldError;

public record ValidationError(
        String field,
        String message,
        Object value
) {

    public ValidationError(final FieldError fieldError) {
        this(fieldError.getField(), fieldError.getDefaultMessage(), fieldError.getRejectedValue());
    }

}
