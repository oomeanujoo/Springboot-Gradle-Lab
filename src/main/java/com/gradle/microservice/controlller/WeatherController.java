package com.gradle.microservice.controlller;


import com.gradle.microservice.dto.WeatherRequestDTO;
import com.gradle.microservice.dto.WeatherResponseDTO;

import com.gradle.microservice.service.WeatherScheduler;
import com.gradle.microservice.service.WeatherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;
    private final WeatherScheduler weatherScheduler;

    @PostMapping
    public ResponseEntity<WeatherResponseDTO> getWeather(@Valid @RequestBody WeatherRequestDTO requestDTO) {
        log.info("Received weather request for lat={}, lon={}", requestDTO.getLat(), requestDTO.getLon());
        WeatherResponseDTO response = weatherService.getWeather(requestDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "weather-api"));
    }

    @PostMapping("/trigger-schedule")
    public ResponseEntity<String> triggerScheduledFetch() {
        log.info("Manual trigger received - fetching weather for all cities");
        weatherScheduler.fetchWeatherForAllCities();
        return ResponseEntity.ok("Scheduled weather fetch triggered manually");
    }

    @PostMapping("/scheduler/interval")
    public ResponseEntity<String> updateScheduleByMinutes(@RequestParam int minutes) {
        log.info("API Request: Update schedule to run every {} minutes", minutes);
        String result = weatherScheduler.updateScheduleByMinutes(minutes);
        if (result.startsWith("Error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/scheduler/cron")
    public ResponseEntity<String> updateScheduleByCron(@RequestParam String expression) {
        log.info("API Request: Update schedule to cron expression: {}", expression);
        String result = weatherScheduler.updateScheduleByCron(expression);
        if (result.startsWith("Error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/scheduler/status")
    public ResponseEntity<String> getSchedulerStatus() {
        String status = weatherScheduler.getSchedulerStatus();
        return ResponseEntity.ok(status);
    }

    @PostMapping("/scheduler/stop")
    public ResponseEntity<String> stopDynamicScheduler() {
        log.info("API Request: Stop dynamic scheduler");
        String result = weatherScheduler.stopDynamicScheduler();
        return ResponseEntity.ok(result);
    }
}