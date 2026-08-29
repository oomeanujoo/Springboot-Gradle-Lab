package com.gradle.microservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Standard error response format for all API errors.
 * This ensures consistent error structure across the application.
 *
 * WHY NEEDED?
 * - GlobalExceptionHandler returns this format
 * - Client knows exactly what to expect
 * - Includes timestamp for debugging
 */
@Data
@Builder
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
}