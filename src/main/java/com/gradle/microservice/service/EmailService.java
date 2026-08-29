package com.gradle.microservice.service;

import org.springframework.stereotype.Component;

@Component
public class EmailService {

	public String sendEmail(String to) {
		return "Email sent to " + to;
	}
}
