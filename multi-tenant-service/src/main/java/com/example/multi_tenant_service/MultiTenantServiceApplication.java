package com.example.multi_tenant_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.multi_tenant_service.client")
public class MultiTenantServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MultiTenantServiceApplication.class, args);
    }
}

