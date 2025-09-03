package com.andreiromila.vetl.api.user;

import com.andreiromila.vetl.api.AbstractIntegrationTest;
import com.andreiromila.vetl.responses.ErrorResponse;
import com.andreiromila.vetl.user.User;
import com.andreiromila.vetl.user.web.UserCreateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.ZonedDateTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.andreiromila.vetl.factories.AggregatesFactory.createUser;
import static com.andreiromila.vetl.utils.StringUtils.generateRandomString;
import static org.assertj.core.api.Assertions.assertThat;

public class UserCreateIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvcTester mvc;

    MockMvcTester.MockMvcRequestBuilder httpPost() {
        return mvc.post()
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.USER_AGENT, SPRING_BOOT_AGENT);
    }

    @Test
    void createUser_byGuest_returnsHttp401Unauthorized() {

        var body = """
                {
                    "fullName": "John W. Doe",
                    "username": "john.doe",
                    "email": "john.doe@email.com",
                    "password": "Pa$$w0rd!"
                }
                """;

        httpPost()
                .uri("/api/v1/users")
                .content(body)
                .exchange()

                .assertThat()
                .hasStatus(HttpStatus.UNAUTHORIZED)

                .bodyJson()
                .convertTo(ErrorResponse.class)
                .satisfies(error -> {
                    assertThat(error.timestamp()).isBefore(ZonedDateTime.now());
                    assertThat(error.code()).isEqualTo(401);
                    assertThat(error.message()).isEqualTo("Invalid credentials.");
                });
    }

    @Test
    void createUser_withValidData_returnsCreatedUser() {

        // Given we have an administrator
        login("administrator");

        var body = """
                {
                    "fullName": "John W. Doe",
                    "username": "john.doe",
                    "email": "john.doe@email.com",
                    "password": "Pa$$w0rd!"
                }
                """;

        ResponseEntity<UserCreateResponse> response = http.postForEntity("/api/v1/users", new HttpEntity<>(body), UserCreateResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Make sure the user is stored into the database
        assertThat(userRepository.findById(response.getBody().id()))
                .hasValueSatisfying(user -> {

                    // The stored password should NOT be in plain text
                    assertThat(user.getPassword()).isNotEqualTo("Pa$$w0rd!");

                    assertThat(user.getUsername()).isEqualTo("john.doe");
                    assertThat(user.getFullName()).isEqualTo("John W. Doe");
                    assertThat(user.getEmail()).isEqualTo("john.doe@email.com");

                    assertThat(user.getCreatedAt()).isNotNull();
                    assertThat(user.getModifiedAt()).isNotNull();
                });

    }

    @Test
    void createUser_byRegularUser_returnsForbidden() {

        // Given we have a regular user
        loginViewer("regular.user");

        var body = """
                {
                    "fullName": "John W. Doe",
                    "username": "john.doe",
                    "email": "john.doe@email.com",
                    "password": "Pa$$w0rd!"
                }
                """;

        ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/users", new HttpEntity<>(body), ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Make sure the user is NOT stored into the database
        assertThat(userRepository.count()).isOne(); // The admin user only

    }

    @Nested
    @DisplayName("Create user fails if")
    class UserCreateValidationTest {

        @Test
        @DisplayName("The full name is null")
        void createUser_withNullFullName_returnsBadRequest() {

            // Given we have an administrator
            login("administrator");

            var body = """
                {
                    "fullName": null,
                    "username": "john.doe",
                    "email": "john.doe@email.com",
                    "password": "Pa$$w0rd!"
                }
                """;

            final ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/users", new HttpEntity<>(body), ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().validationErrors().getFirst().field()).isEqualTo("fullName");

        }

        @Test
        @DisplayName("The full name contains invalid characters")
        void createUser_withInvalidFullName_returnsBadRequest() {

            // Given we have an administrator
            login("administrator");

            var body = """
                {
                    "fullName": "invalid \\" user # name",
                    "username": "john.doe",
                    "email": "john.doe@email.com",
                    "password": "Pa$$w0rd!"
                }
                """;


            final ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/users", new HttpEntity<>(body), ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().validationErrors().getFirst().field()).isEqualTo("fullName");

        }

        @Test
        @DisplayName("The full name contains less than 4 characters")
        void createUser_with3CharactersFullName_returnsBadRequest() {

            // Given we have an administrator
            login("administrator");

            var body = """
                {
                    "fullName": "abc",
                    "username": "john.doe",
                    "email": "john.doe@email.com",
                    "password": "Pa$$w0rd!"
                }
                """;

            final ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/users", new HttpEntity<>(body), ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().validationErrors().getFirst().field()).isEqualTo("fullName");

        }

        @Test
        @DisplayName("The full name contains more than 100 characters")
        void createUser_with101CharactersFullName_returnsBadRequest() {

            // Given we have an administrator
            login("administrator");

            final String fullName101Characters = IntStream.rangeClosed(1, 101)
                    .mapToObj(i -> "b")
                    .collect(Collectors.joining());

            var body = """
                {
                    "fullName": "%s",
                    "username": "john.doe",
                    "email": "john.doe@email.com",
                    "password": "Pa$$w0rd!"
                }
                """.formatted(fullName101Characters);

            final ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/users", new HttpEntity<>(body), ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().validationErrors().getFirst().field()).isEqualTo("fullName");

        }

        @Test
        @DisplayName("The username is null")
        void createUser_withNullUsername_returnsBadRequest() {

            // Given we have an administrator
            login("administrator");

            var body = """
                {
                    "fullName": "John Doe",
                    "username": null,
                    "email": "john.doe@email.com",
                    "password": "Pa$$w0rd!"
                }
                """;

            final ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/users", new HttpEntity<>(body), ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().validationErrors().size()).isEqualTo(1);

        }

        @Test
        @DisplayName("The username contains invalid characters")
        void createUser_withInvalidUsername_returnsBadRequest() {

            // Given we have an administrator
            login("administrator");

            var body = """
                {
                    "fullName": "John Doe",
                    "username": "invalid user name %%",
                    "email": "john.doe@email.com",
                    "password": "Pa$$w0rd!"
                }
                """;

            final ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/users", new HttpEntity<>(body), ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().validationErrors().size()).isEqualTo(1);

        }

        @Test
        @DisplayName("The username contains more than 100 characters")
        void createUser_with101CharactersUsername_returnsBadRequest() {

            // Given we have an administrator
            login("administrator");

            final String username101Characters = IntStream.rangeClosed(1, 101)
                    .mapToObj(i -> "a")
                    .collect(Collectors.joining());

            var body = """
                {
                    "fullName": "John Doe",
                    "username": "%s",
                    "email": "john.doe@email.com",
                    "password": "Pa$$w0rd!"
                }
                """.formatted(username101Characters);

            final ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/users", new HttpEntity<>(body), ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().validationErrors().getFirst().field()).isEqualTo("username");

        }

        @Test
        @DisplayName("The username contains less than 3 characters")
        void createUser_with3CharactersUsername_returnsBadRequest() {

            // Given we have an administrator
            login("administrator");

            // The username must be at least 4 characters
            var body = """
                {
                    "fullName": "John Doe",
                    "username": "abc",
                    "email": "john.doe@email.com",
                    "password": "Pa$$w0rd!"
                }
                """;

            final ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/users", new HttpEntity<>(body), ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().validationErrors().getFirst().field()).isEqualTo("username");

        }

        @Test
        @DisplayName("The username is already taken")
        void createUser_withExistingUsername_returnsBadRequest() {

            // Given we have an administrator
            login("administrator");

            // Given we have an existing user with username "existing.username"
            userRepository.save(createUser("existing.username"));

            var body = """
                {
                    "fullName": "John Doe",
                    "username": "existing.username",
                    "email": "john.doe@email.com",
                    "password": "Pa$$w0rd!"
                }
                """;

            final ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/users", new HttpEntity<>(body), ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().validationErrors().getFirst().field()).isEqualTo("username");

        }

        @Test
        @DisplayName("The email is null")
        void createUser_withNullEmail_returnsBadRequest() {

            // Given we have an administrator
            login("administrator");

            var body = """
                {
                    "fullName": "John Doe",
                    "username": "john.doe",
                    "email": null,
                    "password": "Pa$$w0rd!"
                }
                """;

            final ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/users", new HttpEntity<>(body), ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().validationErrors().getFirst().field()).isEqualTo("email");

        }

        @Test
        @DisplayName("The email is invalid")
        void createUser_withInvalidEmail_returnsBadRequest() {

            // Given we have an administrator
            login("administrator");

            var body = """
                {
                    "fullName": "John Doe",
                    "username": "john.doe",
                    "email": "invalid-email",
                    "password": "Pa$$w0rd!"
                }
                """;

            final ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/users", new HttpEntity<>(body), ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().validationErrors().getFirst().field()).isEqualTo("email");

        }

        @Test
        @DisplayName("The email is already taken")
        void createUser_withExistingEmail_returnsBadRequest() {

            // Given we have an administrator
            login("administrator");

            final User user = createUser("existing.email");
            user.setEmail("existing.email@email.com");

            userRepository.save(user);

            var body = """
                {
                    "fullName": "John Doe",
                    "username": "john.doe",
                    "email": "existing.email@email.com",
                    "password": "Pa$$w0rd!"
                }
                """;

            final ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/users", new HttpEntity<>(body), ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().validationErrors().getFirst().field()).isEqualTo("email");

        }

        @Test
        @DisplayName("The email contains more than 200 characters")
        void createUser_with201CharactersEmail_returnsBadRequest() {

            // Given we have an administrator
            login("administrator");

            // Hibernate validator uses 64 character max length for the username
            final String localPart = "a" + generateRandomString(63);

            // Hibernate have a 255 max limit but a 63 limit on any "part" "abc".abc.com
            final String domain = IntStream.rangeClosed(1, 4)
                    .mapToObj(i -> "b" + generateRandomString(50))
                    .collect(Collectors.joining("."));

            var body = """
                {
                    "fullName": "John Doe",
                    "username": "john.doe",
                    "email": "%s@%s.com",
                    "password": "Pa$$w0rd!"
                }
                """.formatted(localPart, domain);

            final ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/users", new HttpEntity<>(body), ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().validationErrors().getFirst().field()).isEqualTo("email");

        }

        @Test
        @DisplayName("The password is null")
        void createUser_withNullPassword_returnsBadRequest() {

            // Given we have an administrator
            login("administrator");

            var body = """
                {
                    "fullName": "John Doe",
                    "username": "john.doe",
                    "email": "john.doe@example.com",
                    "password": null
                }
                """;

            final ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/users", new HttpEntity<>(body), ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().validationErrors().getFirst().field()).isEqualTo("password");

        }

        @Test
        @DisplayName("The password has invalid characters")
        void createUser_withInvalidPassword_returnsBadRequest() {

            // Given we have an administrator
            login("administrator");

            var body = """
                {
                    "fullName": "John Doe",
                    "username": "john.doe",
                    "email": "john.doe@example.com",
                    "password": "abc"
                }
                """;

            final ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/users", new HttpEntity<>(body), ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().validationErrors().getFirst().field()).isEqualTo("password");

        }
    }

}
