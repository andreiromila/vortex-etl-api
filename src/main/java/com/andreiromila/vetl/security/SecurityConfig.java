package com.andreiromila.vetl.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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

}
