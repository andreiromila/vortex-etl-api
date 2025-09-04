package com.andreiromila.vetl.storage;

import com.andreiromila.vetl.properties.MinioProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Service layer for abstracting file storage operations with MinIO.
 * It handles uploading files and generating public URLs for them.
 */
@Service
public class FileStorageService {

    /**
     * The configured MinIO client bean.
     */
    private final MinioClient minioClient;

    /**
     * The application's MinIO properties.
     */
    private final MinioProperties properties;

    /**
     * Constructs the service with the MinIO client and configuration properties.
     *
     * @param minioClient {@link MinioClient} The configured MinIO client bean.
     * @param properties  {@link MinioProperties} The application's MinIO properties.
     */
    public FileStorageService(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    /**
     * Uploads a file to the configured MinIO bucket.
     *
     * @param file {@link MultipartFile} The file to upload.
     * @return The unique object name (key) assigned to the stored file.
     * @throws RuntimeException if the upload fails.
     */
    public String uploadFile(MultipartFile file) {
        try {
            String objectName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.bucketName())
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to MinIO", e);
        }
    }

    /**
     * Generates a permanent public URL for a given object in MinIO.
     * This method assumes the bucket has a public read policy.
     *
     * @param objectName {@link String} The unique key of the object in the bucket.
     * @return The full, publicly accessible URL to the object.
     */
    public String getPublicFileUrl(String objectName) {

        // Return null if the object name is not provided
        if (objectName == null || objectName.isBlank()) {
            return null;
        }

        // Construct the URL by combining endpoint, bucket, and object name
        return String.join("/", properties.endpoint(), properties.bucketName(), objectName);
    }

}
