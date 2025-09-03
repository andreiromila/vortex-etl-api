package com.andreiromila.vetl.responses;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Contains the basic structure for all the errors
 *
 * @param timestamp        {@link ZonedDateTime} The current timestamp
 * @param code             {@link Integer} The error code
 * @param message          {@link String} The error message
 * @param validationErrors {@link Map} The validation errors maps (if it's a validation error)
 * @param path             {@link String} The path that produced the error
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        ZonedDateTime timestamp,
        Integer code,
        String message,
        List<ValidationError> validationErrors,
        String path
) {

    public ErrorResponse(Integer code, String message, String path) {
        this(ZonedDateTime.now(), code, message, null, path);
    }

    public ErrorResponse(Integer code, String message, List<ValidationError> validationErrors, String path) {
        this(ZonedDateTime.now(), code, message, validationErrors, path);
    }

}
