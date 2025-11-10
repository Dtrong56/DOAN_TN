package com.example.resident_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.resident_service.client")
public class ResidentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResidentServiceApplication.class, args);
	}

}
