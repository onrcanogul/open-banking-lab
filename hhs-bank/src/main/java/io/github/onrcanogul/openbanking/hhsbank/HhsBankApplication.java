package io.github.onrcanogul.openbanking.hhsbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HhsBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(HhsBankApplication.class, args);
	}

}
