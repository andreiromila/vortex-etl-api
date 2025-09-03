package com.andreiromila.vetl.auth;

import java.time.ZonedDateTime;

public record LoginResponse(
        String token,
        ZonedDateTime expiresAt
) { }