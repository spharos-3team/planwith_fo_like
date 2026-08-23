package com.planwith.planwith_fo_like.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
@ConditionalOnProperty(name = "like.kafka.consumer-enabled", havingValue = "true")
public class LikeKafkaConsumerConfig {
}
