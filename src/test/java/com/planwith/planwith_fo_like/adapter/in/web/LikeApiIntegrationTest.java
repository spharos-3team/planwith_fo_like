package com.planwith.planwith_fo_like.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class LikeApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void putLikeAndDeleteUnlikeAreIdempotentAndSupportRelike() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();
		UUID ownerUuid = UUID.randomUUID();
		String likePath = "/api/v1/likes/STORY/" + targetUuid;

		mockMvc.perform(put(likePath)
						.header("X-Member-UUID", memberUuid)
						.param("targetOwnerUuid", ownerUuid.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(true))
				.andExpect(jsonPath("$.likeType").value("STORY"))
				.andExpect(jsonPath("$.likeCount").value(1))
				.andExpect(jsonPath("$.alreadyApplied").value(false));

		mockMvc.perform(put(likePath)
						.header("X-Member-UUID", memberUuid)
						.param("targetOwnerUuid", ownerUuid.toString()))
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

		mockMvc.perform(delete(likePath)
						.header("X-Member-UUID", memberUuid)
						.param("targetOwnerUuid", ownerUuid.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(false))
				.andExpect(jsonPath("$.likeCount").value(0))
				.andExpect(jsonPath("$.alreadyApplied").value(false));

		mockMvc.perform(delete(likePath)
						.header("X-Member-UUID", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(false))
				.andExpect(jsonPath("$.likeCount").value(0))
				.andExpect(jsonPath("$.alreadyApplied").value(true));

		mockMvc.perform(put(likePath)
						.header("X-Member-UUID", memberUuid)
						.param("targetOwnerUuid", ownerUuid.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(true))
				.andExpect(jsonPath("$.likeCount").value(1))
				.andExpect(jsonPath("$.alreadyApplied").value(false));
	}

	@Test
	void rejectsUnsupportedLikeTypeAndInvalidUuid() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();

		mockMvc.perform(put("/api/v1/likes/PLAN/" + targetUuid)
						.header("X-Member-UUID", memberUuid))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_LIKE_TYPE"));

		mockMvc.perform(put("/api/v1/likes/STORY/not-uuid")
						.header("X-Member-UUID", memberUuid))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_LIKE_TARGET"));

		mockMvc.perform(put("/api/v1/likes/STORY/" + targetUuid))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
	}

	@Test
	void storyAndCommentLikesAreCommandedIndependently() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();

		mockMvc.perform(put("/api/v1/likes/STORY/" + targetUuid)
						.header("X-Member-UUID", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.likeType").value("STORY"))
				.andExpect(jsonPath("$.likeCount").value(1));

		mockMvc.perform(put("/api/v1/likes/COMMENT/" + targetUuid)
						.header("X-Member-UUID", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.likeType").value("COMMENT"))
				.andExpect(jsonPath("$.likeCount").value(1));

		mockMvc.perform(delete("/api/v1/likes/STORY/" + targetUuid)
						.header("X-Member-UUID", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(false))
				.andExpect(jsonPath("$.likeCount").value(0));

		mockMvc.perform(get("/api/v1/likes/count")
						.param("targetType", "COMMENT")
						.param("targetUuid", targetUuid.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.likeCount").value(1));
	}
}
