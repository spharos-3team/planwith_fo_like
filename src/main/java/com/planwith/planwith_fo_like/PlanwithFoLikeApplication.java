package com.planwith.planwith_fo_like;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.planwith.planwith_fo_like.config.AuthProperties;
import com.planwith.planwith_fo_like.config.DeployProperties;

@SpringBootApplication
@EnableConfigurationProperties({AuthProperties.class, DeployProperties.class})
public class PlanwithFoLikeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanwithFoLikeApplication.class, args);
	}

}
