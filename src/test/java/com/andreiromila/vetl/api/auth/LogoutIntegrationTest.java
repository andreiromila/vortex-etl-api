package com.andreiromila.vetl.api.auth;

import com.andreiromila.vetl.AbstractIntegrationTest;
import com.andreiromila.vetl.responses.ErrorResponse;
import com.andreiromila.vetl.token.TokenWithExpiration;
import com.andreiromila.vetl.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.andreiromila.vetl.factories.AggregatesFactory.createUser;
import static org.assertj.core.api.Assertions.assertThat;

public class LogoutIntegrationTest extends AbstractIntegrationTest {

    HttpHeaders getAuthHeader(String token) {
        final HttpHeaders headers = new HttpHeaders();

        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        return headers;
    }

    @Test
    void logout_byGuest_returnsUnauthorized() {

        ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/logout", new HttpEntity<>(null), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(response.getBody())
                .isNotNull()
                .satisfies(body -> {
                    assertThat(body.code()).isEqualTo(401);
                    assertThat(body.message()).isEqualTo("Invalid credentials.");
                });

    }

    @Test
    void logout_withInvalidToken_returnsUnauthorized() {

        ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/logout", new HttpEntity<>(getAuthHeader("invalid.token")), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(response.getBody())
                .isNotNull()
                .satisfies(body -> {
                    assertThat(body.code()).isEqualTo(401);
                    assertThat(body.message()).isEqualTo("Invalid credentials.");
                });

    }

    @Test
    void logout_withValidToken_returnsNoContent() {

        // Given we have a user and a valid token
        final User john = userRepository.save(createUser("john"));
        final TokenWithExpiration tokenWithExpiration = tokenService.createToken(john.getUsername(), SPRING_BOOT_AGENT);

        // Used as Bearer token
        addAuthorizationHeader(tokenWithExpiration.token());

        ResponseEntity<Void> logoutResponse = http.postForEntity("/api/v1/logout", new HttpEntity<>(null), Void.class);

        // No content response
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Make sure the token is invalidated
        ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/logout", new HttpEntity<>(getAuthHeader("invalid.token")), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(response.getBody())
                .isNotNull()
                .satisfies(body -> {
                    assertThat(body.code()).isEqualTo(401);
                    assertThat(body.message()).isEqualTo("Invalid credentials.");
                });
    }
}
