package com.andreiromila.vetl.auth;

import lombok.Builder;

@Builder
public record LoginRequest(
        String username,
        String password
) { }
