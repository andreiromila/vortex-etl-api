package com.andreiromila.vetl.token;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Spring Data repository for token
 * persistence operations.
 * <p>
 * Provides basic CRUD functionality
 * and query methods for Token entities.
 *
 * @Note Uses String as ID type matching Token.uuid field
 */
public interface TokenRepository extends ListCrudRepository<Token, String>, PagingAndSortingRepository<Token, String> {

    /**
     * Finds a paginated list of tokens for a specific username.
     *
     * @param username {@link String} The username whose tokens are to be retrieved.
     * @param pageable {@link Pageable} The pagination and sorting information.
     * @return A {@link Page} of {@link Token} entities.
     */
    Page<Token> findByUsername(String username, Pageable pageable);

}
