package com.maintenance.maintenance_mcp_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.maintenance")
public class MaintenanceMcpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MaintenanceMcpServerApplication.class, args);
	}

}