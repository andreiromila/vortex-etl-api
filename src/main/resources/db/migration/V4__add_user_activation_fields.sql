
-- V4__add_user_activation_fields.sql
-- Simplifies the user table for an email-based activation workflow.

-- Make the password field nullable for pre-activation user state.
ALTER TABLE `user` MODIFY COLUMN `password` VARCHAR(255) NULL;

-- Add new columns for the activation process.
-- We use the existing 'modified_at' field to track the token's creation time.
ALTER TABLE `user`
    ADD COLUMN `email_activation_code` VARCHAR(255) NULL COMMENT 'A unique token for activating the account via email.',
    ADD COLUMN `email_validated_at` TIMESTAMP NULL COMMENT 'The timestamp when the user confirmed their email.';

-- New users are now created as disabled by default, waiting for activation.
-- We apply this AFTER adding the columns to avoid issues.
UPDATE `user` SET `enabled` = FALSE WHERE `password` IS NULL;

ALTER TABLE `user` MODIFY `enabled` BOOLEAN NOT NULL DEFAULT FALSE;
