package com.andreiromila.vetl.role;

import com.andreiromila.vetl.AbstractDatabaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class RoleRepositoryTest extends AbstractDatabaseTest {

    @Autowired
    RoleRepository roleRepository;

    @Test
    void whenApplicationStarts_shouldLoadInitialRoles() {

        // Verificamos que los 3 roles han sido cargados
        long roleCount = roleRepository.count();
        assertThat(roleCount).isEqualTo(3L);

        // Verificamos que podemos encontrar un rol específico
        Role adminRole = roleRepository.findById(1L).orElseThrow();
        assertThat(adminRole.name()).isEqualTo("ADMIN");
    }

}
