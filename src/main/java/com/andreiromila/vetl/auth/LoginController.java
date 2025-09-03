package com.andreiromila.vetl.auth;

import com.andreiromila.vetl.token.TokenService;
import com.andreiromila.vetl.token.TokenWithExpiration;
import com.andreiromila.vetl.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest Controller handling user authentication
 * operations.
 * <p>
 * Serves as entry point for credential
 * validation and JWT token initiation.
 */
@RestController
@RequestMapping("/api/v1/login")
public class LoginController {

    /**
     * Core authentication component delegated by Spring Security
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Token service core component
     */
    private final TokenService tokenService;

    /**
     * Constructs controller with required authentication dependencies
     *
     * @param authenticationManager {@link AuthenticationManager} Spring Security's authentication coordinator
     * @param tokenService          {@link TokenService} Token service bean
     */
    public LoginController(final AuthenticationManager authenticationManager, final TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    /**
     * Authenticates user credentials and initiates secure session
     * <ol>
     *  <li> Convert credentials to authentication token
     *  <li> Delegate to authentication provider chain
     *  <li> Return secured response on successful verification
     * </ol>
     *
     * @param credentials {@link LoginRequest} Login request payload containing username/password
     * @return ResponseEntity with authentication token (implementation pending)
     */
    @PostMapping
    public ResponseEntity<?> login(@RequestHeader("User-Agent") String userAgent, @RequestBody LoginRequest credentials) {

        // Try to authenticate
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(credentials.username(), credentials.password())
        );

        // Get the authenticated user
        final User user = (User) authentication.getPrincipal();

        // Now let's generate the token
        final TokenWithExpiration token = tokenService.createToken(user.getUsername(), userAgent);

        // Return the response
        return ResponseEntity.ok(
                new LoginResponse(token.token(), token.expiresAt())
        );
    }

}
