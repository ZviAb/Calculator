package com.example.calculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Calculator Spring Boot application.
 * This class serves as the entry point for the application.
 */
@SpringBootApplication
public class CalculatorApplication {

	/**
	 * Main method to start the Spring Boot application.
	 * The application will run on port 8080 by default.
	 */
	public static void main(String[] args) {
		SpringApplication.run(CalculatorApplication.class, args);
	}
}