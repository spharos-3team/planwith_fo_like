package com.planwith.planwith_fo_like.application.port.in;

import com.planwith.planwith_fo_like.application.command.AddLikeCommand;
import com.planwith.planwith_fo_like.application.query.LikeCommandResult;

public interface AddLikeUseCase {

	LikeCommandResult add(AddLikeCommand command);
}
