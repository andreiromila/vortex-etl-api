package com.andreiromila.vetl.api.user;

import com.andreiromila.vetl.api.AbstractIntegrationTest;
import com.andreiromila.vetl.responses.CustomPage;
import com.andreiromila.vetl.user.web.UserBasicResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.andreiromila.vetl.factories.AggregatesFactory.createUser;
import static com.andreiromila.vetl.factories.AggregatesFactory.createUsers;
import static org.assertj.core.api.Assertions.assertThat;

public class UserSearchIntegrationTest extends AbstractIntegrationTest {

    @Test
    void listUsers_withValidToken_returnsFirstPageOfUsers() {

        // Given we have 3 users in the database
        userRepository.saveAll(List.of(
                createUser("robert"),
                createUser("jane"),
                createUser("billy")
        ));

        // And a logged-in user "John"
        loginAdmin("john");

        // When John accesses the user-list endpoint
        final ResponseEntity<CustomPage<UserBasicResponse>> response = http.exchange(
                "/api/v1/users", HttpMethod.GET, new HttpEntity<>(""), new ParameterizedTypeReference<>() {
                }
        );

        // Should receive 200 ok
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // The total should be 4
        assertThat(response.getBody())
                .isNotNull()
                .satisfies(body -> {
                    assertThat(body.totalElements()).isEqualTo(4);
                    assertThat(body.content()).hasSize(4);
                });

    }


    @Test
    void listUsers_returnsCustomPageStructure() {

        // Given we have a verified use that is logged in
        loginAdmin("john");

        // And 25 users in the database (with John are 25)
        userRepository.saveAll(createUsers(24));

        // When John accesses the first page
        final ResponseEntity<CustomPage<UserBasicResponse>> response = http.exchange(
                "/api/v1/users?query=&size=10&page=1", HttpMethod.GET, new HttpEntity<>(""), new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getBody())
                // Then the response should have pagination data
                .isNotNull()
                .satisfies(body -> {

                    // The number of Users per page should be 10
                    assertThat(body.content()).hasSize(10);

                    // The page should be 1
                    assertThat(body.page()).isOne();

                    // Total pages -> 3 (10 users per page)
                    assertThat(body.totalPages()).isEqualTo(3);

                    // The total elements should be 25
                    assertThat(body.totalElements()).isEqualTo(25);

                    // And the size should be 10
                    assertThat(body.size()).isEqualTo(10);
                });

    }

    @Test
    void listUsers_withUsernameFilter_returnsFilteredPage() {

        // Given we have a verified user that is logged in
        loginAdmin("john");

        // And 3 more users
        userRepository.save(createUser("tech.guru"));
        userRepository.save(createUser("technical.user"));
        userRepository.save(createUser("technical.coder"));

        // When John searches for "tech" should receive a page of 3 results
        final ResponseEntity<CustomPage<UserBasicResponse>> response = http.exchange(
                "/api/v1/users?query=tECh", HttpMethod.GET, new HttpEntity<>(""), new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Total results 3 and only one page (without John)
        assertThat(response.getBody().totalElements()).isEqualTo(3);
        assertThat(response.getBody().totalPages()).isEqualTo(1);

    }

    @Test
    void listUser_withInvalidSortColumn_returnsResultsIgnoringTheValues() {

        // Given we have a verified user that is logged in
        loginAdmin("john");

        // And 3 more users
        userRepository.save(createUser("b_second"));
        userRepository.save(createUser("a_first"));
        userRepository.save(createUser("c_third"));

        // When John lists the users ordering by username and by unknown, receives the list sorted by username
        final ResponseEntity<CustomPage<UserBasicResponse>> response = http.exchange(
                "/api/v1/users?sort=username,asc&sort=unknown,desc", HttpMethod.GET, new HttpEntity<>(""), new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Total results 4
        assertThat(response.getBody().totalElements()).isEqualTo(4);

        // With a specific order
        assertThat(response.getBody().content().get(0).username()).isEqualTo("a_first");
        assertThat(response.getBody().content().get(1).username()).isEqualTo("b_second");
        assertThat(response.getBody().content().get(2).username()).isEqualTo("c_third");
        assertThat(response.getBody().content().get(3).username()).isEqualTo("john");
    }

}
