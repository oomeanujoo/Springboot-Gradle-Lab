package com.gradle.microservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gradle.microservice.dto.SmtpClient;

@Configuration
public class AppConfig {

	@Bean
	public SmtpClient smtpClient() {
		return new SmtpClient("anuzz8602@gmail.com");
	}
}
