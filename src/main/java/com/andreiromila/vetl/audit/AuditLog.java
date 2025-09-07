package com.andreiromila.vetl.audit;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("audit_log")
public record AuditLog(
        @Id
        Long id,

        Long actorId,
        String actorUsername,

        // Por defecto, Spring Data JDBC lo persistirá como un String
        AuditActionType actionType,
        AuditTargetType targetType,

        String targetId,
        String targetDisplayName,

        // El campo de detalles sigue siendo un String que contendrá JSON
        String details,

        @CreatedDate
        Instant createdAt

) { }
