package com.andreiromila.vetl.api.token;

import com.andreiromila.vetl.AbstractIntegrationTest;
import com.andreiromila.vetl.responses.CustomPage;
import com.andreiromila.vetl.responses.ErrorResponse;
import com.andreiromila.vetl.token.web.TokenBasicResponse;
import com.andreiromila.vetl.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.stream.IntStream;

import static com.andreiromila.vetl.factories.AggregatesFactory.createUser;
import static org.assertj.core.api.Assertions.assertThat;

public class TokenFilterIntegrationTest extends AbstractIntegrationTest {

    private final String ENDPOINT = "/api/v1/users/{username}/access-tokens";

    @Test
    void listTokensForUser_asAnonymous_returnsHttp401Unauthorized() {
        // When an unauthenticated guest tries to access the endpoint
        ResponseEntity<ErrorResponse> response = http.getForEntity(ENDPOINT, ErrorResponse.class, "some-user");

        // Then the response should be Unauthorized
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void listTokensForUser_asAdmin_returnsHttp200Ok() {
        // Given an admin is logged in
        loginWithRole("admin.user", 1L); // Login as ADMIN

        // And another user has tokens
        User targetUser = createUser("john.doe");
        userRepository.save(targetUser);
        IntStream.range(0, 5).forEach(i -> tokenService.createToken(targetUser.getUsername(), "Test Agent " + i));

        // When the admin requests the tokens for the target user
        ResponseEntity<CustomPage<TokenBasicResponse>> response = http.exchange(
                ENDPOINT, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                }, targetUser.getUsername());

        // Then the request is successful and returns the tokens
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().totalElements()).isEqualTo(5);
    }

    @Test
    void listTokensForUser_asSelf_returnsHttp200Ok() {
        // Given a user is logged in
        User selfUser = loginViewer("jane.doe");

        // And has generated some tokens for themselves
        IntStream.range(0, 3).forEach(i -> tokenService.createToken(selfUser.getUsername(), "Personal Agent " + i));

        // When the user requests their own list of tokens
        ResponseEntity<CustomPage<TokenBasicResponse>> response = http.exchange(
                ENDPOINT, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                }, selfUser.getUsername());

        // Then the request is successful
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // 3 created + 1 from login = 4 total
        assertThat(response.getBody().totalElements()).isEqualTo(4);
    }

    @Test
    void listTokensForUser_asDifferentUser_returnsHttp403Forbidden() {
        // Given a user is logged in
        loginViewer("jane.doe");

        // And another user exists with tokens
        User otherUser = userRepository.save(createUser("john.doe"));
        tokenService.createToken(otherUser.getUsername(), "Secret Agent");

        // When the logged-in user tries to access the other user's tokens
        ResponseEntity<ErrorResponse> response = http.getForEntity(ENDPOINT, ErrorResponse.class, otherUser.getUsername());

        // Then the request is forbidden
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void listTokensForUser_asEditor_forAnotherUser_returnsHttp403Forbidden() {
        // Given an editor is logged in
        loginWithRole("editor.user", 2L); // Login with EDITOR role (ID=2)

        // And another user exists
        User otherUser = userRepository.save(createUser("john.doe"));

        // When the editor tries to access the other user's tokens
        ResponseEntity<ErrorResponse> response = http.getForEntity(ENDPOINT, ErrorResponse.class, otherUser.getUsername());

        // Then the request is forbidden
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
