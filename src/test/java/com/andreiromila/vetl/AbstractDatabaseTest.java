package com.andreiromila.vetl;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@DataJdbcTest
@Testcontainers
public abstract class AbstractDatabaseTest {

    static final MySQLContainer<?> mySqlContainer =
            new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                    .withReuse(true);

    @BeforeAll
    static void beforeAll() {
        mySqlContainer.start();
    }

    // Inyecta dinámicamente las propiedades de la BBDD del contenedor en Spring
    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySqlContainer::getUsername);
        registry.add("spring.datasource.password", mySqlContainer::getPassword);
        // Hacemos que Flyway se ejecute contra este contenedor
        registry.add("spring.flyway.url", mySqlContainer::getJdbcUrl);
        registry.add("spring.flyway.user", mySqlContainer::getUsername);
        registry.add("spring.flyway.password", mySqlContainer::getPassword);
    }
}
