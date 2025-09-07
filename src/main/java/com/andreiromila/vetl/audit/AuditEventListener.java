package com.andreiromila.vetl.audit;

import com.andreiromila.vetl.user.User;
import com.andreiromila.vetl.user.event.UserCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditEventListener(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @EventListener
    public void handleUserCreatedEvent(final UserCreatedEvent event) {

        final User actor = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final User target = event.getUser();

        final String details = getRawDetails(target);

        final AuditLog log = new AuditLog(
                null,

                actor.getId(),
                actor.getUsername(),

                AuditActionType.USER_CREATED,
                AuditTargetType.USER,

                target.getId().toString(),
                target.getFullName(),

                details,

                Instant.now()
        );

        auditLogRepository.save(log);
    }

    private String getRawDetails(User target) {
        try {
            // Create a custom details node
            final ObjectNode details = objectMapper.createObjectNode();

            details.put("username", target.getUsername());
            details.put("email", target.getEmail());

            return objectMapper.writeValueAsString(details);

        } catch (JsonProcessingException e) {
            // Manejar error de serialización, aunque es poco probable
            log.error(e.getMessage(), e);
        }

        // Just return null ...
        return null;
    }

}
