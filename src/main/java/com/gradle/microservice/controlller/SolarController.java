package com.gradle.microservice.controlller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/solar")
public class SolarController {

    private static final Map<String, Map<String, Object>> PLANETS = new HashMap<>();

    static {
        PLANETS.put("mercury", Map.of("name", "Mercury", "order", 1, "type", "Terrestrial"));
        PLANETS.put("venus", Map.of("name", "Venus", "order", 2, "type", "Terrestrial"));
        PLANETS.put("earth", Map.of("name", "Earth", "order", 3, "type", "Terrestrial"));
        PLANETS.put("mars", Map.of("name", "Mars", "order", 4, "type", "Terrestrial"));
        PLANETS.put("jupiter", Map.of("name", "Jupiter", "order", 5, "type", "Gas Giant"));
        PLANETS.put("saturn", Map.of("name", "Saturn", "order", 6, "type", "Gas Giant"));
        PLANETS.put("uranus", Map.of("name", "Uranus", "order", 7, "type", "Ice Giant"));
        PLANETS.put("neptune", Map.of("name", "Neptune", "order", 8, "type", "Ice Giant"));
    }

    @GetMapping("/planets")
    public ResponseEntity<List<Map<String, Object>>> getAllPlanets() {
        return ResponseEntity.ok(new ArrayList<>(PLANETS.values()));
    }

    @GetMapping("/planets/{name}")
    public ResponseEntity<Object> getPlanetByName(@PathVariable String name) {
        Map<String, Object> planet = PLANETS.get(name.toLowerCase());
        if (planet == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Planet not found: " + name));
        }
        return ResponseEntity.ok(planet);
    }
}
