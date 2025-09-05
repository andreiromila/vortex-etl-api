package com.andreiromila.vetl.api.role;

import com.andreiromila.vetl.api.AbstractIntegrationTest;
import com.andreiromila.vetl.responses.ErrorResponse;
import com.andreiromila.vetl.role.web.RoleView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RoleIntegrationTest extends AbstractIntegrationTest {

    private static final String API_ROLES_URL = "/api/v1/roles";

    @Test
    void getAllRoles_asGuest_returnsHttp401Unauthorized() {
        // When an unauthenticated request is made
        ResponseEntity<ErrorResponse> response = http.getForEntity(API_ROLES_URL, ErrorResponse.class);

        // Then the server should respond with Unauthorized
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @ParameterizedTest
    @ValueSource(longs = { 1L, 2L, 3L })
    void getAllRoles_asAuthenticatedUser_returnsHttp200OkWithRoleList(Long role) {

        // Given an authenticated user with any role
        loginWithRole("test.user.role." + role, role);

        // When the user requests the list of roles
        ResponseEntity<List<RoleView>> response = http.exchange(
                API_ROLES_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        // Then the response should be successful
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // And the body should contain the list of roles
        List<RoleView> roles = response.getBody();
        assertThat(roles).isNotNull();

        // Flyway loads 3 default roles (ADMIN, EDITOR, VIEWER)
        assertThat(roles).hasSize(3);
        assertThat(roles).extracting(RoleView::name).containsExactlyInAnyOrder("ADMIN", "EDITOR", "VIEWER");
    }
}
