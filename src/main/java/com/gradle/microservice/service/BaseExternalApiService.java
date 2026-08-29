package com.gradle.microservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.microservice.model.ApiAudit;
import com.gradle.microservice.repository.ApiAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/*
 * ============================================================
 * BASE EXTERNAL API SERVICE - AUDIT ENFORCEMENT
 * ============================================================
 *
 * WHAT THIS FILE DOES:
 * --------------------
 * Abstract class that ALL external API services MUST extend
 * Enforces automatic audit logging for every API call
 *
 * WHY ABSTRACT CLASS? (Not Interface)
 * ------------------------------------
 * - Can hold shared state (audit repository, object mapper)
 * - Can provide concrete methods with common logic
 * - Child classes automatically get audit capability
 * - Template Method Pattern: Defines skeleton, child provides details
 *
 * HOW TO USE:
 * -----------
 * 1. Extend this class: public class WeatherService extends BaseExternalApiService
 * 2. Implement executeApiCall() method with actual API logic
 * 3. Call executeWithAudit() instead of calling API directly
 * 4. Audit happens automatically!
 *
 * ENFORCED AUDIT FIELDS:
 * ----------------------
 * - apiName: Must be provided by child class
 * - requestPayload: Auto-captured from input
 * - responsePayload: Auto-captured from output
 * - responseCode: Auto-captured from HTTP status
 * - status: SUCCESS/FAILED/TIMEOUT
 * - responseTimeMs: Auto-calculated
 * - errorMessage: Auto-captured on failure
 *
 * CREATED BY: Anuj
 * LAST UPDATED: 2026-05-23
 * ============================================================
 */

@Slf4j
@Service
@RequiredArgsConstructor
public abstract class BaseExternalApiService {

    // Shared dependencies - all child services will have access
    protected final ApiAuditRepository apiAuditRepository;
    protected final ObjectMapper objectMapper;

    /*
     * ============================================================
     * TEMPLATE METHOD - Executes API call with automatic audit
     * ============================================================
     *
     * WHY THIS PATTERN?
     * - Child classes don't need to write audit code
     * - Audit is guaranteed (can't be forgotten)
     * - Consistent format across all external calls
     *
     * @param apiName - Name of external API (e.g., "OPEN_WEATHER", "GOOGLE_MAPS")
     * @param request - Request object (will be JSON-serialized for audit)
     * @param apiCall - Functional interface that makes the actual API call
     * @return Response from external API
     */
    protected <T, R> R executeWithAudit(String apiName, T request, ApiCall<T, R> apiCall) {

        // ========== STEP 1: Initialize audit record ==========
        long startTime = System.currentTimeMillis();
        ApiAudit audit = new ApiAudit();
        audit.setApiName(apiName);
        audit.setStatus("INITIATED");
        audit.setCreatedOn(LocalDateTime.now());

        // ========== STEP 2: Capture request payload for audit ==========
        try {
            String requestJson = objectMapper.writeValueAsString(request);
            audit.setRequestPayload(requestJson);
            log.info("Executing API call to {} with request: {}", apiName, requestJson);
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize request for audit: {}", e.getMessage());
            audit.setRequestPayload("Serialization failed: " + e.getMessage());
        }

        // ========== STEP 3: Save initial audit (optional, before API call) ==========
        // apiAuditRepository.save(audit); // Uncomment if you want audit before API call

        try {
            // ========== STEP 4: Execute the actual API call ==========
            R response = apiCall.execute();

            // ========== STEP 5: Capture successful response ==========
            long endTime = System.currentTimeMillis();
            audit.setResponseTimeMs(endTime - startTime);
            audit.setStatus("SUCCESS");
            audit.setResponseCode(getSuccessStatusCode());

            // Capture response payload
            try {
                String responseJson = objectMapper.writeValueAsString(response);
                audit.setResponsePayload(responseJson);
                log.info("API call to {} completed successfully in {}ms",
                        apiName, audit.getResponseTimeMs());
            } catch (JsonProcessingException e) {
                log.warn("Could not serialize response for audit: {}", e.getMessage());
                audit.setResponsePayload("Serialization failed: " + e.getMessage());
            }

            // ========== STEP 6: Save final audit record ==========
            apiAuditRepository.save(audit);
            log.debug("Audit saved for API call: {}", apiName);

            return response;

        } catch (Exception ex) {
            // ========== STEP 7: Handle and audit failure ==========
            long endTime = System.currentTimeMillis();
            audit.setResponseTimeMs(endTime - startTime);
            audit.setStatus("FAILED");
            audit.setResponseCode(getFailureStatusCode());
            audit.setErrorMessage(ex.getMessage());

            // Save failure audit
            apiAuditRepository.save(audit);
            log.error("API call to {} failed after {}ms: {}",
                    apiName, audit.getResponseTimeMs(), ex.getMessage(), ex);

            // Re-throw with proper context
            throw new RuntimeException("External API call failed: " + apiName, ex);
        }
    }

    /*
     * Child classes can override these if needed
     *
     * WHY OVERRIDABLE? Different APIs use different status codes
     * - REST APIs: 200 for success
     * - GraphQL: 200 always, success in body
     * - File uploads: 201 for created
     */
    protected int getSuccessStatusCode() {
        return 200; // Default HTTP OK
    }

    protected int getFailureStatusCode() {
        return 500; // Default internal server error
    }

    /*
     * ============================================================
     * FUNCTIONAL INTERFACE FOR API CALLS
     * ============================================================
     *
     * WHY THIS INTERFACE?
     * - Allows lambda expressions in child classes
     * - Type-safe: T = Request type, R = Response type
     * - Example: () -> webClient.get().retrieve().bodyToMono(Response.class).block()
     */
    @FunctionalInterface
    protected interface ApiCall<T, R> {
        R execute() throws Exception;
    }
}