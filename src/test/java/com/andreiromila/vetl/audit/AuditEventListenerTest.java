package com.andreiromila.vetl.audit;

import com.andreiromila.vetl.AbstractIntegrationTest;
import com.andreiromila.vetl.mail.EmailService;
import com.andreiromila.vetl.user.User;
import com.andreiromila.vetl.user.UserService;
import com.andreiromila.vetl.user.web.UserCreateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the AuditEventListener.
 * This test verifies that application events, specifically UserCreatedEvent,
 * correctly trigger the creation of audit log entries via the listener.
 * <p>
 * It operates by calling the service layer directly and then asserting
 * the state of the audit_log table.
 */
@Transactional // Hereda de AbstractIntegrationTest que ya lo tiene
public class AuditEventListenerTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @MockitoBean
    private EmailService emailService;

    private User adminActor;

    @BeforeEach
    void setup() {

        adminActor = new User();
        adminActor.setUsername("audit.admin");
        adminActor.setFullName("Audit Admin");
        adminActor.setEmail("admin@audit.com");
        adminActor.setModifiedAt(Instant.now());
        adminActor = userRepository.save(adminActor);

        final User userDetails = userService.loadUserByUsername(adminActor.getUsername());
        final Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

    }

    @AfterEach
    void tearDown() {
        // Limpiar el contexto de seguridad después de cada test.
        SecurityContextHolder.clearContext();
    }

    @Test
    void whenUserIsCreated_handleUserCreatedEvent_createsAuditLogEntryWithCorrectData() {

        // --- GIVEN ---
        final UserCreateRequest newUserRequest = new UserCreateRequest(
                "New Audited User",
                "new.audited.user",
                "audited.user@example.com",
                Set.of(3L) // Asigna el rol VIEWER (ID 3)
        );

        // --- WHEN ---
        final User createdUser = userService.createUser(newUserRequest);

        // --- THEN ---
        assertThat(auditLogRepository.count()).isEqualTo(1);

        AuditLog savedLog = auditLogRepository.findAll().iterator().next();

        // Verifica que la acción y el objetivo son correctos
        assertThat(savedLog.actionType()).isEqualTo(AuditActionType.USER_CREATED);
        assertThat(savedLog.targetType()).isEqualTo(AuditTargetType.USER);

        // Verifica que el objetivo (target) del evento es el usuario recién creado
        assertThat(savedLog.targetId()).isEqualTo(createdUser.getId().toString());
        assertThat(savedLog.targetDisplayName()).isEqualTo("New Audited User");

        // Verifica que el actor del evento es el administrador que estaba logueado
        assertThat(savedLog.actorId()).isEqualTo(adminActor.getId());
        assertThat(savedLog.actorUsername()).isEqualTo("audit.admin");

        // Verifica el resto de campos por completitud
        assertThat(savedLog.details()).isNotNull();
        assertThat(savedLog.createdAt()).isNotNull();
    }
}

