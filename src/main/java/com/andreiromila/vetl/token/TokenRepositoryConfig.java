package com.andreiromila.vetl.token;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;

import java.util.UUID;

import static java.util.Objects.isNull;

/**
 * Configures UUID generation for Token
 * entities before persistence.
 * <p>
 * Ensures database-agnostic UUID handling
 * for primary keys.
 */
@Configuration
public class TokenRepositoryConfig {

    /**
     * Generates UUID primary key for new Token entities.
     * Executes before entity conversion to database format.
     *
     * @return Callback that adds UUID if missing
     */
    @Bean
    public BeforeConvertCallback<Token> tokenUuidGeneratorCallback() {
        return token -> {
            if (isNull(token.uuid())) {
                return token.toBuilder()
                        .uuid(UUID.randomUUID().toString())
                        .build();
            }

            return token;
        };
    }

}
