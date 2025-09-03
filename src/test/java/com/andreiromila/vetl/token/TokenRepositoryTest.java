package com.andreiromila.vetl.token;

import com.andreiromila.vetl.AbstractDatabaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TokenRepositoryConfig.class)
public class TokenRepositoryTest extends AbstractDatabaseTest {

    @Autowired
    TokenRepository tokenRepository;

    @Test
    void shouldSaveWithGeneratedUuid() {

        // When a new token is saved
        final Token token = tokenRepository.save(
                new Token(null, "john", "Postman 9.2", true, Instant.now())
        );

        // It should have a generated uuid
        assertThat(token.uuid()).isNotNull();

    }

    @Test
    void shouldUpdateExistingToken() {

        // Given we have a token
        final Token token = tokenRepository.save(
                new Token(null, "john", "Postman 9.2", true, Instant.now())
        );

        // And modify the username
        final Token updatedToken = token.toBuilder()
                .username("updated")
                .build();

        // When we save the new token
        final Token savedToken = tokenRepository.save(updatedToken);

        // Then the stored token should have an updated username
        assertThat(savedToken.username()).isEqualTo("updated");

    }
}
