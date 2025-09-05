package com.andreiromila.vetl.security;

import com.andreiromila.vetl.security.filters.JwtAuthorizationFilter;
import com.andreiromila.vetl.token.TokenService;
import com.andreiromila.vetl.user.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Central security configuration defining
 * application protection rules and crypto
 * strategies.
 * <p>
 * Configures authentication requirements,
 * session management, and password encoding.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Cors configuration extracted to application yaml
     *
     * @param corsProperties {@link CorsProperties} The configured properties
     * @return The CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.allowedOrigins());
        configuration.setAllowedMethods(corsProperties.allowedMethods());
        configuration.setAllowedHeaders(corsProperties.allowedHeaders());
        configuration.setAllowCredentials(corsProperties.allowCredentials());
        configuration.setExposedHeaders(corsProperties.exposedHeaders());
        configuration.setMaxAge(corsProperties.maxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configures BCrypt password hashing with
     * strength 10 (default) for secure credential
     * storage.
     * <p>
     * Strength 10 = 2^10 iterations (1024 rounds)
     * Automatically handles salt generation/storage
     * Recommended alternative to deprecated SHA-based hashing
     *
     * @return BCrypt password encoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the core authentication manager
     * for DAO-based user credential verification.
     * <p>
     * Integrates with Spring Security's authentication
     * system using database-backed user details.
     * <p>
     * Typical authentication sequence:
     * <ol>
     *  <li> UsernamePasswordAuthenticationToken created from login request
     *  <li> ProviderManager delegates to DaoAuthenticationProvider
     *  <li> Provider uses UserDetailsService.loadUserByUsername()
     *  <li> PasswordEncoder.matches() compares credentials
     *  <li> Returns fully authenticated Authentication object
     * </ol>
     *
     * @param userService     {@link UserDetailsService} User details service implementation for loading security principals
     * @param passwordEncoder {@link PasswordEncoder} Password encoder for credential validation
     * @return Authentication manager configured for database authentication
     */
    @Bean
    public AuthenticationManager authenticationManager(final UserDetailsService userService, final PasswordEncoder passwordEncoder) {
        final DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }

    /**
     * Defines the security filter chain with
     * stateless JWT-based authentication
     * <p>
     * Disabled CSRF because:
     * - Stateless API using JWT/Bearer tokens
     * - No browser cookie-based authentication
     * - Protected endpoints require explicit auth
     * <p>
     * Session management ensures:
     * - No session cookies created
     * - Each request requires explicit auth
     *
     * @param http                    {@link HttpSecurity} Security configuration builder
     * @param authenticatedEntryPoint {@link UnauthorizedAuthenticatedEntryPoint} The unauthorized en
     * @return Configured security filter chain
     * @throws Exception On configuration errors
     */
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http, final UnauthorizedAuthenticatedEntryPoint authenticatedEntryPoint, final UserService userService, final TokenService tokenService) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(SecurityConfig::httpAuthorizationsConfig)
                .addFilterBefore(new JwtAuthorizationFilter(userService, tokenService), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(SecurityConfig::stateless)
                .exceptionHandling(config -> config.authenticationEntryPoint(authenticatedEntryPoint))
                .build();
    }

    /**
     * Configures stateless session management (no JSESSIONID cookies)
     *
     * @param session {@link SessionManagementConfigurer} Session management configurer
     */
    private static void stateless(final SessionManagementConfigurer<HttpSecurity> session) {
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    /**
     * Defines endpoint authorization rules
     *
     * @param registry {@link AuthorizeHttpRequestsConfigurer} Authorization configuration registry
     */
    private static void httpAuthorizationsConfig(final AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry) {
        // We need to allow the /error endpoint for everyone
        registry.requestMatchers("/error").permitAll();

        // User registration is allowed for any user
        registry.requestMatchers(HttpMethod.POST, "/api/v1/login").permitAll();

        // Allow public access to the activation endpoints (GET for validation, POST for commitment)
        registry.requestMatchers(HttpMethod.GET, "/api/v1/users/{username}/activations/{token}").permitAll();
        registry.requestMatchers(HttpMethod.POST, "/api/v1/users/{username}/activations").permitAll();

        // Everything else must be authenticated
        registry.anyRequest().authenticated();

    }
}
