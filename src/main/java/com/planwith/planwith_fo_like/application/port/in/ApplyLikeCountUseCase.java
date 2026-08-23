package com.planwith.planwith_fo_like.application.port.in;

import com.planwith.planwith_fo_like.application.command.LikeCountApplyCommand;

public interface ApplyLikeCountUseCase {

	void apply(LikeCountApplyCommand command);
}
