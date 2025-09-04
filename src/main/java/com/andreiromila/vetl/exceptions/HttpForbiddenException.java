package com.andreiromila.vetl.exceptions;

import org.springframework.http.HttpStatus;

/**
 * An exception that represents an HTTP 403 Forbidden error.
 * Should be thrown when a user is authenticated but not authorized to perform an action.
 */
public class HttpForbiddenException extends HttpException {

    /**
     * Constructs a new HttpForbiddenException with a standard message.
     */
    public HttpForbiddenException() {
        super(HttpStatus.FORBIDDEN, "Access Denied. You do not have permission to perform this action.");
    }

    /**
     * Constructs a new HttpForbiddenException with a custom message.
     * @param message {@link String} The detail message.
     */
    public HttpForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}