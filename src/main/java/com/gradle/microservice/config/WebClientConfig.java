package com.gradle.microservice.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient Configuration with timeouts.
 *
 * WHY TIMEOUTS? Prevents hanging requests that consume threads.
 * Connect timeout = 5s, Read timeout = 10s, Write timeout = 10s.
 *
 * ============================================================
 * CONFIGURATION NOTES
 * ============================================================
 *
 * Property used: weather.api.url (from application.properties)
 * Value: https://api.openweathermap.org/data/2.5/weather
 *
 * WHY THIS PROPERTY?
 * - Already exists in application.properties
 * - No need to add new properties
 * - Matches existing configuration
 *
 * CREATED BY: Team
 * LAST UPDATED: 2026-05-23
 * ============================================================
 */
@Configuration
public class WebClientConfig {

    // FIXED: Using existing property "weather.api.url" instead of "weather.api.base-url"
    @Value("${weather.api.url}")
    private String baseUrl;

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)  // 5 second connect timeout
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))   // 10s read
                        .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS))  // 10s write
                )
                .responseTimeout(Duration.ofSeconds(10));  // Total response timeout

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}