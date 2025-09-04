package com.andreiromila.vetl.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the MinIO client, loaded from application.yml.
 * This class maps all properties under the 'minio' prefix.
 *
 * @param endpoint    {@link String} The network endpoint (URL) of the MinIO server.
 * @param accessKey   {@link String} The access key (username) for MinIO authentication.
 * @param secretKey   {@link String} The secret key (password) for MinIO authentication.
 * @param bucketName  {@link String} The name of the default bucket to be used for uploads.
 */
@ConfigurationProperties(prefix = "minio")
public record MinioProperties(
        String endpoint,
        String accessKey,
        String secretKey,
        String bucketName
) { }
