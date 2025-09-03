package com.andreiromila.vetl.user;

import com.andreiromila.vetl.AbstractDatabaseTest;
import com.andreiromila.vetl.role.RoleRepository;
import com.andreiromila.vetl.user.web.UserCreateRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

    UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, bcrypt);
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

    @Test
    void createUser_withValidData_encryptsPassword() {

        UserCreateRequest request = getUserCreateRequest();

        User createdUser = userService.createUser(request);
        User persistedUser = userRepository.findById(createdUser.getId()).orElseThrow();

        assertThat(persistedUser.getPassword())
                .isNotEqualTo("Pa$$w0rd!")
                .startsWith("$2a$")
                .satisfies(encodedPassword ->
                        assertThat(bcrypt.matches("Pa$$w0rd!", encodedPassword))
                                .as("Password verification with BCrypt")
                                .isTrue());
    }

    private static @NotNull UserCreateRequest getUserCreateRequest() {
        return new UserCreateRequest("John Doe.", "john", "john@example.com", "Pa$$w0rd!", Set.of(3L));
    }
}
