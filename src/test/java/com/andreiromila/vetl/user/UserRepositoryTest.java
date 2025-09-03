package com.andreiromila.vetl.user;

import com.andreiromila.vetl.AbstractDatabaseTest;
import com.andreiromila.vetl.role.RoleRepository;
import com.andreiromila.vetl.role.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static com.andreiromila.vetl.factories.AggregatesFactory.createUser;
import static com.andreiromila.vetl.factories.AggregatesFactory.getAdminRole;
import static org.assertj.core.api.Assertions.assertThat;

public class UserRepositoryTest extends AbstractDatabaseTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

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

        // When
        User savedUser = userRepository.save(newUser);
        userRepository.insertUserRole(savedUser.getId(), 1L);

        // Then
        final Set<UserRole> userRoles = roleRepository.findUserRoleByUsers(List.of(savedUser.getId()));
        assertThat(userRoles.iterator().next().role()).isEqualTo(1L);
    }

}
