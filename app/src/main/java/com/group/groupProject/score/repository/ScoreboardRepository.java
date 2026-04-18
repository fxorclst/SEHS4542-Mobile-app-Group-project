package com.group.groupProject.score.repository;

import com.group.groupProject.score.model.LeaderboardResponse;
import com.group.groupProject.score.model.SubmitScoreRequest;
import com.group.groupProject.score.model.SubmitScoreResponse;

public interface ScoreboardRepository {
    void getTopScores(RepositoryCallback<LeaderboardResponse> callback);

    void getTopScoresByLevel(int level, RepositoryCallback<LeaderboardResponse> callback);

    void submitScore(String groupId, SubmitScoreRequest request, RepositoryCallback<SubmitScoreResponse> callback);
}

