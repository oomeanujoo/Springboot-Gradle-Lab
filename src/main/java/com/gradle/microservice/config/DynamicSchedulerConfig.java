package com.gradle.microservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * DYNAMIC SCHEDULER CONFIGURATION
 *
 * ============================================================
 * WHAT THIS FILE DOES
 * ============================================================
 * - Creates a ThreadPoolTaskScheduler bean for dynamic cron scheduling
 * - Allows changing schedules at runtime without restart
 * - Required for the dynamic scheduler feature in WeatherScheduler
 *
 * WHY SEPARATE BEAN?
 * - Default @Scheduled annotations are static (fixed at compile time)
 * - Dynamic scheduling needs a programmatic scheduler
 * - This bean enables runtime schedule changes
 *
 * CREATED BY: Team
 * LAST UPDATED: 2026-05-23
 * ============================================================
 */
@Configuration
public class DynamicSchedulerConfig {

    /**
     * Creates a ThreadPoolTaskScheduler for dynamic scheduling
     *
     * Pool size: 5 threads (can handle multiple parallel tasks)
     * Thread name prefix: "dynamic-scheduler-" (easy to identify in logs)
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("dynamic-scheduler-");
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.initialize();
        return scheduler;
    }
}