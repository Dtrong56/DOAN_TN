package com.example.notification_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.example.notification_service.service.EmailService;

@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.notification_service.client")
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner testMail(EmailService emailService) {
		return args -> {
			emailService.sendEmail(
				"n21dccn190@student.ptithcm.edu.vn",
				"Test Notification Service",
				"Mail gửi thành công!"
			);
		};
	}


}
