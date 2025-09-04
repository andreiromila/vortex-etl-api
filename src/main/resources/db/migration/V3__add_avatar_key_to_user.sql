
ALTER TABLE `user` ADD COLUMN `avatar_key` VARCHAR(255) NULL
COMMENT 'The object key/name for the user avatar stored in MinIO.';
