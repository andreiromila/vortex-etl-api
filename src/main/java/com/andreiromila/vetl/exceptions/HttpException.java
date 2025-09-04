package com.andreiromila.vetl.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base class for all custom HTTP-related exceptions.
 * Each exception carries an associated HTTP status and a message.
 */
@Getter
public class HttpException extends RuntimeException {

    private final HttpStatus status;

    /**
     * Constructs a new HttpException with the specified status and message.
     *
     * @param status  {@link HttpStatus} The HTTP status to be returned to the client.
     * @param message {@link String} The detail message.
     */
    protected HttpException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

}
