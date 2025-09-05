package com.andreiromila.vetl.api.user;

import com.andreiromila.vetl.api.AbstractIntegrationTest;
import com.andreiromila.vetl.responses.ErrorResponse;
import com.andreiromila.vetl.user.User;
import com.andreiromila.vetl.utils.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class UserActivationIntegrationTest extends AbstractIntegrationTest {

    private final String VALID_USERNAME = "pending.user";
    private final String VALID_TOKEN = StringUtils.generateRandomString(64);
    private final String STRONG_PASSWORD = "StrongPassword1!";

    private User pendingUser;

    @BeforeEach
    void setupUser() {
        // Create a user in the state justo después de ser creado por un admin
        pendingUser = new User();
        pendingUser.setUsername(VALID_USERNAME);
        pendingUser.setEmail("pending.user@example.com");
        pendingUser.setFullName("Pending User");
        pendingUser.setEnabled(false);
        pendingUser.setEmailActivationCode(VALID_TOKEN);
        pendingUser.setModifiedAt(Instant.now());
        userRepository.save(pendingUser);
    }

    // --- Pruebas para el endpoint de VALIDACIÓN (GET) ---

    @Nested
    class GetValidationEndpoint {

        @Test
        void validateToken_withValidUsernameAndToken_returnsHttp204NoContent() {
            // When we validate a valid token for an existing unactivated user
            ResponseEntity<Void> response = http.getForEntity(
                    "/api/v1/users/{username}/activations/{token}",
                    Void.class, VALID_USERNAME, VALID_TOKEN
            );
            // Then the response is successful (204 No Content)
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }

        @Test
        void validateToken_withNonExistentUser_returnsHttp404NotFound() {
            // When we try to validate a token for a user that does not exist
            ResponseEntity<ErrorResponse> response = http.getForEntity(
                    "/api/v1/users/ghost.user/activations/{token}",
                    ErrorResponse.class, VALID_TOKEN
            );
            // Then the server responds with Not Found
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void validateToken_withInvalidToken_returnsHttp404NotFound() {
            // When we use an incorrect token
            ResponseEntity<ErrorResponse> response = http.getForEntity(
                    "/api/v1/users/{user}/activations/invalid-token-123",
                    ErrorResponse.class, VALID_USERNAME
            );
            // Then the server responds with Not Found to avoid leaking info
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void validateToken_thatIsExpired_returnsHttp410Gone() {
            // Given the user's token was created 25 hours ago
            pendingUser.setModifiedAt(Instant.now().minus(25, ChronoUnit.HOURS));
            userRepository.save(pendingUser);

            // When we try to validate it
            ResponseEntity<ErrorResponse> response = http.getForEntity(
                    "/api/v1/users/{user}/activations/{token}",
                    ErrorResponse.class, VALID_USERNAME, VALID_TOKEN
            );
            // Then the server responds with Gone
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
        }

        @Test
        void validateToken_thatHasAlreadyBeenUsed_returnsHttp404NotFound() {
            // Given the user has already activated their account
            pendingUser.setEmailValidatedAt(Instant.now());
            userRepository.save(pendingUser);

            // When we try to validate the same token again
            ResponseEntity<ErrorResponse> response = http.getForEntity(
                    "/api/v1/users/{user}/activations/{token}",
                    ErrorResponse.class, VALID_USERNAME, VALID_TOKEN
            );
            // Then the server responds with Not Found
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }


    // --- Pruebas para el endpoint de ACTIVACIÓN (POST) ---

    @Nested
    class PostActivationEndpoint {

        @Test
        void activateAccount_withValidData_returnsHttp204AndActivatesUser() {
            // Given a valid request body
            String body = """
                {
                    "token": "%s",
                    "password": "%s",
                    "passwordConfirmation": "%s"
                }
                """.formatted(VALID_TOKEN, STRONG_PASSWORD, STRONG_PASSWORD);

            // When the activation request is sent
            ResponseEntity<Void> response = http.postForEntity(
                    "/api/v1/users/{username}/activations",
                    new HttpEntity<>(body),
                    Void.class,
                    VALID_USERNAME
            );

            // Then the request is successful
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // And the user's state in the database is now 'activated'
            User activatedUser = userRepository.findByUsername(VALID_USERNAME).orElseThrow();
            assertThat(activatedUser.isEnabled()).isTrue();
            assertThat(activatedUser.getPassword()).isNotNull();
            assertThat(activatedUser.getEmailValidatedAt()).isNotNull();
            assertThat(activatedUser.getEmailActivationCode()).isNull(); // Token has been consumed
        }

        @Test
        void activateAccount_withMismatchedPasswords_returnsHttp400BadRequest() {
            // Given a body with passwords that do not match
            String body = """
                {
                    "token": "%s",
                    "password": "%s",
                    "passwordConfirmation": "differentPassword1!"
                }
                """.formatted(VALID_TOKEN, STRONG_PASSWORD);

            // When the activation request is sent
            ResponseEntity<ErrorResponse> response = http.postForEntity(
                    "/api/v1/users/{username}/activations",
                    new HttpEntity<>(body),
                    ErrorResponse.class,
                    VALID_USERNAME
            );

            // Then the server returns a Bad Request
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            // Y el mensaje viene de nuestra validación a nivel de clase
            assertThat(response.getBody().message()).contains("The provided information is invalid.");
            assertThat(response.getBody().validationErrors().getFirst().field()).isEqualTo("userActivationRequest");
        }

        @Test
        void activateAccount_withWeakPassword_returnsHttp400BadRequest() {
            // Given a body with a password that fails the @Pattern validation
            String body = """
                {
                    "token": "%s",
                    "password": "weak",
                    "passwordConfirmation": "weak"
                }
                """.formatted(VALID_TOKEN);

            ResponseEntity<ErrorResponse> response = http.postForEntity(
                    "/api/v1/users/{username}/activations",
                    new HttpEntity<>(body),
                    ErrorResponse.class,
                    VALID_USERNAME
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            // Verifica que el error de validación corresponde al campo 'password'
            assertThat(response.getBody().validationErrors().getFirst().field()).isEqualTo("password");
        }

        // El resto de casos de error (token inválido, expirado, etc.) ya están cubiertos
        // por el `GET`, pero podemos añadir uno para el POST por completitud.
        @Test
        void activateAccount_withExpiredToken_returnsHttp410Gone() {
            // Given the token is expired
            pendingUser.setModifiedAt(Instant.now().minus(25, ChronoUnit.HOURS));
            userRepository.save(pendingUser);

            String body = """
                {
                    "token": "%s",
                    "password": "%s",
                    "passwordConfirmation": "%s"
                }
                """.formatted(VALID_TOKEN, STRONG_PASSWORD, STRONG_PASSWORD);

            ResponseEntity<ErrorResponse> response = http.postForEntity(
                    "/api/v1/users/{username}/activations",
                    new HttpEntity<>(body),
                    ErrorResponse.class,
                    VALID_USERNAME
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
        }
    }
}