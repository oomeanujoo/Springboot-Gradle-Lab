package com.gradle.microservice.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache Configuration using Caffeine (in-memory cache).
 *
 * WHY CAFFEINE? High-performance, near-LRU caching.
 * Alternative: Redis for distributed caching across multiple instances.
 *
 * TTL = 10 minutes - Weather doesn't change rapidly, reduces API calls.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("weather");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)  // Cache expires after 10 minutes
                .maximumSize(1000)                       // Store up to 1000 entries
                .recordStats());                         // Track cache hit/miss rates
        return cacheManager;
    }
}