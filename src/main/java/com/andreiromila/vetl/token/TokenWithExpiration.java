package com.andreiromila.vetl.token;

import java.time.ZonedDateTime;

/**
 * Data transfer object for returning generated tokens with expiry information.
 * Contains the signed JWT and its system timezone-aware expiration timestamp.
 *
 * @param token     {@link String} Compact JWT string
 * @param expiresAt {@link ZonedDateTime} Expiration time with zone information
 */
public record TokenWithExpiration(
        String token,
        ZonedDateTime expiresAt
) { }
