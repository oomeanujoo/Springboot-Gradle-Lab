package com.gradle.microservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LightResponseDTO implements Serializable {
    private String result;
    private String processedBy;
}
