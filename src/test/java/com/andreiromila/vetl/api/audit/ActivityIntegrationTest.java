package com.andreiromila.vetl.api.audit;

import com.andreiromila.vetl.AbstractIntegrationTest;
import com.andreiromila.vetl.audit.AuditActionType;
import com.andreiromila.vetl.audit.AuditLog;
import com.andreiromila.vetl.audit.AuditLogRepository;
import com.andreiromila.vetl.audit.AuditTargetType;
import com.andreiromila.vetl.audit.web.ActivityLogView;
import com.andreiromila.vetl.responses.CustomPage;
import com.andreiromila.vetl.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ActivityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    AuditLogRepository auditLogRepository;

    User testUser;

    @BeforeEach
    void setupData() {

        // Crea un usuario para usar como actor en los logs
        testUser = loginAdmin("test.actor");

        // Crea 10 registros de log para probar la paginación
        IntStream.rangeClosed(1, 10)
                .mapToObj(i -> new AuditLog(
                        null,
                        testUser.getId(),
                        testUser.getUsername(),
                        AuditActionType.USER_CREATED,
                        AuditTargetType.USER,
                        "target" + i,
                        "Target User " + i,
                        """
                        {
                            "username": "target%s",
                            "email": "target%s@email.com"
                        }
                        """.formatted(i, i),
                        Instant.now().plusSeconds(i * 60L) // Fechas incrementales
                ))
                .forEach(auditLogRepository::save);
    }

    @Test
    void getRecentActivity_asGuest_returnsHttp401Unauthorized() {

        http.getRestTemplate().getInterceptors().clear();

        // When
        ResponseEntity<Void> response = http.getForEntity("/api/v1/activity", Void.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getRecentActivity_asAuthenticatedUser_returnsDefaultFirstPageOfActivities() {
        // When se pide la actividad sin parámetros
        ResponseEntity<CustomPage<ActivityLogView>> response = http.exchange(
                "/api/v1/activity",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then la respuesta es correcta
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CustomPage<ActivityLogView> page = response.getBody();
        assertThat(page).isNotNull();

        // Verifica los parámetros por defecto de @PageableDefault
        assertThat(page.size()).isEqualTo(5);
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.content()).hasSize(5);
        assertThat(page.totalElements()).isEqualTo(10);
        assertThat(page.totalPages()).isEqualTo(2);

        // Verifica que esté ordenado por fecha de creación descendente
        List<ActivityLogView> content = page.content();
        assertThat(content).isSortedAccordingTo(Comparator.comparing(ActivityLogView::createdAt).reversed());
    }

    @Test
    void getRecentActivity_withCustomPagination_returnsCorrectPage() {
        // When se pide una página y tamaño específicos
        ResponseEntity<CustomPage<ActivityLogView>> response = http.exchange(
                "/api/v1/activity?page=2&size=3",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CustomPage<ActivityLogView> page = response.getBody();
        assertThat(page).isNotNull();

        // Verifica la paginación personalizada
        assertThat(page.size()).isEqualTo(3);
        assertThat(page.page()).isEqualTo(2);
        assertThat(page.content()).hasSize(3);
        assertThat(page.totalElements()).isEqualTo(10);
        assertThat(page.totalPages()).isEqualTo(4); // 10 elementos / 3 por página = 3.33 -> 4 páginas
    }

    @Test
    void getRecentActivity_withUnsafeSortParameter_ignoresItAndUsesDefaultSort() {
        // When se intenta ordenar por una columna no permitida
        ResponseEntity<CustomPage<ActivityLogView>> response = http.exchange(
                "/api/v1/activity?sort=details,asc",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Then la respuesta es correcta
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CustomPage<ActivityLogView> page = response.getBody();
        assertThat(page).isNotNull();

        // Y los datos siguen ordenados por el criterio por defecto (createdAt, DESC)
        List<ActivityLogView> content = page.content();
        assertThat(content).isSortedAccordingTo(Comparator.comparing(ActivityLogView::createdAt).reversed());
    }
}
