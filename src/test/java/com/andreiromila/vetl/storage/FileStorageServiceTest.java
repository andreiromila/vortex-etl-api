package com.andreiromila.vetl.storage;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private MinioClient minioClient;

    private MinioProperties minioProperties;
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        minioProperties = new MinioProperties(
                "http://localhost",
                "access-key",
                "secret-key",
                "bucket-name"
        );

        fileStorageService = new FileStorageService(minioClient, minioProperties);
    }

    @Test
    void uploadFile_withUnsafeFilename_invokesSanitizationAndUploadsCleanName() throws Exception {
        // Given
        // Usamos un nombre de fichero con caracteres "ilegales"
        MockMultipartFile fileWithUnsafeChars = new MockMultipartFile(
                "avatar",
                "My Test File (1).png",
                "image/png",
                "test-data".getBytes()
        );

        // El nombre del fichero que ESPERAMOS después de sanitizar y añadir el UUID
        String expectedSanitizedPart = "my-test-file-1.png";

        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        // When
        String finalObjectName = fileStorageService.uploadFile(fileWithUnsafeChars);

        // Then
        assertThat(finalObjectName).isNotNull();

        // Verificamos que el nombre del objeto final CONTIENE la parte sanitizada
        assertThat(finalObjectName).endsWith(expectedSanitizedPart);

        // También podemos verificarlo con el captor
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());

        String capturedObjectName = captor.getValue().object();
        assertThat(capturedObjectName).isEqualTo(finalObjectName);
        assertThat(capturedObjectName).doesNotContain(" "); // No debe contener espacios
        assertThat(capturedObjectName).doesNotContain("("); // No debe contener paréntesis
    }

}