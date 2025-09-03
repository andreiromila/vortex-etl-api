package com.andreiromila.vetl.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserFilterRepository {

    Page<User> search(final String query, final Pageable pageable);

}
