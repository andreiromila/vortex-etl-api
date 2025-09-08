package com.andreiromila.vetl.api.user;

import com.andreiromila.vetl.AbstractIntegrationTest;
import com.andreiromila.vetl.responses.ErrorResponse;
import com.andreiromila.vetl.user.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class UserPasswordChangeIntegrationTest extends AbstractIntegrationTest {

    private final String API_URL = "/api/v1/users/{username}/password";
    private final String STRONG_PASSWORD_NEW = "NewPassword123!";

    @Test
    void changePassword_asSelf_withCorrectCurrentPassword_succeeds() {
        // Given un usuario está logueado con una contraseña conocida
        User user = loginWithPassword("jane.doe", "OldPassword123!");

        String body = """
            {
                "currentPassword": "OldPassword123!",
                "password": "%s",
                "passwordConfirmation": "%s"
            }
            """.formatted(STRONG_PASSWORD_NEW, STRONG_PASSWORD_NEW);

        // When cambia su propia contraseña
        ResponseEntity<Void> response = http.exchange(API_URL, HttpMethod.PUT, new HttpEntity<>(body), Void.class, user.getUsername());

        // Then la operación es exitosa
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Y la contraseña en la BBDD ha cambiado
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(STRONG_PASSWORD_NEW, updatedUser.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("OldPassword123!", updatedUser.getPassword())).isFalse();
    }

    @Nested
    class PermissionErrors {

        @Test
        void changePassword_asAdmin_forAnotherUser_returnsForbidden() {
            // Given un admin está logueado
            loginAdmin("test.admin");

            // Y otro usuario existe
            User targetUser = createUserWithPassword("target.user", "OldPassword123!");

            String body = """
            {
                "currentPassword": "OldPassword123!",
                "password": "%s",
                "passwordConfirmation": "%s"
            }
            """.formatted(STRONG_PASSWORD_NEW, STRONG_PASSWORD_NEW);

            // When el admin intenta cambiar la contraseña del otro usuario
            ResponseEntity<ErrorResponse> response = http.exchange(API_URL, HttpMethod.PUT, new HttpEntity<>(body), ErrorResponse.class, targetUser.getUsername());

            // Then la operación está prohibida
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void changePassword_asGuest_returnsUnauthorized() {
            String body = "{ ... }";
            http.getRestTemplate().getInterceptors().clear();

            ResponseEntity<Void> response = http.exchange(API_URL, HttpMethod.PUT, new HttpEntity<>(body), Void.class, "any.user");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    class ValidationErrors {

        @Test
        void changePassword_withIncorrectCurrentPassword_returnsBadRequest() {
            User user = loginWithPassword("jane.doe", "CorrectOldPassword!");

            String body = """
            {
                "currentPassword": "WRONG_PASSWORD",
                "password": "%s",
                "passwordConfirmation": "%s"
            }
            """.formatted(STRONG_PASSWORD_NEW, STRONG_PASSWORD_NEW);

            ResponseEntity<ErrorResponse> response = http.exchange(API_URL, HttpMethod.PUT, new HttpEntity<>(body), ErrorResponse.class, user.getUsername());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).isEqualTo("Incorrect current password.");
        }

        @Test
        void changePassword_withMismatchedNewPasswords_returnsBadRequest() {
            User user = loginWithPassword("jane.doe", "OldPassword123!");
            String body = """
            {
                "currentPassword": "OldPassword123!",
                "password": "NewPassword1",
                "passwordConfirmation": "NewPassword2"
            }
            """;
            ResponseEntity<ErrorResponse> response = http.exchange(API_URL, HttpMethod.PUT, new HttpEntity<>(body), ErrorResponse.class, user.getUsername());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().validationErrors().getFirst().message()).isEqualTo("The passwords do not match.");
        }
    }
}
