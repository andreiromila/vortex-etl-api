package com.andreiromila.vetl.user;

import com.andreiromila.vetl.AbstractDatabaseTest;
import com.andreiromila.vetl.role.RoleRepository;
import com.andreiromila.vetl.storage.FileStorageService;
import com.andreiromila.vetl.user.web.UserCreateRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Set;

import static com.andreiromila.vetl.factories.AggregatesFactory.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserServiceTest extends AbstractDatabaseTest {

    static final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @MockitoBean
    FileStorageService storageService;

    UserService userService;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, bcrypt, storageService, eventPublisher);
    }

    @Test
    void loadUserByUsername_withExistingUser_returnsUserFromDatabase() {

        // Given we have a user into the database
        User john = userRepository.save(createUser("john"));

        User loadedUser = userService.loadUserByUsername("john");

        assertThat(loadedUser).isNotNull();
        assertThat(loadedUser.getId()).isEqualTo(john.getId());
    }

    @Test
    void loadUserByUsername_withInvalidUsername_throwsException() {

        assertThatThrownBy(() -> userService.loadUserByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("No user found with username: nonexistent");

    }

    @Test
    void createUser_withValidData_autoGeneratesUserIdentifier() {

        UserCreateRequest request = getUserCreateRequest();

        User createdUser = userService.createUser(request);

        // The user also must have an id from the database
        assertThat(createdUser.getId()).isNotNull();
    }

    private static @NotNull UserCreateRequest getUserCreateRequest() {
        return new UserCreateRequest("John Doe.", "john", "john@example.com", Set.of(3L));
    }
}
