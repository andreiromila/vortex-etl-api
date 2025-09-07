package com.andreiromila.vetl.audit;

/**
 * Enumeration of possible target entity types for an audit log entry.
 * Defines the nature of the resource that was acted upon.
 */
public enum AuditTargetType {

    USER,
    PROJECT,
    JOB,
    ROLE,    // Ej: Si añades un CRUD para roles
    SYSTEM   // Para acciones generales del sistema sin un target específico
}
