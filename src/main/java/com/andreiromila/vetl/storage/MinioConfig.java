package com.andreiromila.vetl.storage;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the MinIO client as a Spring Bean, making it available
 * for dependency injection throughout the application.
 */
@Configuration
public class MinioConfig {

    private final MinioProperties properties;

    /**
     * Constructs the configuration with MinIO properties.
     *
     * @param properties {@link MinioProperties} The properties loaded from application.yml.
     */
    public MinioConfig(MinioProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates and configures the MinioClient bean.
     *
     * @return A fully configured {@link MinioClient} instance.
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.endpoint())
                .credentials(properties.accessKey(), properties.secretKey())
                .build();
    }

}
