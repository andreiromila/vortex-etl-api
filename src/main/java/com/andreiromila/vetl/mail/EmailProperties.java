package com.andreiromila.vetl.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Maps custom email-related properties from the application configuration file.
 * This provides a type-safe way to access configuration for the EmailService.
 *
 * @param from
 * @param fromDisplayName
 * @param logoUrl
 */
@ConfigurationProperties("spring.mail.properties.mail")
public record EmailProperties(

        // From email address
        String from,

        // From display name
        String fromDisplayName,

        // Public logo url
        String logoUrl
) {}
