package com.planwith.planwith_fo_like.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.StringJoiner;
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

		mockMvc.perform(get(likePath + "/me")
						.header("X-Member-UUID", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(true));

		mockMvc.perform(get(likePath + "/count"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.targetUuid").value(targetUuid.toString()))
				.andExpect(jsonPath("$.likeType").value("STORY"))
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

		mockMvc.perform(get("/api/v1/likes/COMMENT/" + targetUuid + "/count"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.likeType").value("COMMENT"))
				.andExpect(jsonPath("$.likeCount").value(1));
	}

	@Test
	void queryStatusAndCountForInitialRender() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();
		String queryBase = "/api/v1/likes/STORY/" + targetUuid;

		mockMvc.perform(get(queryBase + "/me")
						.header("X-Member-UUID", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(false));

		mockMvc.perform(get(queryBase + "/count"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.targetUuid").value(targetUuid.toString()))
				.andExpect(jsonPath("$.likeType").value("STORY"))
				.andExpect(jsonPath("$.likeCount").value(0));

		mockMvc.perform(put(queryBase)
						.header("X-Member-UUID", memberUuid))
				.andExpect(status().isOk());

		mockMvc.perform(get(queryBase + "/me")
						.header("X-Member-UUID", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(true));

		mockMvc.perform(get(queryBase + "/count"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.likeCount").value(1));

		mockMvc.perform(get(queryBase + "/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));

		mockMvc.perform(get("/api/v1/likes/PLAN/" + targetUuid + "/count"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_LIKE_TYPE"));
	}

	@Test
	void snapshotSupportsStoryCommentOptimisticUiAndGuestView() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		UUID targetUuid = UUID.randomUUID();
		String storyPath = "/api/v1/likes/STORY/" + targetUuid;

		mockMvc.perform(get(storyPath))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.likeType").value("STORY"))
				.andExpect(jsonPath("$.targetUuid").value(targetUuid.toString()))
				.andExpect(jsonPath("$.liked").value(false))
				.andExpect(jsonPath("$.likeCount").value(0))
				.andExpect(jsonPath("$.optimisticLikeCount").value(1))
				.andExpect(jsonPath("$.optimisticUnlikeCount").value(0));

		mockMvc.perform(put(storyPath)
						.header("X-Member-UUID", memberUuid))
				.andExpect(status().isOk());

		mockMvc.perform(get(storyPath)
						.header("X-Member-UUID", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(true))
				.andExpect(jsonPath("$.likeCount").value(1))
				.andExpect(jsonPath("$.optimisticLikeCount").value(2))
				.andExpect(jsonPath("$.optimisticUnlikeCount").value(0));

		mockMvc.perform(get("/api/v1/likes/COMMENT/" + targetUuid)
						.header("X-Member-UUID", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.likeType").value("COMMENT"))
				.andExpect(jsonPath("$.liked").value(false))
				.andExpect(jsonPath("$.likeCount").value(0));

		mockMvc.perform(get(storyPath))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.liked").value(false))
				.andExpect(jsonPath("$.likeCount").value(1));

		mockMvc.perform(get("/api/v1/likes/PLAN/" + targetUuid))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_LIKE_TYPE"));
	}

	@Test
	void batchSnapshotSupportsCommentListAndRejectsOverflow() throws Exception {
		UUID memberUuid = UUID.randomUUID();
		UUID firstComment = UUID.randomUUID();
		UUID secondComment = UUID.randomUUID();

		mockMvc.perform(put("/api/v1/likes/COMMENT/" + firstComment)
						.header("X-Member-UUID", memberUuid))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/likes/snapshots")
						.header("X-Member-UUID", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"likeType":"COMMENT","targetUuids":["%s","%s"]}
								""".formatted(firstComment, secondComment)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.snapshots[0].likeType").value("COMMENT"))
				.andExpect(jsonPath("$.snapshots[0].targetUuid").value(firstComment.toString()))
				.andExpect(jsonPath("$.snapshots[0].liked").value(true))
				.andExpect(jsonPath("$.snapshots[0].likeCount").value(1))
				.andExpect(jsonPath("$.snapshots[0].optimisticUnlikeCount").value(0))
				.andExpect(jsonPath("$.snapshots[1].targetUuid").value(secondComment.toString()))
				.andExpect(jsonPath("$.snapshots[1].liked").value(false))
				.andExpect(jsonPath("$.snapshots[1].likeCount").value(0))
				.andExpect(jsonPath("$.snapshots[1].optimisticLikeCount").value(1));

		StringJoiner overflow = new StringJoiner("\",\"", "{\"likeType\":\"COMMENT\",\"targetUuids\":[\"", "\"]}");
		for (int index = 0; index < 51; index++) {
			overflow.add(UUID.randomUUID().toString());
		}
		mockMvc.perform(post("/api/v1/likes/snapshots")
						.header("X-Member-UUID", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content(overflow.toString()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_LIKE_TARGET"));
	}
}
