package com.gradle.microservice.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Response DTO sent to the client.
 * Contains only the fields the client needs (not the full OpenWeather response).
 */
@Data
@Builder
public class WeatherResponseDTO {
    private String city;
    private Double temperature;
    private String weatherCondition;
    private String provider;
    private String cached;  // NEW: indicates if response came from cache
}