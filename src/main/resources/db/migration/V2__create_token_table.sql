
-- Tabla `token`
CREATE TABLE `token` (
    uuid CHAR(36) NOT NULL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    user_agent VARCHAR(1000) NOT NULL,
    enable BOOLEAN NOT NULL DEFAULT true,
    expires_at TIMESTAMP NOT NULL,
    INDEX idx_token_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
