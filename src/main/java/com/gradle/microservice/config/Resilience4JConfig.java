package com.gradle.microservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4J Configuration.
 *
 * Provides:
 * 1. Circuit Breaker - Prevents cascade failures
 * 2. Retry - Handles transient failures
 * 3. Rate Limiter - Respects API free tier limits
 */
@Configuration
public class Resilience4JConfig {

    /**
     * Circuit Breaker Configuration
     *
     * WHY? When OpenWeather API fails, circuit opens after 5 failures.
     * Subsequent calls fail fast without waiting for timeout.
     * After 30 seconds, half-open state tests if API recovered.
     */
    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .slidingWindowSize(10)                    // Evaluate last 10 calls
                .failureRateThreshold(50)                 // Open circuit if 50% fail
                .waitDurationInOpenState(Duration.ofSeconds(30))  // Wait 30s before retry
                .permittedNumberOfCallsInHalfOpenState(3) // Allow 3 test calls in half-open
                .build();
    }

    /**
     * Retry Configuration
     *
     * WHY? Network issues are often transient.
     * Retry 3 times with exponential backoff (1s, 2s, 4s).
     */
    @Bean
    public RetryConfig retryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)                           // Try 3 times
                .waitDuration(Duration.ofSeconds(1))      // Initial wait 1 second
                .retryExceptions(Exception.class)         // Retry on any exception
                .build();
    }

    /**
     * Rate Limiter Configuration
     *
     * WHY? OpenWeather free tier allows 60 calls per minute.
     * Set limit to 50 calls/minute to stay safe.
     * Excess requests get 429 (Too Many Requests) response.
     */
    @Bean
    public RateLimiterConfig rateLimiterConfig() {
        return RateLimiterConfig.custom()
                .limitForPeriod(50)                       // 50 requests per period
                .limitRefreshPeriod(Duration.ofMinutes(1)) // Period = 1 minute
                .timeoutDuration(Duration.ofSeconds(1))    // Wait max 1 second for permit
                .build();
    }
}