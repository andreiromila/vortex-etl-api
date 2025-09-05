package com.andreiromila.vetl.exceptions;

import org.springframework.http.HttpStatus;

/**
 * An exception that represents an HTTP 410 Gone status.
 * <p>
 * This should be thrown when a resource, such as an activation token,
 * has intentionally been made unavailable and is not expected to return.
 * It is a more specific and permanent version of a 404 Not Found.
 */
public class HttpGoneException extends HttpException {

    /**
     * Constructs a new HttpGoneException with a detail message.
     *
     * @param message {@link String} The error message explaining why the resource is gone.
     */
    public HttpGoneException(String message) {
        super(HttpStatus.GONE, message);
    }
}