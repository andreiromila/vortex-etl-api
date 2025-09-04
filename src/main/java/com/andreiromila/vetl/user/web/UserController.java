package com.andreiromila.vetl.user.web;

import com.andreiromila.vetl.exceptions.HttpBadRequestException;
import com.andreiromila.vetl.responses.CustomPage;
import com.andreiromila.vetl.storage.FileStorageService;
import com.andreiromila.vetl.user.User;
import com.andreiromila.vetl.user.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

import static com.andreiromila.vetl.utils.PageableUtils.getPageableWithSafeSort;

/**
 * Rest controller for handling user-related HTTP requests.
 * Maps endpoints under the base path `/api/v1/users`.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    /**
     * The allowed sorting columns for the user table
     */
    public static final Set<String> sortingColumns = Set.of("id", "username", "fullName", "email");

    /**
     * Service layer for user operations.
     */
    private final UserService userService;

    /**
     * Service for file storage.
     */
    private final FileStorageService fileStorageService;

    /**
     * Constructs the controller with a dependency-injected UserService.
     *
     * @param userService        {@link UserService} Service layer for user operations.
     * @param fileStorageService {@link FileStorageService} Service for file storage.
     */
    public UserController(final UserService userService, final FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Retrieves details of the authenticated user.
     *
     * @param user {@link User} Authenticated user resolved via Spring Security's {@link AuthenticationPrincipal}.
     * @return ResponseEntity containing a {@link UserBasicResponse} with non-sensitive user data.
     */
    @GetMapping("/me")
    public ResponseEntity<UserBasicResponse> getAuthenticatedUserDetails(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserBasicResponse.from(user));
    }

    /**
     * Creates a new user
     *
     * @param request {@link UserCreateRequest} The request body
     * @return Response entity
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserCreateResponse> create(@Valid @RequestBody UserCreateRequest request) {

        // Store the user into the database
        final User user = userService.createUser(request);

        // Create the response body
        final UserCreateResponse responseBody = UserCreateResponse.from(user);

        // Return the entity with the newly created user data
        return ResponseEntity.ok(responseBody);

    }

    @GetMapping
    public ResponseEntity<?> search(@RequestParam(value = "query", required = false) String query, Pageable pageable) {

        // Get the pageable with safe sorting
        final Pageable safeSort = getPageableWithSafeSort(pageable, sortingColumns);

        // Get the list of users from the database
        final Page<User> userPage = userService.searchUsers(query, safeSort);

        // Transform the user page to the expected output
        final List<UserBasicResponse> userList = userPage.getContent()
                .stream()
                .map(UserBasicResponse::from)
                .toList();

        // Create the response body
        final CustomPage<UserBasicResponse> response = new CustomPage<>(userList, userPage.getPageable(), userPage.getTotalElements());

        // Return the 200 ok response
        return ResponseEntity.ok(response);

    }

    /**
     * Handles the avatar upload for the currently authenticated user.
     *
     * @param currentUser {@link User} The authenticated user principal.
     * @param file        {@link MultipartFile} The avatar file being uploaded.
     * @return An HTTP 200 OK response on success.
     */
    @PostMapping(path = "/me/avatar", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Void> uploadOwnAvatar(@AuthenticationPrincipal User currentUser, @RequestParam("file") MultipartFile file) {
        // Delegate to the shared avatar upload logic
        uploadAndSetAvatar(currentUser.getId(), file);
        return ResponseEntity.ok().build();
    }

    /**
     * Handles the avatar upload for any user, restricted to administrators.
     *
     * @param userId {@link Long} The ID of the user whose avatar is being changed.
     * @param file   {@link MultipartFile} The avatar file being uploaded.
     * @return An HTTP 200 OK response on success.
     */
    @PostMapping(path = "/{userId}/avatar", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> uploadUserAvatar(@PathVariable Long userId, @RequestParam("file") MultipartFile file) {
        // Delegate to the shared avatar upload logic
        uploadAndSetAvatar(userId, file);
        return ResponseEntity.ok().build();
    }

    /**
     * Private helper method to handle file validation and storage logic.
     *
     * @param userId {@link Long} The ID of the user to associate the avatar with.
     * @param file   {@link MultipartFile} The avatar file.
     * @throws HttpBadRequestException if the file is invalid (e.g., too large).
     */
    private void uploadAndSetAvatar(Long userId, MultipartFile file) {

        // Validate file presence and size (e.g., max 5MB)
        if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024) {
            throw new HttpBadRequestException("File is empty or exceeds the 5MB limit.");
        }

        // Upload the file via the storage service to get its unique key
        String avatarKey = fileStorageService.uploadFile(file);

        // Update the user's record in the database with the new key
        userService.updateAvatarKey(userId, avatarKey);
    }
}
