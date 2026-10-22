package io.github.onrcanogul.openbanking.authsimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * The bank's GKD (strong customer authentication) channel for the lab:
 * customer login via Keycloak, consent approval screen, GKD events to hhs-bank over Kafka,
 * and the ÖHVPS erişim belirteci endpoint that YÖS calls with the yetKod.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class AuthSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthSimulatorApplication.class, args);
    }
}
