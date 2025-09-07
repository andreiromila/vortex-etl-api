package com.andreiromila.vetl.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for querying and managing audit log data.
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(final AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Retrieves a paginated list of the most recent audit log entries.
     * @param pageable Pagination information (should be sorted by createdAt descending).
     * @return A Page of AuditLog entities.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> findRecentActivity(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

}
