package com.gradle.microservice.microservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/*
 * ============================================================
 * WEATHER EVENT DTO - FOR KAFKA MESSAGING
 * ============================================================
 *
 * WHAT THIS FILE DOES:
 * --------------------
 * - Represents a weather request message sent to Kafka
 * - Contains all information needed to fetch weather
 * - Serializable for Kafka transmission
 *
 * WHY WE CANNOT USE EXISTING WeatherRequestDTO?
 * ---------------------------------------------
 * 1. WeatherRequestDTO has validation annotations (@NotNull, @DecimalMin, etc.)
 *    - These are for API validation, not for Kafka messages
 *    - Kafka doesn't need validation at the message level
 *
 * 2. WeatherRequestDTO lacks tracking fields:
 *    - No requestId for tracing
 *    - No timestamp for when request was created
 *    - No cityName field (only lat/lon)
 *
 * 3. Separation of concerns:
 *    - WeatherRequestDTO = HTTP API layer
 *    - WeatherEventDTO = Messaging/Kafka layer
 *    - Different layers have different requirements
 *
 * WHY SERIALIZABLE?
 * - Kafka messages need to be serialized to bytes
 * - JSON serialization is used (Spring Kafka default)
 *
 * FIELDS:
 * - requestId: Unique identifier for tracking across systems
 * - lat/lon: Coordinates for weather API (copied from WeatherRequestDTO)
 * - cityName: Human-readable city name (for logging and debugging)
 * - requestedAt: Timestamp when request was created
 *
 * CREATED BY: Team
 * LAST UPDATED: 2026-05-24
 * ============================================================
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherEventDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String requestId;           // Unique request ID for distributed tracing
    private Double lat;                 // Latitude (copied from WeatherRequestDTO)
    private Double lon;                 // Longitude (copied from WeatherRequestDTO)
    private String cityName;            // City name for logging (from WeatherScheduler)
    private LocalDateTime requestedAt;  // Request timestamp
}