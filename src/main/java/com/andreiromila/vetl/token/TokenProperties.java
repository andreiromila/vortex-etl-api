package com.andreiromila.vetl.token;

import io.jsonwebtoken.security.Keys;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import javax.crypto.SecretKey;
import java.util.Base64;

/**
 * Configuration properties for JWT token generation and validation.
 * Binds application.yml properties to security components.
 *
 * @param ttl       {@link Long} Token time-to-live in seconds
 * @param secret    {@link String} Base64-encoded secret key for signing
 * @param secretKey {@link SecretKey} Derived HMAC-SHA key for JWT operations
 */
@ConfigurationProperties(prefix = "application.token")
public record TokenProperties(
        Long ttl,
        String secret,

        SecretKey secretKey
) {

    /**
     * Constructs token configuration properties
     * with secret key derivation
     *
     * @param ttl    {@link Long} Token time-to-live in seconds
     * @param secret {@link String} Base64-encoded secret key string
     */
    @ConstructorBinding
    public TokenProperties(Long ttl, String secret) {
        this(ttl, secret, Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret)));
    }

}
