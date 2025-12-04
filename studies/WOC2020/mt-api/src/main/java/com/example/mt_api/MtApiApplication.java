package com.example.mt_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// based on https://www.geeksforgeeks.org/spring-boot-3-0-jwt-authentication-with-spring-security-using-mysql-database/

@SpringBootApplication
public class MtApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MtApiApplication.class, args);
	}

}
