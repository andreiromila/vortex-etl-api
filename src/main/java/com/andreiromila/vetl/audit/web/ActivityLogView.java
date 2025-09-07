package com.andreiromila.vetl.audit.web;


import com.andreiromila.vetl.audit.AuditActionType;
import com.andreiromila.vetl.audit.AuditLog;
import com.andreiromila.vetl.audit.AuditTargetType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;

/**
 * Dto for representing a single audit
 * log entry in a user-facing activity feed.
 */
public record ActivityLogView(
        String actorUsername,

        AuditActionType actionType,
        AuditTargetType targetType,

        String targetId,
        String targetDisplayName,

        Map<String, Object> details,

        Instant createdAt
) {
    /**
     * Factory method to convert an AuditLog entity to its view representation.
     *
     * @param log The AuditLog entity from the database.
     * @return A new ActivityLogView instance.
     */
    public static ActivityLogView from(AuditLog log, ObjectMapper objectMapper) {

        Map<String, Object> detailsMap = parseDetails(log.details(), objectMapper);

        return new ActivityLogView(
                log.actorUsername() != null ? log.actorUsername() : "System",
                log.actionType(),
                log.targetType(),
                log.targetId(),
                log.targetDisplayName(),
                detailsMap,
                log.createdAt()
        );
    }

    /**
     * Parses the JSON string from the details field into a Map.
     */
    private static Map<String, Object> parseDetails(String detailsJson, ObjectMapper objectMapper) {
        if (detailsJson == null || detailsJson.isBlank()) {
            return Map.of();
        }

        try {

            // @formatter:off
            return objectMapper.readValue(detailsJson, new TypeReference<>() { });
            // @formatter:on

        } catch (JsonProcessingException e) {
            // Return a simple map with the raw string in case of a parsing error
            return Map.of("rawDetails", detailsJson);
        }
    }
}

