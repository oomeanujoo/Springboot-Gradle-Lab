package com.gradle.microservice.service;

import com.gradle.microservice.model.Hostel;
import com.gradle.microservice.repository.HostelRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostConstrService {

    @Autowired
    private HostelRepository _hostelRepository;

    @PostConstruct
    void seedMethod() {
        Hostel hostel = new Hostel();
        hostel.setName("Varanasi");
        _hostelRepository.save(hostel);
        System.out.println("✅ Data seeded successfully!");
    }
}