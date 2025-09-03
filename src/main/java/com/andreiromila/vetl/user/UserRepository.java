package com.andreiromila.vetl.user;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface UserRepository extends UserFilterRepository, ListCrudRepository<User, Long>, PagingAndSortingRepository<User, Long> {

    Optional<User> findByUsername(final String username);

    Optional<User> findByEmail(final String email);
}
