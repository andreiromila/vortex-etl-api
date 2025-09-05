package com.andreiromila.vetl.api;

import com.andreiromila.vetl.token.TokenService;
import com.andreiromila.vetl.token.TokenWithExpiration;
import com.andreiromila.vetl.user.User;
import com.andreiromila.vetl.user.UserRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.client.MockMvcClientHttpRequestFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.andreiromila.vetl.factories.AggregatesFactory.createUser;

/**
 * Using @Transactional in tests it makes sure the DB is clean every time
 */
@Transactional
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    /**
     * Spring boot agent - used inside the token
     */
    public static final String SPRING_BOOT_AGENT = "SpringBoot Integration-Tests Agent";

    protected static MySQLContainer<?> mySqlContainer =
            new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                    .withReuse(true);

    protected static MinIOContainer minioContainer =
            new MinIOContainer("minio/minio:RELEASE.2025-07-23T15-54-02Z-cpuv1")
                    .withReuse(true);

    @BeforeAll
    static void beforeAll() throws Exception {
        mySqlContainer.start();
        minioContainer.start();

        // --- Lógica para crear el bucket público ---
        // Necesitamos crear el bucket 'vortex-avatars' y hacerlo público
        // para que las pruebas de integración funcionen correctamente.
        MinioClient setupClient = MinioClient.builder()
                .endpoint(minioContainer.getS3URL())
                .credentials(minioContainer.getUserName(), minioContainer.getPassword())
                .build();

        String bucketName = "vortex-avatars";
        // 1. Comprueba si el bucket ya existe
        boolean bucketExists = setupClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

        // 2. Si NO existe, créalo.
        if ( ! bucketExists) {
            setupClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

            // La política para hacer el bucket público
            String publicPolicy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]}]}";
            setupClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(publicPolicy).build());
        }

    }

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {

        // Database config
        registry.add("spring.datasource.url", mySqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySqlContainer::getUsername);
        registry.add("spring.datasource.password", mySqlContainer::getPassword);

        // Flyway
        registry.add("spring.flyway.url", mySqlContainer::getJdbcUrl);
        registry.add("spring.flyway.user", mySqlContainer::getUsername);
        registry.add("spring.flyway.password", mySqlContainer::getPassword);

        // MinIO config
        // Inyectamos dinámicamente la configuración del contenedor MinIO
        // en el contexto de Spring para que la aplicación se conecte a él.
        registry.add("minio.endpoint", minioContainer::getS3URL);
        registry.add("minio.access-key", minioContainer::getUserName);
        registry.add("minio.secret-key", minioContainer::getPassword);
        registry.add("minio.bucket-name", () -> "vortex-avatars");
    }

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TokenService tokenService;

    @Autowired
    protected MockMvc mvc;

    protected TestRestTemplate http;

    @BeforeEach
    public void setUp() {

        http = new TestRestTemplate(
                new RestTemplateBuilder()
                        .requestFactory(() -> new MockMvcClientHttpRequestFactory(mvc))
        );

        // We must configure the StringHttpMessageConverter to use UTF-8
        // This is needed because the rest-template does a strange string conversion
        // with spanish accents, and it returns a reading error.
        http.getRestTemplate().getMessageConverters()
                .stream()
                .filter(converter -> converter instanceof StringHttpMessageConverter)
                .forEach(converter -> ((StringHttpMessageConverter) converter).setDefaultCharset(StandardCharsets.UTF_8));

        // Clear all interceptors, it looks like every test use the same RestTemplate
        // and if the test before already added an Authorization token header then
        // the current test has it too, we must remove it
        http.getRestTemplate()
                .getInterceptors()
                .clear();

        final List<ClientHttpRequestInterceptor> interceptors = List.of(
                AbstractIntegrationTest::acceptJsonInterceptor,
                AbstractIntegrationTest::contentTypeJsonInterceptor,
                AbstractIntegrationTest::userAgentInterceptor
        );

        http.getRestTemplate()
                .getInterceptors()
                .addAll(interceptors);
    }

    protected static ClientHttpResponse userAgentInterceptor(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().set(HttpHeaders.USER_AGENT, SPRING_BOOT_AGENT);
        return execution.execute(request, body);
    }

    protected static ClientHttpResponse acceptJsonInterceptor(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().setAccept(List.of(MediaType.APPLICATION_JSON));
        return execution.execute(request, body);
    }

    protected static ClientHttpResponse contentTypeJsonInterceptor(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return execution.execute(request, body);
    }

    protected void addAuthorizationHeader(String token) {
        http.getRestTemplate()
                .getInterceptors()
                .add((request, body, execution) -> {
                    request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                    return execution.execute(request, body);
                });
    }

    protected User loginViewer(String username) {
        return loginWithRole(username, 3L);
    }

    protected User loginEditor(String username) {
        return loginWithRole(username, 2L);
    }

    protected User loginAdmin(String username) {
        return loginWithRole(username, 1L);
    }

    protected User loginWithRole(String username, Long... roleIds) {
        User user = createUser(username);
        User authenticatedUser = userRepository.save(user);

        if (roleIds != null) {
            for (Long roleId : roleIds) {
                userRepository.insertUserRole(authenticatedUser.getId(), roleId);
            }
        }

        final TokenWithExpiration tokenWithExpiration = tokenService.createToken(username, SPRING_BOOT_AGENT);
        addAuthorizationHeader(tokenWithExpiration.token());
        return authenticatedUser;
    }

}
