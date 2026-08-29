package com.gradle.microservice.service;

import com.gradle.microservice.dto.LightResponseDTO;
import com.gradle.microservice.dto.LightRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BlackholeService {

    private final KafkaTemplate<String, Object> kafkaTemplate; // <-- Object here
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BlackholeService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "blackhole-requests", groupId = "blackhole-group")
    public void listenLight(String message) {
        try {
            LightRequestDTO light = objectMapper.readValue(message, LightRequestDTO.class);
            System.out.println("Blackhole received light from: " + light.getSource());

            LightResponseDTO response = new LightResponseDTO(
                    "Light absorbed by blackhole",
                    "BlackholeService"
            );

            kafkaTemplate.send("blackhole-responses", response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
