package com.planwith.planwith_fo_like.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "like.kafka")
public class LikeKafkaProperties {

	private boolean consumerEnabled = false;
	private Topics topics = new Topics();

	public boolean isConsumerEnabled() {
		return consumerEnabled;
	}

	public void setConsumerEnabled(boolean consumerEnabled) {
		this.consumerEnabled = consumerEnabled;
	}

	public Topics getTopics() {
		return topics;
	}

	public void setTopics(Topics topics) {
		this.topics = topics;
	}

	public static class Topics {
		private String likeCreated = "planwith.like.created";
		private String likeRemoved = "planwith.like.removed";

		public String getLikeCreated() {
			return likeCreated;
		}

		public void setLikeCreated(String likeCreated) {
			this.likeCreated = likeCreated;
		}

		public String getLikeRemoved() {
			return likeRemoved;
		}

		public void setLikeRemoved(String likeRemoved) {
			this.likeRemoved = likeRemoved;
		}
	}
}
