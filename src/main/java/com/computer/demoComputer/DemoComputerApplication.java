package com.computer.demoComputer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// @SpringBootApplication(exclude = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
public class DemoComputerApplication {
	public static void main(String[] args) {
		SpringApplication.run(DemoComputerApplication.class, args);
	}

}
 