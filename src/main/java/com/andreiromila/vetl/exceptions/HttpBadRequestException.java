package com.andreiromila.vetl.exceptions;

import org.springframework.http.HttpStatus;

/**
 * An exception that represents an HTTP 400 Bad Request.
 * Should be thrown when the client's request is malformed or contains invalid data.
 */
public class HttpBadRequestException extends HttpException {

    /**
     * Constructs a new HttpBadRequestException with a detail message.
     *
     * @param message {@link String} The error message explaining why the request was bad.
     */
    public HttpBadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}