package com.andreiromila.vetl.api.user;

import com.andreiromila.vetl.AbstractIntegrationTest;
import com.andreiromila.vetl.role.RoleRepository;
import com.andreiromila.vetl.role.UserRole;
import com.andreiromila.vetl.user.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class UserUpdateIntegrationTest  extends AbstractIntegrationTest {

    private final String API_URL = "/api/v1/users/{username}/details";

    @Autowired
    RoleRepository roleRepository;

    @Nested
    class AdminPermissions {

        @Test
        void updateUserDetails_asAdminForAnotherUser_succeeds() {
            // Given un admin está logueado
            loginAdmin("test.admin");

            // Y un usuario objetivo existe con el rol de Viewer y estado enabled=true
            User targetUser = loginViewer("target.user");

            // El admin intenta cambiar el nombre, deshabilitar al usuario y promoverlo a Editor
            String body = """
                {
                    "fullName": "Name Updated by Admin",
                    "roleId": 2,
                    "enabled": false
                }
                """;

            // When la petición se envía
            ResponseEntity<Void> response = http.exchange(API_URL, HttpMethod.PUT, new HttpEntity<>(body), Void.class, targetUser.getUsername());

            // Then la operación es exitosa
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            // Y los datos en la BBDD reflejan todos los cambios
            User updatedUser = userRepository.findById(targetUser.getId()).orElseThrow();
            assertThat(updatedUser.getFullName()).isEqualTo("Name Updated by Admin");
            assertThat(updatedUser.isEnabled()).isFalse();

            Set<UserRole> roles = roleRepository.findUserRoleByUsers(List.of(updatedUser.getId()));
            assertThat(roles.iterator().next().role()).isEqualTo(2L); // Ahora es Editor
        }

        @Test
        void updateUserDetails_asAdminForSelf_onlyFullNameIsUpdated() {
            // Given un admin está logueado
            User admin = loginAdmin("test.admin");

            // Y el admin intenta cambiar su propio nombre Y su rol (lo cual debería ser ignorado)
            String body = """
                {
                    "fullName": "Admin Name Updated",
                    "roleId": 3,
                    "enabled": false
                }
                """;

            // When
            ResponseEntity<Void> response = http.exchange(API_URL, HttpMethod.PUT, new HttpEntity<>(body), Void.class, admin.getUsername());

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            User updatedAdmin = userRepository.findById(admin.getId()).orElseThrow();
            assertThat(updatedAdmin.getFullName()).isEqualTo("Admin Name Updated");

            // Verificaciones críticas: rol y estado NO deben haber cambiado
            Set<UserRole> roles = roleRepository.findUserRoleByUsers(List.of(updatedAdmin.getId()));
            assertThat(roles.iterator().next().role()).isEqualTo(1L); // Sigue siendo ADMIN
            assertThat(updatedAdmin.isEnabled()).isTrue();
        }
    }

    @Nested
    class UserPermissions {
        @Test
        void updateUserDetails_asSelf_onlyFullNameIsUpdated() {
            // Given un Viewer está logueado
            User user = loginViewer("jane.doe");

            // Y el usuario intenta cambiar su nombre, y maliciosamente también su rol y estado
            String body = """
                {
                    "fullName": "Jane Doe NewName",
                    "roleId": 1,
                    "enabled": false
                }
                """;

            // When
            ResponseEntity<Void> response = http.exchange(API_URL, HttpMethod.PUT, new HttpEntity<>(body), Void.class, user.getUsername());

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            User updatedUser = userRepository.findById(user.getId()).orElseThrow();
            assertThat(updatedUser.getFullName()).isEqualTo("Jane Doe NewName");

            // Verificaciones críticas: rol y estado NO han cambiado
            Set<UserRole> roles = roleRepository.findUserRoleByUsers(List.of(updatedUser.getId()));
            assertThat(roles.iterator().next().role()).isEqualTo(3L); // Sigue siendo Viewer
            assertThat(updatedUser.isEnabled()).isTrue();
        }

        @Test
        void updateUserDetails_asDifferentUser_returnsForbidden() {
            // Given un usuario está logueado
            loginViewer("actor.user");

            // Y otro usuario existe
            User targetUser = loginViewer("target.user");

            String body = """
                { "fullName": "Should Not Update", "roleId": 1, "enabled": true }
                """;

            // When el actor intenta editar al objetivo
            ResponseEntity<Void> response = http.exchange(API_URL, HttpMethod.PUT, new HttpEntity<>(body), Void.class, targetUser.getUsername());

            // Then la operación es prohibida por el @PreAuthorize
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    class GuestPermissions {
        @Test
        void updateUserDetails_asGuest_returnsUnauthorized() {
            String body = """
                { "fullName": "Should Not Update", "roleId": 1, "enabled": true }
                """;
            http.getRestTemplate().getInterceptors().clear(); // Sin autenticación

            ResponseEntity<Void> response = http.exchange(API_URL, HttpMethod.PUT, new HttpEntity<>(body), Void.class, "some-user");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
