package com.andreiromila.vetl.audit;

/**
 * Enumeration of possible actions that can be recorded in the audit log.
 * Provides a type-safe way to define and reference activity types.
 */
public enum AuditActionType {

    // --- User Management Actions ---
    USER_CREATED,
    USER_ACTIVATED,
    USER_UPDATED, // Para cambios de rol, nombre, etc.
    USER_PASSWORD_CHANGED,
    USER_AVATAR_CHANGED,
    USER_DELETED,

    // --- Project Management Actions ---
    PROJECT_CREATED,
    PROJECT_UPDATED,
    PROJECT_DELETED,

    // --- Job Lifecycle Actions ---
    JOB_SUBMITTED,
    JOB_PROCESSING_STARTED,
    JOB_COMPLETED,
    JOB_FAILED,

    // --- Security Actions ---
    USER_LOGIN_SUCCESS, // Podrías añadirlo en el futuro
    USER_LOGOUT          // Podrías añadirlo en el futuro
}
