package io.github.onrcanogul.openbanking.hhsbank.infrastructure.configuration;

import io.github.onrcanogul.openbanking.hhsbank.common.exception.ConsentNotFoundException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
public class KafkaConfig {
    @Bean
    DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> template) {
        DefaultErrorHandler handler = new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(template),
                new ExponentialBackOff(500, 2.0) {{ setMaxElapsedTime(10_000); }});
        handler.addNotRetryableExceptions(
                ConsentNotFoundException.class);
        return handler;
    }
}
