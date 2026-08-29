package com.gradle.microservice.repository;

import com.gradle.microservice.model.ApiAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiAuditRepository extends JpaRepository<ApiAudit, Long> {
}