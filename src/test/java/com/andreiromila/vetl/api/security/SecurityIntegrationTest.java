package com.andreiromila.vetl.api.security;

import com.andreiromila.vetl.api.AbstractIntegrationTest;
import com.andreiromila.vetl.responses.ErrorResponse;
import com.andreiromila.vetl.token.TokenProperties;
import com.andreiromila.vetl.user.User;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    TokenProperties properties;

    String buildJwt(final String username, final Date expiration) {
        return buildJwt(UUID.randomUUID().toString(), username, expiration);
    }

    String buildJwt(final String uuid, final String username, final Date expiration) {
        return Jwts.builder()
                .claims(new HashMap<>())
                .id(uuid)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expiration)
                .signWith(properties.secretKey())
                .compact();
    }

    @Test
    void accessProtectedEndpoint_withoutValidBearerToken_returnsUnauthorizedResponse() {

        // When a guest tries to access the list users endpoint
        // When John accesses the user-list endpoint
        final ResponseEntity<ErrorResponse> response = http.exchange(
                "/api/v1/users", HttpMethod.GET, new HttpEntity<>(""), new ParameterizedTypeReference<>() { }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(401);
        assertThat(response.getBody().message()).isEqualTo("Invalid credentials.");
    }

    @Test
    void accessProtectedEndpoint_withInvalidAuthorizationHeader_returnsUnauthorized() {

        // When a guest tries to access the list users endpoint
        // When John accesses the user-list endpoint
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "InvalidTokenFormatWithoutBearerPrefix");

        final ResponseEntity<ErrorResponse> response = http.exchange(
                "/api/v1/users", HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<>() { }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(401);
        assertThat(response.getBody().message()).isEqualTo("Invalid credentials.");
    }

    @Test
    void accessProtectedEndpoint_withDisabledUser_returnsErrorMessage() {

        // Given we have a disabled user
        final User authenticated = loginViewer("disabled.user");
        authenticated.setEnabled(false);
        userRepository.save(authenticated);

        // When a guest tries to access the list users endpoint
        // When John accesses the user-list endpoint
        final ResponseEntity<ErrorResponse> response = http.exchange(
                "/api/v1/users", HttpMethod.GET, new HttpEntity<>(""), new ParameterizedTypeReference<>() { }
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(401);
        assertThat(response.getBody().message()).isEqualTo("Invalid credentials.");
    }

    @Test
    void accessProtectedEndpoint_withExpiredBearerToken_returnsUnauthorizedResponse() {

        // Given we have an expired token,
        // Get the expiration date
        final Date expiration = new Date(System.currentTimeMillis() - 60_000);

        // Create the token
        final String expiredToken = buildJwt("john", expiration);

        // Login with and expired token
        addAuthorizationHeader(expiredToken);

        // When a guest tries to access the list users endpoint
        // When John accesses the user-list endpoint
        final ResponseEntity<ErrorResponse> response = http.exchange(
                "/api/v1/users", HttpMethod.GET, new HttpEntity<>(""), new ParameterizedTypeReference<>() { }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
