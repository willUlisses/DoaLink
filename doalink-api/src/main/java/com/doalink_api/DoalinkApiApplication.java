package com.doalink_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DoalinkApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DoalinkApiApplication.class, args);
	}

}
