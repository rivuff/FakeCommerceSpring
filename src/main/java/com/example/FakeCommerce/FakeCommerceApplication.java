package com.example.FakeCommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FakeCommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FakeCommerceApplication.class, args);
	}

}
