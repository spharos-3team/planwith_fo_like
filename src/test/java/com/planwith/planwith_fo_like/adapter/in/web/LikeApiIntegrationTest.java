package com.planwith.planwith_fo_like.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class LikeApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void addLikeIsIdempotentAndQueryReadsCounter() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();
		UUID ownerUuid = UUID.randomUUID();
		String body = """
				{
				  "targetType": "STORY",
				  "targetUuid": "%s",
				  "targetOwnerUuid": "%s"
				}
				""".formatted(targetUuid, ownerUuid);

		mockMvc.perform(post("/api/v1/likes")
						.header("X-Member-UUID", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(true))
				.andExpect(jsonPath("$.likeCount").value(1))
				.andExpect(jsonPath("$.alreadyApplied").value(false));

		mockMvc.perform(post("/api/v1/likes")
						.header("X-Member-UUID", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(true))
				.andExpect(jsonPath("$.likeCount").value(1))
				.andExpect(jsonPath("$.alreadyApplied").value(true));

		mockMvc.perform(get("/api/v1/likes/me")
						.header("X-Member-UUID", memberUuid)
						.param("targetType", "STORY")
						.param("targetUuid", targetUuid.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(true));

		mockMvc.perform(get("/api/v1/likes/count")
						.param("targetType", "STORY")
						.param("targetUuid", targetUuid.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.likeCount").value(1));

		mockMvc.perform(delete("/api/v1/likes")
						.header("X-Member-UUID", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(false))
				.andExpect(jsonPath("$.likeCount").value(0))
				.andExpect(jsonPath("$.alreadyApplied").value(false));
	}
}
