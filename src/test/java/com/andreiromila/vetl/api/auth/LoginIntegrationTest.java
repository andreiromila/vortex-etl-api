package com.andreiromila.vetl.api.auth;

import com.andreiromila.vetl.AbstractIntegrationTest;
import com.andreiromila.vetl.auth.LoginResponse;
import com.andreiromila.vetl.mail.EmailService;
import com.andreiromila.vetl.responses.ErrorResponse;
import com.andreiromila.vetl.user.User;
import com.andreiromila.vetl.user.UserService;
import com.andreiromila.vetl.user.web.UserCreateRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;

public class LoginIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @MockitoBean
    EmailService emailService;

    @Test
    void login_usingNullUsername_returnsUnauthorized() {

        String body = """
                {
                    "username": null,
                    "password": "Pa$$w0rd!"
                }
                """;

        ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/login", new HttpEntity<>(body), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().get(WWW_AUTHENTICATE)).hasSize(1);
        assertThat(response.getHeaders().get(WWW_AUTHENTICATE)).contains("Bearer realm=vortex.andreiromila.com");

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(401);
        assertThat(response.getBody().message()).isEqualTo("Invalid credentials.");
    }


    @Test
    void login_usingNullPassword_returnsUnauthorized() {

        String body = """
                {
                    "username": "john",
                    "password": null
                }
                """;

        ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/login", new HttpEntity<>(body), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().get(WWW_AUTHENTICATE)).hasSize(1);
        assertThat(response.getHeaders().get(WWW_AUTHENTICATE)).contains("Bearer realm=vortex.andreiromila.com");

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(401);
        assertThat(response.getBody().message()).isEqualTo("Invalid credentials.");
    }

    @Test
    void login_usingInvalidCredentials_returnsUnauthorized() {

        String body = """
                {
                    "username": "nonexistent",
                    "password": "Pa$$w0rd!"
                }
                """;

        ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/login", new HttpEntity<>(body), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().get(WWW_AUTHENTICATE)).hasSize(1);
        assertThat(response.getHeaders().get(WWW_AUTHENTICATE)).contains("Bearer realm=vortex.andreiromila.com");

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(401);
        assertThat(response.getBody().message()).isEqualTo("Invalid credentials.");
    }

    @Test
    void login_withValidCredentials_returnsOkResponse() {

        // Given we have a user
        User john = userService.createUser(
                getUserCreateRequest()
        );

        // Enable and activate the user ...
        john.setEnabled(true);
        john.setEmailValidatedAt(Instant.now());
        john.setPassword(passwordEncoder.encode("Pa$$w0rd!"));

        userRepository.save(john);

        // When John tries to authenticate with the system
        String body = """
                {
                    "username": "john",
                    "password": "Pa$$w0rd!"
                }
                """;

        ResponseEntity<LoginResponse> response = http.postForEntity("/api/v1/login", new HttpEntity<>(body), LoginResponse.class);

        // Should get a 200 Ok response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // And a token with an expiration timestamp greater than now
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isNotBlank();
        assertThat(response.getBody().expiresAt()).isAfter(ZonedDateTime.now());

    }

    private static @NotNull UserCreateRequest getUserCreateRequest() {
        return new UserCreateRequest("John Doe.", "john", "john@example.com", Set.of(3L));
    }

    @Test
    void login_withDisabledUser_returnsUnauthorized() {

        // Given we have an unverified user
        User john = userService.createUser(
                getUserCreateRequest()
        );

        john.setEnabled(false);
        userRepository.save(john);

        // When John tries to authenticate with the system
        String body = """
                {
                    "username": "john",
                    "password": "Pa$$w0rd!"
                }
                """;

        ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/login", new HttpEntity<>(body), ErrorResponse.class);

        // Should get a 401 Ok response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().get(WWW_AUTHENTICATE)).hasSize(1);
        assertThat(response.getHeaders().get(WWW_AUTHENTICATE)).contains("Bearer realm=vortex.andreiromila.com");

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(401);
        assertThat(response.getBody().message()).isEqualTo("Invalid credentials.");

    }

}
