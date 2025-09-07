package com.andreiromila.vetl.api.user;

import com.andreiromila.vetl.AbstractIntegrationTest;
import com.andreiromila.vetl.responses.ErrorResponse;
import com.andreiromila.vetl.user.User;
import com.andreiromila.vetl.user.web.UserBasicResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;

public class UserDetailsIntegrationTest extends AbstractIntegrationTest {

    @Test
    void userDetails_withoutBearerToken_returnsUnauthorized() {

        final ResponseEntity<ErrorResponse> response = http.getForEntity("/api/v1/users/me", ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().get(WWW_AUTHENTICATE)).hasSize(1);
        assertThat(response.getHeaders().get(WWW_AUTHENTICATE)).contains("Bearer realm=vortex.andreiromila.com");

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(401);
        assertThat(response.getBody().message()).isEqualTo("Invalid credentials.");
    }

    @Test
    void userDetails_withAuthenticatedUser_returnsUserDetails() {

        // Given we have a user and a valid token
        final User john = loginAdmin("john");
        final ResponseEntity<UserBasicResponse> response = http.getForEntity("/api/v1/users/" + john.getUsername(), UserBasicResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().username()).isEqualTo(john.getUsername());
        assertThat(response.getBody().email()).isEqualTo(john.getEmail());
        assertThat(response.getBody().fullName()).isEqualTo(john.getFullName());
        assertThat(response.getBody().enabled()).isTrue();

        // The created at property is set by the database, we don't have it here
        assertThat(response.getBody().createdAt()).isNotNull();
        assertThat(response.getBody().modifiedAt()).isNotNull();

        // And it has a list of roles
        assertThat(response.getBody().roles()).hasSize(1);
    }
}
