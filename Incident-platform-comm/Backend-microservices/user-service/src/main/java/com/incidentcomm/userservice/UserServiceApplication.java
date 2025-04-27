package com.incidentcomm.userservice;

import com.incidentcomm.userservice.model.Role;
import com.incidentcomm.userservice.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner initRoles(RoleRepository roleRepository) {
		return args -> {
			// Initialize roles if they don't exist
			for (Role.ERole role : Role.ERole.values()) {
				if (!roleRepository.findByName(role).isPresent()) {
					roleRepository.save(new Role(role));
				}
			}
		};
	}
}