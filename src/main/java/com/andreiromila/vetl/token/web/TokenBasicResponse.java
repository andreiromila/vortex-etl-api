package com.andreiromila.vetl.token.web;

import com.andreiromila.vetl.token.Token;

import java.time.Instant;

public record TokenBasicResponse(
        String uuid,

        String username,

        String userAgent,

        boolean enable,

        Instant expiresAt
) {

    /**
     * Factory method to convert a {@link Token} entity to a {@link TokenBasicResponse}.
     *
     * @param token {@link Token} Entity to transform.
     * @return Dto containing non-sensitive token data.
     */
    public static TokenBasicResponse from(final Token token) {
        return new TokenBasicResponse(
                token.uuid(),
                token.username(),
                token.userAgent(),
                token.enable(),
                token.expiresAt()
        );
    }

}
