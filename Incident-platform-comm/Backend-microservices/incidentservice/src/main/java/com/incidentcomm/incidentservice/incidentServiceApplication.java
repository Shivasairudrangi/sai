package com.incidentcomm.incidentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync  // For async event processing
@EnableScheduling  // For scheduled tasks
public class incidentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(incidentServiceApplication.class, args);
	}
}