package com.andreiromila.vetl.advice;

import com.andreiromila.vetl.responses.ErrorResponse;
import com.andreiromila.vetl.responses.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalControllerAdvice {

    /**
     * Exception handler for authentication failure
     *
     * @param request   {@link HttpServletRequest} The servlet request
     *
     * @return The http unauthorized response
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(final HttpServletRequest request) {

        final ErrorResponse responseBody = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid credentials.",
                request.getServletPath()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer realm=vortex.andreiromila.com")
                .body(responseBody);

    }

    /**
     * Exception handler for authorization failure
     *
     * @param exception {@link AuthorizationDeniedException} The authorization exception
     * @param request   {@link HttpServletRequest} The servlet request
     *
     * @return The http unauthorized response
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(final AuthorizationDeniedException exception, final HttpServletRequest request) {

        final ErrorResponse responseBody = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Access Denied.",
                request.getServletPath()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(responseBody);

    }

    /**
     * Validation errors handler for response transformation
     *
     * @param exception {@link MethodArgumentNotValidException} The exception
     * @param request   {@link HttpServletRequest} The request
     *
     * @return Response entity with formatted body
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {

        final List<ValidationError> validationMessages = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(ValidationError::new)
                .toList();

        // Create the data for the output
        final ErrorResponse errorResponse = new ErrorResponse(400, "The provided information is invalid.", validationMessages, request.getServletPath());

        // Return the response with bad request status
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
