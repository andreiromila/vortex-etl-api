package com.andreiromila.vetl.exceptions;

import org.springframework.http.HttpStatus;

/**
 * An exception that represents an HTTP 500 Internal Server Error
 * Should be thrown when there is a serious problem with the system
 */
public class HttpInternalServerErrorException extends HttpException {

    /**
     * Constructs a new HttpInternalServerErrorException with a detail message.
     *
     * @param message {@link String} The error message.
     */
    public HttpInternalServerErrorException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

}
