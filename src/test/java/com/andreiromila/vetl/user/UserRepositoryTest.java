package com.andreiromila.vetl.user;

import com.andreiromila.vetl.AbstractDatabaseTest;
import com.andreiromila.vetl.role.RoleReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static com.andreiromila.vetl.factories.AggregatesFactory.createUser;
import static org.assertj.core.api.Assertions.assertThat;

public class UserRepositoryTest extends AbstractDatabaseTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void shouldSaveUserAndFetchByUsername() {
        // Given
        User newUser = createUser();

        // When
        userRepository.save(newUser);

        // Then
        User foundUser = userRepository.findByUsername(newUser.getUsername()).orElseThrow();
        assertThat(foundUser.getFullName()).isEqualTo(newUser.getFullName());
    }

    @Test
    void shouldSaveUserWithRoles() {
        // Given
        User newUser = createUser();

        // Asigna el rol 'ADMIN' (ID = 1, según la migración V1)
        newUser.setRoles(Set.of(new RoleReference(1L)));

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        User foundUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(foundUser.getRoles()).hasSize(1);
        assertThat(foundUser.getRoles().iterator().next().role()).isEqualTo(1L);
    }

}
