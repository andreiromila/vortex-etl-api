package com.andreiromila.vetl.api.token;

import com.andreiromila.vetl.api.AbstractIntegrationTest;
import com.andreiromila.vetl.responses.CustomPage;
import com.andreiromila.vetl.responses.ErrorResponse;
import com.andreiromila.vetl.token.web.TokenBasicResponse;
import com.andreiromila.vetl.user.web.UserBasicResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenFilterIntegrationTest extends AbstractIntegrationTest {

    @Test
    void filterTokens_byGuest_returnsUnauthorized() {

        ResponseEntity<ErrorResponse> response = http.getForEntity("/api/v1/access-tokens", ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(response.getBody())
                .isNotNull()
                .satisfies(body -> {
                    assertThat(body.code()).isEqualTo(401);
                    assertThat(body.message()).isEqualTo("Invalid credentials.");
                });

    }

    @Test
    void filterTokens_byUser_returnsCustomPage() {

        login("john");

        IntStream.rangeClosed(1, 33)
                .forEach(index -> tokenService.createToken("john", SPRING_BOOT_AGENT));

        final ResponseEntity<CustomPage<TokenBasicResponse>> response = http.exchange(
                "/api/v1/access-tokens?page=1&size=5&sort=uuid,desc", HttpMethod.GET, new HttpEntity<>(""), new ParameterizedTypeReference<>() { }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isNotNull()
                .satisfies(body -> {

                    // We should be at page 1
                    assertThat(body.page()).isOne();

                    // Total of 7 pages
                    assertThat(body.totalPages()).isEqualTo(7);

                    // The total number of elements is 34 (logged in token and the other 33)
                    assertThat(body.totalElements()).isEqualTo(34);

                    // Total number of tokens in the content is 5 (the page size)
                    assertThat(body.content()).hasSize(5);

                });
    }

    @Test
    void filterTokens_withInvalidSortColumn_returnsResultsIgnoringTheValues() {

        // Given we have a verified user that is logged in
        login("john");

        // And 3 more tokens
        IntStream.rangeClosed(1, 3)
                .forEach(index -> tokenService.createToken("john", SPRING_BOOT_AGENT));

        // When John lists the users ordering by username and by unknown, receives the list sorted by username
        final ResponseEntity<CustomPage<UserBasicResponse>> response = http.exchange(
                "/api/v1/access-tokens?sort=uuid,asc&sort=unknown,desc", HttpMethod.GET, new HttpEntity<>(""), new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Total results 4
        assertThat(response.getBody().totalElements()).isEqualTo(4);
    }
}
