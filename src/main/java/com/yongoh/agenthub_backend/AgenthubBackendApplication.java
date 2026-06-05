package com.yongoh.agenthub_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AgenthubBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgenthubBackendApplication.class, args);
	}

}
