package com.planwith.planwith_fo_like.application.port.in;

import com.planwith.planwith_fo_like.application.command.RemoveLikeCommand;
import com.planwith.planwith_fo_like.application.query.LikeCommandResult;

public interface RemoveLikeUseCase {

	LikeCommandResult remove(RemoveLikeCommand command);
}
