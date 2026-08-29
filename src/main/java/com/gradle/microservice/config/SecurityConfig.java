package com.gradle.microservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for testing (especially for POST via Postman/cURL)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Allow all endpoints
                );

        return http.build();
    }
}
