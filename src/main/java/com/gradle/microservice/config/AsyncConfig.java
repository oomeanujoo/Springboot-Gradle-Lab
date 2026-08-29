package com.gradle.microservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async Configuration for non-blocking background processing.
 *
 * WHY ASYNC? For operations that don't need immediate response:
 * - Saving audit logs
 * - Sending notifications
 * - Processing non-critical data
 *
 * This prevents slow operations from blocking the main request thread.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);           // Always keep 5 threads ready
        executor.setMaxPoolSize(10);            // Up to 10 threads when busy
        executor.setQueueCapacity(100);         // Queue 100 tasks if all threads busy
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}