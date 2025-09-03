package com.andreiromila.vetl.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;

/**
 * Service handling JWT token lifecycle
 * management and validation.
 * <p>
 * Integrates with persistence layer
 * for token revocation tracking.
 */
@Service
public class TokenService {

    /**
     * Token persistence component
     */
    private final TokenRepository repository;

    /**
     * Token configuration parameters
     */
    private final TokenProperties properties;

    /**
     * Constructs TokenService with required dependencies
     *
     * @param repository {@link TokenRepository} Token persistence component
     * @param properties {@link TokenProperties} Token configuration parameters
     */
    public TokenService(final TokenRepository repository, final TokenProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    /**
     * Persists token metadata in database before JWT generation
     *
     * @param username   {@link String} Authenticated username
     * @param userAgent  {@link String} Client's User-Agent header
     * @param expiration {@link Instant} Token expiration timestamp
     * @return {@link Token} Created token entity with generated UUID
     */
    private Token save(final String username, final String userAgent, final Instant expiration) {
        final Token token = Token.builder()
                .username(username)
                .userAgent(userAgent)
                .expiresAt(expiration)
                .enable(true)
                .build();

        return repository.save(token);
    }

    /**
     * Generates JWT token and stores associated metadata
     *
     * @param username  {@link String} Authenticated username
     * @param userAgent {@link String} Client's User-Agent header
     * @return {@link TokenWithExpiration} Generated JWT with expiration info
     */
    public TokenWithExpiration createToken(String username, String userAgent) {
        // Get the expiration date
        final Date expiration = new Date(System.currentTimeMillis() + 1000 * properties.ttl());

        // Before we create the user, we must store the new Token ID into the database
        // What if the token was stolen? It is mandatory to block the request
        final Token token = save(username, userAgent, expiration.toInstant());

        // Create the token
        final String compactToken = Jwts.builder()
                .claims(new HashMap<>())
                .id(token.uuid())
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expiration)
                .signWith(properties.secretKey())
                .compact();

        // Create the expiration timestamp
        final ZonedDateTime expiresAt = ZonedDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault());

        // Return the login response with the token
        return new TokenWithExpiration(compactToken, expiresAt);
    }

    /**
     * Parses and validates JWT claims
     *
     * @param token {@link String} JWT token to validate
     * @return {@link Claims} Verified token claims
     * @throws io.jsonwebtoken.JwtException If token validation fails
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(properties.secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validates JWT token and extracts username
     *
     * @param bearerToken {@link String} JWT token from Authorization header
     * @param userAgent   {@link String} Current request's User-Agent
     * @return {@link String} Authenticated username
     * @throws IllegalArgumentException For invalid/revoked tokens
     */
    public String extractUsername(final String bearerToken, final String userAgent) {
        // Validate signature and extract all claims
        final Claims claims = extractClaims(bearerToken);
        final String uuid = claims.getId();

        // Get the token from the database
        final Token token = repository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("No token was found with id: %s".formatted(uuid)));

        if ( ! token.enable()) {
            throw new IllegalArgumentException("The provided token has been disabled by the user or a system administrator.");
        }

        if (!token.userAgent().equals(userAgent)) {
            final String message = "Invalid User-Agent expected [%s] but received [%s].".formatted(token.userAgent(), userAgent);
            throw new IllegalArgumentException(message);
        }

        return claims.getSubject();
    }

    /**
     * Invalidates a valid token
     *
     * @param bearerToken {@link String} The token to disable
     */
    public void invalidate(final String bearerToken) {

        // Validate signature and extract all claims
        final Claims claims = extractClaims(bearerToken);
        final String uuid = claims.getId();

        // Get the token from the database
        final Token token = repository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("No token was found with id: %s".formatted(uuid)));

        // Disable the current token
        repository.save(
                token.toBuilder()
                        .enable(false)
                        .build()
        );
    }

    public Page<Token> findAll(final Pageable pageable) {
        return repository.findAll(pageable);
    }
}
