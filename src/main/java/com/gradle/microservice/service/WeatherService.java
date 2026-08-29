package com.gradle.microservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.microservice.dto.OpenWeatherResponse;
import com.gradle.microservice.dto.WeatherRequestDTO;
import com.gradle.microservice.dto.WeatherResponseDTO;
import com.gradle.microservice.model.WeatherData;
import com.gradle.microservice.repository.ApiAuditRepository;
import com.gradle.microservice.repository.WeatherRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/*
 * ============================================================
 * WEATHER SERVICE - WITH ABSTRACT BASE CLASS AUDIT
 * ============================================================
 *
 * WHAT CHANGED?
 * -------------
 * Before: Had audit code scattered inside try-catch blocks
 * After: Extends BaseExternalApiService, audit is AUTOMATIC!
 *
 * WHY BETTER?
 * -----------
 * - No duplicate audit code
 * - Audit is guaranteed (can't forget to log)
 * - Focus only on business logic
 * - Consistent across all external API services
 *
 * HOW AUDIT WORKS NOW:
 * --------------------
 * 1. executeWithAudit() handles all audit logging
 * 2. Child only provides: API name + lambda with actual call
 * 3. Audit auto-saves success/failure with timings
 *
 * ============================================================
 * FIXES APPLIED ON 2026-05-23
 * ============================================================
 *
 * FIX 1: Removed duplicate .path("/data/2.5/weather")
 *        Reason: baseUrl already contains this path from application.properties
 *
 * FIX 2: Removed hardcoded .scheme("https") and .host("api.openweathermap.org")
 *        Reason: These should be configured, not hardcoded
 *        Better: Use baseUrl directly from application.properties
 *
 * FIX 3: Now using baseUrl directly with query parameters
 *        This ensures single source of truth for API URL
 *
 * CREATED BY: Anuj
 * LAST UPDATED: 2026-05-23
 * ============================================================
 */

@Slf4j
@Service
public class WeatherService extends BaseExternalApiService {

    private final WebClient webClient;
    private final WeatherRepository weatherRepository;

    @Value("${weather.api.key}")
    private String apiKey;

    // baseUrl from application.properties: https://api.openweathermap.org/data/2.5/weather
    @Value("${weather.api.url}")
    private String baseUrl;

    public WeatherService(
            ApiAuditRepository apiAuditRepository,
            ObjectMapper objectMapper,
            WebClient webClient,
            WeatherRepository weatherRepository) {
        super(apiAuditRepository, objectMapper); // Pass to parent
        this.webClient = webClient;
        this.weatherRepository = weatherRepository;
    }

    /*
     * ============================================================
     * GET WEATHER - WITH AUTOMATIC AUDIT
     * ============================================================
     *
     * WHY NO AUDIT CODE HERE?
     * ------------------------
     * The executeWithAudit() method from parent handles:
     * - Request payload capture
     * - Response payload capture
     * - Timing calculation
     * - Status tracking
     * - Error logging
     * - Saving to database
     *
     * Child only provides: API name + lambda with actual call
     *
     * ============================================================
     * OLD CODE (HARDCODED - REMOVED)
     * ============================================================
     * .uri(uriBuilder -> uriBuilder
     *     .scheme("https")
     *     .host("api.openweathermap.org")
     *     .path("/data/2.5/weather")
     *     .queryParam("lat", requestDTO.getLat())
     *     .queryParam("lon", requestDTO.getLon())
     *     .queryParam("appid", apiKey)
     *     .queryParam("units", "metric")
     *     .build())
     *
     * PROBLEM WITH OLD CODE:
     * - WebClientConfig.baseUrl was also set to same URL
     * - This caused duplicate path: /data/2.5/weather appearing twice
     * - Result: 404 error because URL became:
     *   https://api.openweathermap.org/data/2.5/weather/data/2.5/weather
     *
     * ============================================================
     * NEW CODE (FIXED)
     * ============================================================
     * Using baseUrl directly from application.properties
     * This is the SINGLE SOURCE OF TRUTH for API URL
     * No duplication, no hardcoding!
     * ============================================================
     */
    @Cacheable(value = "weather", key = "#requestDTO.lat + ',' + #requestDTO.lon")
    @CircuitBreaker(name = "weatherApi", fallbackMethod = "fallbackWeather")
    @Retry(name = "weatherApi", fallbackMethod = "fallbackWeather")
    @RateLimiter(name = "weatherApi")
    public WeatherResponseDTO getWeather(WeatherRequestDTO requestDTO) {

        // ========== BUILD COMPLETE URL WITH QUERY PARAMETERS ==========
        // baseUrl = https://api.openweathermap.org/data/2.5/weather
        // Adding query parameters: lat, lon, appid, units
        String fullUrl = baseUrl
                + "?lat=" + requestDTO.getLat()
                + "&lon=" + requestDTO.getLon()
                + "&appid=" + apiKey
                + "&units=metric";

        log.debug("Calling OpenWeather API with URL: {}", fullUrl.replace(apiKey, "***HIDDEN***"));

        // ========== THE LAMBDA WITH ACTUAL API CALL ==========
        // Parent handles all audit automatically!
        OpenWeatherResponse response = executeWithAudit(
                "OPEN_WEATHER_API",           // API Name
                requestDTO,                   // Request object (auto-JSON for audit)
                () -> {                       // Lambda with actual API call
                    return webClient
                            .get()
                            .uri(fullUrl)     // Using full URL from properties (NO hardcoding!)
                            .retrieve()
                            .bodyToMono(OpenWeatherResponse.class)
                            .blockOptional(Duration.ofSeconds(10))
                            .orElseThrow(() -> new RuntimeException("Weather API timeout"));
                }
        );

        // ========== BUSINESS LOGIC (NO AUDIT CODE NEEDED) ==========
        WeatherData weatherData = WeatherData.builder()
                .city(response.getName())
                .temperature(response.getMain().getTemp())
                .weatherCondition(response.getWeather().get(0).getMain())
                .provider("OPEN_WEATHER")
                .build();

        weatherRepository.save(weatherData);

        return WeatherResponseDTO.builder()
                .city(weatherData.getCity())
                .temperature(weatherData.getTemperature())
                .weatherCondition(weatherData.getWeatherCondition())
                .provider(weatherData.getProvider())
                .cached("false")
                .build();
    }

    /*
     * Fallback method when Circuit Breaker triggers
     */
    public WeatherResponseDTO fallbackWeather(WeatherRequestDTO requestDTO, Throwable t) {
        log.warn("Fallback triggered for weather API: {}", t.getMessage());
        return WeatherResponseDTO.builder()
                .city("Unknown")
                .temperature(null)
                .weatherCondition("Weather service temporarily unavailable")
                .provider("FALLBACK")
                .cached("true")
                .build();
    }
}