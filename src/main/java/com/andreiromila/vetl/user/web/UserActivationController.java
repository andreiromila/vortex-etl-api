package com.andreiromila.vetl.user.web;

import com.andreiromila.vetl.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles the user account activation process.
 */
@RestController
@RequestMapping("/api/v1/users/{username}/activations")
public class UserActivationController {

    private final UserService userService;

    public UserActivationController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Validates an activation token to check if it's still usable.
     * This endpoint provides immediate feedback to the frontend upon page load.
     *
     * @param username {@link String} The user's username from the query parameter.
     * @param token    {@link String} The activation token from the query parameter.
     *
     * @return An HTTP 200 OK response if the token is valid, otherwise throws an exception
     * that will be handled by the GlobalControllerAdvice (e.g., 404, 400).
     */
    @GetMapping("/{token}")
    public ResponseEntity<Void> validateActivationToken(@PathVariable String username,
                                                        @PathVariable String token) {

        // Validates the token for the current user
        userService.validateActivationToken(username, token);

        // If no exception is throwend then it's a 204 No Content response
        return ResponseEntity.noContent().build();
    }

    /**
     * Activates a user account by setting their password, after the token has been validated.
     * This is the final commitment step of the activation process.
     *
     * @param request The request body containing the username, token, and new password.
     * @return An HTTP 200 OK response on successful activation.
     */
    @PostMapping
    public ResponseEntity<Void> activateAccount(@PathVariable String username, @Valid @RequestBody UserActivationRequest request) {

        // Try to activate the current user and set the password
        userService.activateUserAccount(
                username,
                request.token(),
                request.password()
        );

        // If no exception is throwend then it's a 204 No Content response
        return ResponseEntity.noContent().build();
    }

}
