package com.andreiromila.vetl.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * This authentication entry point is used to return a
 * 401 Unauthorized response to the user when there is
 * an invalid authentication (expired token, nonexistent
 * user, disabled user, etc.)
 */
@Component
public class UnauthorizedAuthenticatedEntryPoint implements AuthenticationEntryPoint {

    /**
     * The handler exception resolver bean
     */
    private final HandlerExceptionResolver exceptionResolver;

    /**
     * Unauthorized authentication entry point constructor
     *
     * @param exceptionResolver {@link HandlerExceptionResolver} The handler exception resolver bean
     */
    public UnauthorizedAuthenticatedEntryPoint(@Qualifier("handlerExceptionResolver") final HandlerExceptionResolver exceptionResolver) {
        this.exceptionResolver = exceptionResolver;
    }

    /**
     * Responds to the user with an 401 unauthenticated response, by default,
     * Spring will return a 403 Forbidden response if the user is not authenticated.
     *
     * @param request   {@link HttpServletRequest} The request
     * @param response  {@link HttpServletResponse} The response
     * @param exception {@link AuthenticationException} Authentication exception
     */
    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException exception) {
        // The WWW-Authenticate header is mandatory for 401 responses
        exceptionResolver.resolveException(request, response, null, exception);
    }
}
