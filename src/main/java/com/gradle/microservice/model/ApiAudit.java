package com.gradle.microservice.model;

import jakarta.persistence.*;
import lombok.*;

/*
 * ============================================================
 * API AUDIT ENTITY - NO AUDITING
 * ============================================================
 *
 * WHAT THIS FILE DOES:
 * --------------------
 * Logs every external API call made by the application
 * Stores request/response payloads, status codes, errors, response times
 *
 * WHY THIS IS IMPORTANT:
 * ----------------------
 * - Debugging: See exactly what was sent/received
 * - Compliance: Required for SLA monitoring and legal requirements
 * - Performance: Track response_time_ms to monitor API latency
 * - Troubleshooting: When something fails, we have full request/response
 *
 * WHY NO @Audited HERE:
 * ---------------------
 * This table itself stores audit logs (it IS the audit)
 * Auditing an audit table would be redundant and waste space
 * Also contains large TEXT fields (payloads) that don't need change tracking
 *
 * CREATED BY: Team
 * LAST UPDATED: 2026-05-23
 * ============================================================
 */

@Entity
@Table(name = "api_audit")
// NO @Audited here - This table IS the audit, doesn't need change tracking
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiAudit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_name")
    private String apiName;  // Which API was called (e.g., "OPEN_WEATHER")

    @Column(name = "request_payload", columnDefinition = "TEXT")
    private String requestPayload;  // What we sent to external API

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;  // What external API returned

    @Column(name = "request_headers", columnDefinition = "TEXT")
    private String requestHeaders;  // Headers sent (for debugging)

    @Column(name = "response_code")
    private Integer responseCode;  // HTTP status code (200, 404, 500, etc.)

    private String status;  // "SUCCESS", "FAILED", "TIMEOUT"

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;  // If failed, why?

    @Column(name = "external_trace_id")
    private String externalTraceId;  // For distributed tracing across systems

    @Column(name = "response_time_ms")
    private Long responseTimeMs;  // How long the API call took (performance monitoring)
}