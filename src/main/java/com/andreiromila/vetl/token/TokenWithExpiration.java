package com.andreiromila.vetl.token;

import java.time.ZonedDateTime;

/**
 * Data transfer object for returning generated tokens with expiry information.
 * Contains the signed JWT and its system timezone-aware expiration timestamp.
 *
 * @param tokenUuid {@link String} The uuid of the stored token
 * @param token     {@link String} Compact JWT string
 * @param expiresAt {@link ZonedDateTime} Expiration time with zone information
 */
public record TokenWithExpiration(
        String tokenUuid,
        String token,
        ZonedDateTime expiresAt
) { }
