package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ZayApplication {
	private boolean migrationCompleted;


    public static void main(String[] args) {
		SpringApplication.run(ZayApplication.class, args);
	}


}
