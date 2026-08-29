package com.gradle.microservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.microservice.microservice.dto.WeatherEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/*
 * ============================================================
 * KAFKA PRODUCER - Publishes weather requests to Kafka
 * ============================================================
 *
 * WHAT THIS FILE DOES:
 * --------------------
 * - Takes weather request (lat, lon, city name) and sends to Kafka topic
 * - Converts request to JSON before sending
 * - Adds unique request ID for distributed tracing
 *
 * WHY SEPARATE PRODUCER?
 * ----------------------
 * - Decouples scheduler from weather processing
 * - Scheduler doesn't wait for API calls (non-blocking)
 * - Handles backpressure via Kafka buffering
 *
 * ENTERPRISE USE CASE:
 * --------------------
 * - Scheduler calls publishWeatherRequest() for each city
 * - Kafka buffers requests if consumers are slow
 * - Multiple consumers can process in parallel
 *
 * WHY NOT USE WeatherService directly in Scheduler?
 * -------------------------------------------------
 * - WeatherService.getWeather() is synchronous (blocks)
 * - With 8 cities, each API call takes ~1-2 seconds
 * - Total time = 8-16 seconds (scheduler blocks)
 * - With Kafka: Scheduler publishes in milliseconds, non-blocking!
 *
 * CREATED BY: Team
 * LAST UPDATED: 2026-05-24
 * ============================================================
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.weather-requests:weather-requests}")
    private String weatherRequestsTopic;

    /**
     * Publish weather request to Kafka for async processing
     *
     * @param lat Latitude
     * @param lon Longitude
     * @param cityName City name (for logging and debugging)
     */
    public void publishWeatherRequest(Double lat, Double lon, String cityName) {
        try {
            // Create event with unique ID for tracking
            WeatherEventDTO event = WeatherEventDTO.builder()
                    .requestId(UUID.randomUUID().toString())
                    .lat(lat)
                    .lon(lon)
                    .cityName(cityName)
                    .requestedAt(LocalDateTime.now())
                    .build();

            // Send to Kafka (using cityName as key for partitioning)
            // Same city always goes to same partition (ordered processing)
            kafkaTemplate.send(weatherRequestsTopic, cityName, event);

            log.info("📤 Published weather request to Kafka: city={}, requestId={}, topic={}",
                    cityName, event.getRequestId(), weatherRequestsTopic);

        } catch (Exception e) {
            log.error("Failed to publish weather request to Kafka: city={}, error={}",
                    cityName, e.getMessage(), e);
            // Don't throw - let scheduler continue with other cities
        }
    }
}