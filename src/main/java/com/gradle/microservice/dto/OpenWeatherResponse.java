package com.gradle.microservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class OpenWeatherResponse {

    private String name;  // ← ADD THIS - City name from API!

    private Main main;

    private List<Weather> weather;

    @Data
    public static class Main {
        private Double temp;
    }

    @Data
    public static class Weather {
        private String main;
        private String description;
    }
}