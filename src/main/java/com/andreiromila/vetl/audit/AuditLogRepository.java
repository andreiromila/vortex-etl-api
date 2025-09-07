package com.andreiromila.vetl.audit;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Repository interface for AuditLog entities.
 * Extends PagingAndSortingRepository to support pagination and sorting out of the box.
 */
public interface AuditLogRepository extends CrudRepository<AuditLog, Long>, PagingAndSortingRepository<AuditLog, Long> {
}
