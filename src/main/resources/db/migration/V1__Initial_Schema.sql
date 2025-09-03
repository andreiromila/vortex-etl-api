-- V1__Initial_Schema.sql
-- Creación de la estructura inicial para la gestión de usuarios y roles.

-- Tabla de Roles:
-- Almacena los roles disponibles en el sistema.
CREATE TABLE `role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(255) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Tabla de Usuarios:
-- Almacena la información de los usuarios del sistema.
CREATE TABLE `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(100) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `full_name` VARCHAR(255) NOT NULL,
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `modified_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Tabla de Unión User-Role (muchos a muchos):
-- Relaciona a los usuarios con sus roles.
CREATE TABLE `user_role` (
    `user` BIGINT NOT NULL,
    `role` BIGINT NOT NULL,
    PRIMARY KEY (`user`, `role`),
    FOREIGN KEY (`user`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`role`) REFERENCES `role`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Populado Inicial de Datos (Seed Data) para la tabla de Roles
INSERT INTO `role` (`id`, `name`, `description`) VALUES
    (1, 'ADMIN', 'Grants full administrative access. Can manage users, roles, projects, and system settings.'),
    (2, 'EDITOR', 'Allows creating, editing, and submitting jobs within assigned projects. Cannot manage users.'),
    (3, 'VIEWER', 'Provides read-only access to view projects and job statuses. Cannot perform any write actions.');
