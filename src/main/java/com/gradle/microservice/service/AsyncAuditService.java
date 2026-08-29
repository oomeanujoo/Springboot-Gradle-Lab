package com.gradle.microservice.service;

import com.gradle.microservice.model.ApiAudit;
import com.gradle.microservice.repository.ApiAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Async Audit Service - Saves audit logs without blocking main thread.
 *
 * WHY SEPARATE? Audit logging is critical but can be done asynchronously.
 * The user doesn't wait for audit save to complete.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncAuditService {

    private final ApiAuditRepository apiAuditRepository;

    @Async("asyncExecutor")
    public void saveAuditAsync(ApiAudit audit) {
        try {
            apiAuditRepository.save(audit);
            log.debug("Audit saved asynchronously with ID: {}", audit.getId());
        } catch (Exception e) {
            log.error("Failed to save audit asynchronously: {}", e.getMessage(), e);
        }
    }
}