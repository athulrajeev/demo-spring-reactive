package com.test.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class DemoSpringReactiveApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(DemoSpringReactiveApplication.class, args);
	}
}
