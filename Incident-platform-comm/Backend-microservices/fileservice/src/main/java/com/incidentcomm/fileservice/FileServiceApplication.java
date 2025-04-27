package com.incidentcomm.fileservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@SpringBootApplication
@EnableAsync  // For async event processing
@EnableScheduling  // For scheduled tasks like cleanup jobs
public class FileServiceApplication {

	public static void main(String[] args) {
		// Create upload directory if it doesn't exist
		File uploadDir = new File("./uploads");
		if (!uploadDir.exists()) {
			uploadDir.mkdirs();
		}

		SpringApplication.run(FileServiceApplication.class, args);
	}
}