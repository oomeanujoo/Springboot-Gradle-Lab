package com.gradle.microservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data                 // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor    // Generates no-args constructor
@AllArgsConstructor   // Generates all-args constructor
public class LightRequestDTO implements Serializable {
    private String source;
    private String message;
}
