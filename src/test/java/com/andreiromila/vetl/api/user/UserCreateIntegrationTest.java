package com.andreiromila.vetl.api.user;

import com.andreiromila.vetl.api.AbstractIntegrationTest;
import com.andreiromila.vetl.mail.EmailService;
import com.andreiromila.vetl.responses.ErrorResponse;
import com.andreiromila.vetl.user.User;
import com.andreiromila.vetl.user.web.UserCreateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.ZonedDateTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.andreiromila.vetl.factories.AggregatesFactory.createUser;
import static com.andreiromila.vetl.utils.StringUtils.generateRandomString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class UserCreateIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    EmailService emailService;

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
                    "roles": [3]
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
    void createUser_asAdmin_doesNotSetPasswordAndSendsInvitationEmail() {
        // Given un administrador está logueado
        loginAdmin("test.admin");

        // El cuerpo de la petición ya no lleva contraseña
        var body = """
                {
                    "fullName": "New User",
                    "username": "new.user",
                    "email": "new.user@example.com",
                    "roles": [3]
                }
                """;

        // When el admin crea un nuevo usuario
        ResponseEntity<UserCreateResponse> response = http.postForEntity(
                "/api/v1/users",
                new HttpEntity<>(body),
                UserCreateResponse.class
        );

        // Then la respuesta de la API es correcta
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        Long newUserId = response.getBody().id();

        // Y el usuario se ha guardado en la BBDD correctamente
        User createdUser = userRepository.findById(newUserId).orElseThrow();
        assertThat(createdUser.getPassword()).isNull(); // La contraseña es nula
        assertThat(createdUser.isEnabled()).isFalse(); // El usuario está deshabilitado
        assertThat(createdUser.getEmailActivationCode()).isNotNull().hasSize(64); // Se ha generado un código
        assertThat(createdUser.getEmailValidatedAt()).isNull(); // Aún no ha sido validado

        // --- VERIFICACIÓN DEL ENVÍO DE EMAIL ---

        // Creamos un ArgumentCaptor para capturar el cuerpo del email que se envió
        ArgumentCaptor<String> emailBodyCaptor = ArgumentCaptor.forClass(String.class);

        // Verificamos que el método sendSimpleMessage fue llamado UNA VEZ
        // con el email del nuevo usuario, un asunto específico, y capturamos el cuerpo.
        // Usamos `timeout` por si el envío de email se hiciera asíncrono en el futuro.
        verify(emailService, timeout(1000).times(1))
                .sendSimpleMessage(
                        eq("new.user@example.com"),
                        eq("New Vortex ETL Registration Link"),
                        emailBodyCaptor.capture()
                );

        // Ahora podemos hacer aserciones sobre el cuerpo del email capturado
        String capturedEmailBody = emailBodyCaptor.getValue();
        assertThat(capturedEmailBody).contains("http://localhost:5173/set-password");
        assertThat(capturedEmailBody).contains("token=" + createdUser.getEmailActivationCode());
        assertThat(capturedEmailBody).contains("username=" + createdUser.getUsername());
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
                    "roles": [3]
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
            loginAdmin("administrator");

            var body = """
                {
                    "fullName": null,
                    "username": "john.doe",
                    "email": "john.doe@email.com",
                    "roles": [3]
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
            loginAdmin("administrator");

            var body = """
                {
                    "fullName": "invalid \\" user # name",
                    "username": "john.doe",
                    "email": "john.doe@email.com",
                    "roles": [3]
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
            loginAdmin("administrator");

            var body = """
                {
                    "fullName": "abc",
                    "username": "john.doe",
                    "email": "john.doe@email.com",
                    "roles": [3]
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
            loginAdmin("administrator");

            final String fullName101Characters = IntStream.rangeClosed(1, 101)
                    .mapToObj(i -> "b")
                    .collect(Collectors.joining());

            var body = """
                {
                    "fullName": "%s",
                    "username": "john.doe",
                    "email": "john.doe@email.com",
                    "roles": [3]
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
            loginAdmin("administrator");

            var body = """
                {
                    "fullName": "John Doe",
                    "username": null,
                    "email": "john.doe@email.com",
                    "roles": [3]
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
            loginAdmin("administrator");

            var body = """
                {
                    "fullName": "John Doe",
                    "username": "invalid user name %%",
                    "email": "john.doe@email.com",
                    "roles": [3]
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
            loginAdmin("administrator");

            final String username101Characters = IntStream.rangeClosed(1, 101)
                    .mapToObj(i -> "a")
                    .collect(Collectors.joining());

            var body = """
                {
                    "fullName": "John Doe",
                    "username": "%s",
                    "email": "john.doe@email.com",
                    "roles": [3]
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
            loginAdmin("administrator");

            // The username must be at least 4 characters
            var body = """
                {
                    "fullName": "John Doe",
                    "username": "abc",
                    "email": "john.doe@email.com",
                    "roles": [3]
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
            loginAdmin("administrator");

            // Given we have an existing user with username "existing.username"
            userRepository.save(createUser("existing.username"));

            var body = """
                {
                    "fullName": "John Doe",
                    "username": "existing.username",
                    "email": "john.doe@email.com",
                    "roles": [3]
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
            loginAdmin("administrator");

            var body = """
                {
                    "fullName": "John Doe",
                    "username": "john.doe",
                    "email": null,
                    "roles": [3]
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
            loginAdmin("administrator");

            var body = """
                {
                    "fullName": "John Doe",
                    "username": "john.doe",
                    "email": "invalid-email",
                    "roles": [3]
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
            loginAdmin("administrator");

            final User user = createUser("existing.email");
            user.setEmail("existing.email@email.com");

            userRepository.save(user);

            var body = """
                {
                    "fullName": "John Doe",
                    "username": "john.doe",
                    "email": "existing.email@email.com",
                    "roles": [3]
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
            loginAdmin("administrator");

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
                    "roles": [3]
                }
                """.formatted(localPart, domain);

            final ResponseEntity<ErrorResponse> response = http.postForEntity("/api/v1/users", new HttpEntity<>(body), ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().validationErrors().getFirst().field()).isEqualTo("email");

        }

    }

}
