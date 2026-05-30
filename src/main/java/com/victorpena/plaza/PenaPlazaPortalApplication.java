package com.victorpena.plaza;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PenaPlazaPortalApplication {

	public static void main(String[] args) {
		SpringApplication.run(PenaPlazaPortalApplication.class, args);
	}

}
