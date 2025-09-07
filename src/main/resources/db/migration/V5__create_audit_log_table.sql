-- V5__create_audit_log_table.sql
-- Creates the audit_log table to record significant events within the application.
-- This provides a historical trail of actions for monitoring and debugging purposes.

CREATE TABLE `audit_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Information about the user who performed the action.
    -- The actor can be null for system-initiated events.
    `actor_id` BIGINT NULL,
    `actor_username` VARCHAR(100) NULL COMMENT 'Denormalized username of the actor for quick lookups.',

    -- The type of action performed, stored as a string representation of the Enum.
    `action_type` VARCHAR(50) NOT NULL COMMENT 'e.g., USER_CREATED, JOB_SUBMITTED, PROJECT_DELETED',

    -- The entity that was affected by the action (the "target" of the event).
    `target_type` VARCHAR(50) NOT NULL COMMENT 'e.g., USER, PROJECT, JOB',
    `target_id` VARCHAR(255) NOT NULL COMMENT 'Identifier of the target entity (can be numeric or UUID).',
    `target_display_name` VARCHAR(255) NULL COMMENT 'A human-readable name of the target at the time of the event.',

    -- A JSON field for storing additional, action-specific metadata.
    `details` JSON NULL COMMENT 'Flexible field for extra context, e.g., changed fields.',

    -- The timestamp when the event occurred.
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Indexes for performance
    INDEX `idx_audit_log_created_at` (`created_at`),
    INDEX `idx_audit_log_target` (`target_type`, `target_id`),

    -- If the user who performed the action is deleted, we keep the audit log
    -- entry but nullify the reference to preserve history.
    FOREIGN KEY (`actor_id`) REFERENCES `user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

