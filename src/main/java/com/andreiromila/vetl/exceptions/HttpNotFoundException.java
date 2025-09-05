package com.andreiromila.vetl.exceptions;

import org.springframework.http.HttpStatus;

/**
 * An exception that represents an HTTP 404 Not Found
 * Should be thrown when the client's request is not found.
 */
public class HttpNotFoundException  extends HttpException {

    /**
     * Constructs a new HttpNotFoundException with a detail message.
     *
     * @param message {@link String} The error message.
     */
    public HttpNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

}
