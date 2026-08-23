package com.planwith.planwith_fo_like.application.port.in;

import com.planwith.planwith_fo_like.application.query.GetMyLikeStatusQuery;
import com.planwith.planwith_fo_like.application.query.LikeStatusView;

public interface GetMyLikeStatusQueryUseCase {

	LikeStatusView get(GetMyLikeStatusQuery query);
}
