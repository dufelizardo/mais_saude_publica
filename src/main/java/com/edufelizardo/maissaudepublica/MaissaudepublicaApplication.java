package com.edufelizardo.maissaudepublica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MaissaudepublicaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MaissaudepublicaApplication.class, args);
	}

}
