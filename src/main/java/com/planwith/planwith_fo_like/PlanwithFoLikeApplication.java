package com.planwith.planwith_fo_like;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.planwith.planwith_fo_like.config.AuthProperties;
import com.planwith.planwith_fo_like.config.DeployProperties;
import com.planwith.planwith_fo_like.config.LikeCacheProperties;
import com.planwith.planwith_fo_like.config.LikeKafkaProperties;
import com.planwith.planwith_fo_like.config.LikeOutboxProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
		AuthProperties.class,
		DeployProperties.class,
		LikeKafkaProperties.class,
		LikeOutboxProperties.class,
		LikeCacheProperties.class
})
public class PlanwithFoLikeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanwithFoLikeApplication.class, args);
	}

}
