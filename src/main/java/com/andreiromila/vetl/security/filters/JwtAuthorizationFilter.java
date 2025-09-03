package com.andreiromila.vetl.security.filters;

import com.andreiromila.vetl.token.TokenService;
import com.andreiromila.vetl.user.User;
import com.andreiromila.vetl.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authorization filter to authenticate
 * the user using a Bearer token
 */
@Log4j2
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    /**
     * User service bean
     */
    private final UserService userService;

    /**
     * Token service bean
     */
    private final TokenService tokenService;

    /**
     * The JWT authorization filter constructor
     *
     * @param userService  {@link UserService} User service bean
     * @param tokenService {@link TokenService} Token service bean
     */
    public JwtAuthorizationFilter(final UserService userService, final TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    /**
     * Implementation for the authorization filter
     *
     * @param request     {@link HttpServletRequest} The http request
     * @param response    {@link HttpServletResponse} The http response
     * @param filterChain {@link FilterChain} The filter chain
     * @throws ServletException In case of servlet error
     * @throws IOException      In case of i/o error
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {

        // Get the authorization header
        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            UsernamePasswordAuthenticationToken authentication = getAuthentication(request, authorizationHeader);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Validates the token and returns the username
     * and password authentication to be stored in
     * the {@link SecurityContextHolder}
     *
     * @param request             {@link HttpServletRequest} The http servlet request
     * @param authorizationHeader {@link String} The authorization bearer token
     * @return The username and password authentication
     */
    private UsernamePasswordAuthenticationToken getAuthentication(final HttpServletRequest request, final String authorizationHeader) {

        try {

            final String token = authorizationHeader.substring(7);
            final String userAgent = request.getHeader("User-Agent");

            // Parse, validate and extract the username from the token
            final String username = tokenService.extractUsername(token, userAgent);
            final User userDetails = userService.loadUserByUsername(username);

            // Now, what if the user is locked?
            if ( ! userDetails.isEnabled()) {
                log.warn("The user [{}] is disabled.", username);
                return null;
            }

            // Return the authentication token
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        } catch (Exception e) {

            // Log any exception
            log.error(e.getMessage());
            log.error(e);

            // No authorization if this method fails
            return null;
        }
    }
}
