package com.andreiromila.vetl.audit.web;

import com.andreiromila.vetl.audit.AuditLog;
import com.andreiromila.vetl.audit.AuditLogService;
import com.andreiromila.vetl.responses.CustomPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import static com.andreiromila.vetl.utils.PageableUtils.getPageableWithSafeSort;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * REST controller for retrieving the application-wide activity feed.
 */
@RestController
@RequestMapping("/api/v1/activity")
public class ActivityController {

    /**
     * The allowed sorting columns for the user table
     */
    public static final Set<String> SORTING_COLUMNS = Set.of("createdAt", "actorUsername", "actionType");

    private final AuditLogService auditService;
    private final ObjectMapper objectMapper;

    public ActivityController(AuditLogService auditService, ObjectMapper objectMapper) {
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    /**
     * Retrieves a paginated list of recent activity logs for the dashboard.
     * Accessible by any authenticated user. The result is sorted by creation date descending.
     *
     * @param pageable Provides pagination info. Defaults to the first 5 entries.
     * @return A ResponseEntity containing a Page of ActivityLogView objects.
     */
    @GetMapping
    public ResponseEntity<CustomPage<ActivityLogView>> getRecentActivity(@PageableDefault(size = 5) Pageable pageable) {

        // Let's make sure the user doesn't try something else
        final Pageable safePageable = getPageableWithSafeSort(pageable, SORTING_COLUMNS, Sort.by(DESC, "createdAt"));

        // Get the page
        final Page<AuditLog> auditLogPage = auditService.findRecentActivity(safePageable);

        // Map the data to the view
        final List<ActivityLogView> activityViews = auditLogPage.getContent().stream()
                .map(it -> ActivityLogView.from(it, objectMapper))
                .toList();

        // Create the custom page
        final CustomPage<ActivityLogView> response = new CustomPage<>(
                activityViews,
                auditLogPage.getPageable(),
                auditLogPage.getTotalElements()
        );

        return ResponseEntity.ok(response);
    }

}
