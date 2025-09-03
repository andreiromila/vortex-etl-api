package com.andreiromila.vetl.api;

import com.andreiromila.vetl.token.TokenService;
import com.andreiromila.vetl.user.UserRepository;
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
import org.springframework.test.web.client.MockMvcClientHttpRequestFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Using @Transactional in tests it makes sure the DB is clean every time
 */
@Transactional
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    /**
     * Spring boot agent - used inside the token
     */
    public static final String SPRING_BOOT_AGENT = "SpringBoot Integration-Tests Agent";

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TokenService tokenService;

    @Autowired
    MockMvc mvc;

    protected TestRestTemplate http;

    @BeforeEach
    public void setUp() {

        http = new TestRestTemplate(
                new RestTemplateBuilder()
                        .requestFactory(() -> new MockMvcClientHttpRequestFactory(mvc))
        );

        // We must configure the StringHttpMessageConverter to use UTF-8
        // This is needed because the rest-template does a strange string conversion
        // with spanish accents and it returns a reading error.
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
}
