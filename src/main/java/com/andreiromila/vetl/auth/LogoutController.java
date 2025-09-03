package com.andreiromila.vetl.auth;

import com.andreiromila.vetl.token.TokenService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest controller for token invalidation
 */
@RestController
@RequestMapping("/api/v1/logout")
public class LogoutController {

    /**
     * Token service bean
     */
    private final TokenService tokenService;

    /**
     * Logout controller constructor
     *
     * @param tokenService {@link TokenService} Token service bean
     */
    public LogoutController(final TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Invalidates the current token
     *
     * @param bearerToken {@link String} The authorization token to be invalidated
     * @return No content response entity
     */
    @PostMapping
    public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) final String bearerToken) {

        // Invalidate the current token
        tokenService.invalidate(bearerToken.substring(7));

        // Return the no content
        return ResponseEntity.noContent().build();

    }

}
