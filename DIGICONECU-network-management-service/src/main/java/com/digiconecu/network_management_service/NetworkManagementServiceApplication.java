package com.digiconecu.network_management_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NetworkManagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NetworkManagementServiceApplication.class, args);
	}

}
