package com.planwith.planwith_fo_like.application.port.in;

import java.util.List;

import com.planwith.planwith_fo_like.application.query.GetLikeSnapshotQuery;
import com.planwith.planwith_fo_like.application.query.GetLikeSnapshotsQuery;
import com.planwith.planwith_fo_like.application.query.LikeSnapshotView;

public interface GetLikeSnapshotQueryUseCase {

	LikeSnapshotView get(GetLikeSnapshotQuery query);

	List<LikeSnapshotView> getAll(GetLikeSnapshotsQuery query);
}
