package com.gradle.microservice.service;


import com.gradle.microservice.dto.WeatherRequestDTO;
import com.gradle.microservice.dto.WeatherResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;

/*
 * ============================================================
 * WEATHER SCHEDULER - CRON JOB SERVICE
 * ============================================================
 *
 * WHAT THIS FILE DOES:
 * --------------------
 * - Automatically fetches weather data at scheduled times
 * - Option 1 (OLD): Calls WeatherService directly (synchronous)
 * - Option 2 (NEW): Publishes to Kafka (asynchronous) - PHASE 1
 *
 * ============================================================
 * WHY TWO OPTIONS?
 * ============================================================
 *
 * OLD APPROACH (Direct call to WeatherService):
 * - Scheduler waits for each API call (1-2 seconds per city)
 * - 8 cities = 8-16 seconds total
 * - Scheduler thread blocked
 *
 * NEW APPROACH (Publish to Kafka):
 * - Scheduler publishes all 8 messages in milliseconds
 * - No waiting for API calls
 * - Kafka handles buffering and delivery
 * - Consumer processes messages asynchronously
 *
 * ============================================================
 * UPDATED ON 2026-05-24 - PHASE 1 (Producer Only)
 * ============================================================
 * - Added WeatherEventProducer dependency (commented by default)
 * - To enable Kafka: Uncomment the producer code in fetchWeatherForAllCities()
 * - To keep old behavior: Comment out Kafka code, keep direct call
 *
 * CREATED BY: Team
 * LAST UPDATED: 2026-05-24
 * ============================================================
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherScheduler {

    private final WeatherService weatherService;
    private final ThreadPoolTaskScheduler taskScheduler;

    // ========== KAFKA PRODUCER (Commented - Enable for Phase 1) ==========
    // Uncomment the line below to enable Kafka publishing
    // private final WeatherEventProducer weatherEventProducer;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ScheduledFuture<?> currentScheduledTask;
    private String currentCronExpression = "0 */30 * * * *";

    private static class CityConfig {
        String name;
        double lat;
        double lon;
        CityConfig(String name, double lat, double lon) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
        }
    }

    private static final CityConfig[] CITIES = {
            new CityConfig("Mumbai", 19.0760, 72.8777),
            new CityConfig("Delhi", 28.6139, 77.2090),
            new CityConfig("Bangalore", 12.9716, 77.5946),
            new CityConfig("Chennai", 13.0827, 80.2707),
            new CityConfig("Kolkata", 22.5726, 88.3639),
            new CityConfig("Hyderabad", 17.3850, 78.4867),
            new CityConfig("Pune", 18.5204, 73.8567),
            new CityConfig("Ahmedabad", 23.0225, 72.5714)
    };

    // ============================================================
    // DEFAULT STATIC CRON JOB (Runs at startup)
    // ============================================================

    @Scheduled(cron = "0 */30 * * * *")
    public void fetchWeatherEvery30Minutes() {
        log.info("========== SCHEDULED WEATHER FETCH - EVERY 30 MINUTES ==========");
        log.info("Time: {}", LocalDateTime.now().format(formatter));
        fetchWeatherForAllCities();
    }

    // ============================================================
    // ALTERNATIVE STATIC CRON JOBS (Commented - Use as needed)
    // ============================================================

    // @Scheduled(cron = "0 0 * * * *")
    public void fetchWeatherEveryHour() {
        log.info("========== SCHEDULED WEATHER FETCH - EVERY HOUR ==========");
        log.info("Time: {}", LocalDateTime.now().format(formatter));
        fetchWeatherForAllCities();
    }

    // @Scheduled(cron = "0 0 6 * * *")
    public void fetchWeatherDailyAt6AM() {
        log.info("========== SCHEDULED WEATHER FETCH - DAILY AT 6 AM ==========");
        log.info("Time: {}", LocalDateTime.now().format(formatter));
        fetchWeatherForAllCities();
    }

    // @Scheduled(cron = "0 0 0 * * MON")
    public void fetchWeatherWeekly() {
        log.info("========== SCHEDULED WEATHER FETCH - WEEKLY ON MONDAY ==========");
        log.info("Time: {}", LocalDateTime.now().format(formatter));
        fetchWeatherForAllCities();
    }

    // @Scheduled(cron = "0 */1 * * * *")
    public void fetchWeatherEveryMinuteForTesting() {
        log.info("========== SCHEDULED WEATHER FETCH - EVERY MINUTE (TESTING) ==========");
        log.info("Time: {}", LocalDateTime.now().format(formatter));
        log.info("⚠️  TEST MODE ACTIVE - FREQUENT API CALLS");
        fetchWeatherForAllCities();
    }

    // ============================================================
    // DYNAMIC SCHEDULER METHODS
    // ============================================================

    public String updateScheduleByMinutes(int minutes) {
        if (minutes < 1) {
            return "Error: Minutes must be at least 1";
        }
        String cronExpression = "0 */" + minutes + " * * * *";
        return updateScheduleByCron(cronExpression);
    }

    public String updateScheduleByCron(String cronExpression) {
        try {
            new CronTrigger(cronExpression);

            if (currentScheduledTask != null && !currentScheduledTask.isCancelled()) {
                currentScheduledTask.cancel(true);
                log.info("Previous scheduled task cancelled");
            }

            this.currentCronExpression = cronExpression;

            currentScheduledTask = taskScheduler.schedule(
                    this::fetchWeatherForAllCities,
                    new CronTrigger(cronExpression)
            );

            log.info("Scheduler updated to: {}", cronExpression);
            return "Scheduler updated successfully! New cron expression: " + cronExpression;

        } catch (IllegalArgumentException e) {
            log.error("Invalid cron expression: {}", cronExpression, e);
            return "Error: Invalid cron expression '" + cronExpression + "'. " + e.getMessage();
        }
    }

    public String getSchedulerStatus() {
        return "Current Schedule: " + currentCronExpression;
    }

    public String stopDynamicScheduler() {
        if (currentScheduledTask != null && !currentScheduledTask.isCancelled()) {
            currentScheduledTask.cancel(true);
            return "Dynamic scheduler stopped. Default schedule (every 30 minutes) is still active.";
        }
        return "No dynamic scheduler is currently running.";
    }

    // ============================================================
    // CORE METHOD: Fetch weather for all cities
    // ============================================================
    //
    // TWO VERSIONS PROVIDED:
    //
    // Version 1 (OLD): Direct call to WeatherService (synchronous)
    // - Scheduler waits for each API call
    // - Works without Kafka
    // - Good for testing without Kafka
    //
    // Version 2 (NEW): Publish to Kafka (asynchronous)
    // - Uncomment the Kafka code and comment the direct call code
    // - Requires Kafka running
    // - Non-blocking, better performance
    // ============================================================

    public void fetchWeatherForAllCities() {
        log.info("Total cities to process: {}", CITIES.length);

        int successCount = 0;
        int failureCount = 0;

        for (CityConfig city : CITIES) {
            try {
                // ============================================================
                // VERSION 1: DIRECT CALL TO WeatherService (CURRENT ACTIVE)
                // This is the OLD way - works without Kafka
                // ============================================================

                WeatherRequestDTO request = new WeatherRequestDTO();
                request.setLat(city.lat);
                request.setLon(city.lon);

                log.info("Fetching weather for: {} (lat={}, lon={})", city.name, city.lat, city.lon);

                WeatherResponseDTO response = weatherService.getWeather(request);

                log.info("SUCCESS - {} | Temperature: {}°C | Condition: {}",
                        city.name, response.getTemperature(), response.getWeatherCondition());
                successCount++;

                // ============================================================
                // VERSION 2: PUBLISH TO KAFKA (COMMENTED - Enable for Phase 1)
                // Uncomment this block and comment the above direct call to enable Kafka
                // ============================================================
                /*
                // PUBLISH TO KAFKA instead of direct API call
                weatherEventProducer.publishWeatherRequest(city.lat, city.lon, city.name);
                log.info("📤 Published to Kafka: city={}", city.name);
                successCount++;  // Count as success (published to Kafka)
                */

            } catch (Exception e) {
                log.error("FAILED - {}: {}", city.name, e.getMessage());
                failureCount++;
            }
        }

        log.info("WEATHER FETCH COMPLETED - Total: {}, Success: {}, Failed: {}",
                CITIES.length, successCount, failureCount);
    }
}