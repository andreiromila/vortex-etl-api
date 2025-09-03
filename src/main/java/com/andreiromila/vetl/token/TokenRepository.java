package com.andreiromila.vetl.token;

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
}
