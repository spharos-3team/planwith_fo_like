package com.planwith.planwith_fo_like.application.port.in;

import com.planwith.planwith_fo_like.application.query.GetTargetLikeCountQuery;
import com.planwith.planwith_fo_like.application.query.LikeCountView;

public interface GetTargetLikeCountQueryUseCase {

	LikeCountView get(GetTargetLikeCountQuery query);
}
