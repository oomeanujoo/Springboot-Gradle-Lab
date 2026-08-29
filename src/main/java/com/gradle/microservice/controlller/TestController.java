package com.gradle.microservice.controlller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gradle.microservice.dto.SmtpClient;
import com.gradle.microservice.service.BeanLifecycle;
import com.gradle.microservice.service.EmailService;

@RestController
public class TestController {

	@Autowired
	EmailService emailService;

	@Autowired
	SmtpClient smtpClient;

	@Autowired
	BeanLifecycle beanLifecycle;

	@GetMapping("/test")
	public String test() {
		String emailResult = emailService.sendEmail("john@gmail.com");
		String host = smtpClient.host(); // record style — no "get" prefix!
		return emailResult + " via " + host;
	}

	@GetMapping("/lifecycle")
	public String lifecycle() {
		beanLifecycle.BeanInAction(); // call it
		return "Check your console!";
	}

}