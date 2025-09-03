package com.andreiromila.vetl.token;

import com.andreiromila.vetl.AbstractDatabaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@Import({TokenService.class, TokenRepositoryConfig.class})
@EnableConfigurationProperties(TokenProperties.class)
public class TokenServiceTest extends AbstractDatabaseTest {

    @Autowired
    TokenService tokenService;

    @Autowired
    TokenRepository tokenRepository;

    @Test
    void createToken_returnsCompactedTokenWithExpirationTimestamp() {

        TokenWithExpiration response = tokenService.createToken("john", "Postman 9.2");

        assertThat(response.expiresAt()).isAfter(ZonedDateTime.now());
        assertThat(tokenRepository.count()).isEqualTo(1);

    }

    @Test
    void extractUsername_withValidToken_returnsUsername() {

        TokenWithExpiration token = tokenService.createToken("john", "Postman 9.2");

        final String username = tokenService.extractUsername(token.token(), "Postman 9.2");

        assertThat(username).isEqualTo("john");

    }

    @Test
    void extractUsername_withNonExistentToken_throwsIllegalArgumentException() {

        // Given we have a valid token
        TokenWithExpiration token = tokenService.createToken("john", "Postman 9.2");

        // And no data in the database
        tokenRepository.deleteAll();

        // When we try to extract the username we should get an exception
        assertThatThrownBy(() -> tokenService.extractUsername(token.token(), "Postman 9.2"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("No token was found with id:");
    }

    @Test
    void extractUsername_withDisabledToken_throwsIllegalArgumentException() {

        // Given we have a valid token
        final TokenWithExpiration compacted = tokenService.createToken("john", "Postman 9.2");
        final Token token = tokenRepository.findAll().getFirst();

        // And we disable it
        tokenRepository.save(
                token.toBuilder().enable(false).build()
        );

        // When we try to extract the username we should get an exception
        assertThatThrownBy(() -> tokenService.extractUsername(compacted.token(), "Postman 9.2"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The provided token has been disabled by the user or a system administrator.");
    }


    @Test
    void extractUsername_withInvalidUserAgent_throwsIllegalArgumentException() {

        // Given we have a valid token
        TokenWithExpiration token = tokenService.createToken("john", "Postman 9.2");

        // When we try to extract the username we should get an exception
        assertThatThrownBy(() -> tokenService.extractUsername(token.token(), "Invalid User Agent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Invalid User-Agent");
    }
}
