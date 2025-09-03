package com.andreiromila.vetl.user;

import com.andreiromila.vetl.AbstractDatabaseTest;
import com.andreiromila.vetl.role.RoleRepository;
import com.andreiromila.vetl.user.web.UserCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

        UserCreateRequest request = new UserCreateRequest("John Doe.", "john", "john@example.com", "Pa$$w0rd!");

        User createdUser = userService.createUser(request);

        // The user also must have an id from the database
        assertThat(createdUser.getId()).isNotNull();
    }

    @Test
    void createUser_withValidData_encryptsPassword() {

        String password = "Pa$$w0rd!";

        UserCreateRequest request = new UserCreateRequest("John Doe.", "john", "john@example.com", password);

        User createdUser = userService.createUser(request);
        User persistedUser = userRepository.findById(createdUser.getId()).orElseThrow();

        assertThat(persistedUser.getPassword())
                .isNotEqualTo(password)
                .startsWith("$2a$")
                .satisfies(encodedPassword ->
                        assertThat(bcrypt.matches(password, encodedPassword))
                                .as("Password verification with BCrypt")
                                .isTrue());
    }
}
