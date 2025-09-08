package com.andreiromila.vetl.auth;

import java.time.ZonedDateTime;

public record LoginResponse(
        String tokenUuid,
        String token,
        ZonedDateTime expiresAt
) { }