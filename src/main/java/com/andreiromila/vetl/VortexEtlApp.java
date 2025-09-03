package com.andreiromila.vetl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VortexEtlApp {

	public static void main(String[] args) {
		SpringApplication.run(VortexEtlApp.class, args);
	}

}
