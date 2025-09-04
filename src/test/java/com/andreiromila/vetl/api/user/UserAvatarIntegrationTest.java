package com.andreiromila.vetl.api.user;

import com.andreiromila.vetl.api.AbstractIntegrationTest;
import com.andreiromila.vetl.factories.AggregatesFactory;
import com.andreiromila.vetl.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserAvatarIntegrationTest extends AbstractIntegrationTest {

    private final MockMultipartFile avatarFile = new MockMultipartFile(
            "file",
            "avatar.png",
            MediaType.IMAGE_PNG_VALUE,
            "test-image-data".getBytes()
    );

    /**
     * Helper to get authentication headers for MockMVC.
     */
    HttpHeaders getAuthHeadersForUser(User user) {
        String token = tokenService.createToken(user.getUsername(), SPRING_BOOT_AGENT).token();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.add(HttpHeaders.USER_AGENT, SPRING_BOOT_AGENT);
        return headers;
    }

    @Test
    void uploadOwnAvatar_asAuthenticatedUser_returnsHttp201CreatedWithLocationHeader() throws Exception {
        // Given
        User authenticatedUser = login("john.doe");

        // When
        mvc.perform(MockMvcRequestBuilders.multipart("/api/v1/users/{username}/avatar", authenticatedUser.getUsername())
                        .file(avatarFile)
                        .headers(getAuthHeadersForUser(authenticatedUser)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION));

        // Then
        User updatedUser = userRepository.findById(authenticatedUser.getId()).orElseThrow();
        assertThat(updatedUser.getAvatarKey()).isNotNull();
        assertThat(updatedUser.getAvatarKey()).endsWith("_avatar.png");
    }


    @Test
    void uploadOwnAvatar_asGuest_returnsHttp401Unauthorized() throws Exception {
        mvc.perform(MockMvcRequestBuilders.multipart("/api/v1/users/me/avatar").file(avatarFile))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uploadUserAvatar_asAdmin_returnsHttp200Ok() throws Exception {
        // Given
        User admin = login("admin.user", 1L); // Login with ADMIN role
        User targetUser = userRepository.save(AggregatesFactory.createUser("target.user"));

        // When
        mvc.perform(MockMvcRequestBuilders.multipart("/api/v1/users/{username}/avatar", targetUser.getUsername())
                        .file(avatarFile)
                        .headers(getAuthHeadersForUser(admin)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION));

        // Then
        User updatedTargetUser = userRepository.findById(targetUser.getId()).orElseThrow();
        assertThat(updatedTargetUser.getAvatarKey()).isNotNull();
        assertThat(updatedTargetUser.getAvatarKey()).endsWith("_avatar.png");
    }

    @Test
    void uploadUserAvatar_asRegularUser_returnsHttp403Forbidden() throws Exception {
        // Given
        User regularUser = loginViewer("regular.user");
        User targetUser = userRepository.save(AggregatesFactory.createUser("another.user"));

        // When / Then
        mvc.perform(MockMvcRequestBuilders.multipart("/api/v1/users/{username}/avatar", targetUser.getUsername())
                        .file(avatarFile)
                        .headers(getAuthHeadersForUser(regularUser)))
                .andExpect(status().isForbidden());
    }


    @Test
    void uploadAvatar_withFileTooLarge_returnsHttp400BadRequest() throws Exception {
        // Given
        User authenticatedUser = login("john.doe");
        byte[] largeContent = new byte[6 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile("file", "large-avatar.png", MediaType.IMAGE_PNG_VALUE, largeContent);

        // When / Then
        mvc.perform(MockMvcRequestBuilders.multipart("/api/v1/users/{username}/avatar", authenticatedUser.getUsername())
                        .file(largeFile)
                        .headers(getAuthHeadersForUser(authenticatedUser)))
                .andExpect(status().isBadRequest());
    }

}
