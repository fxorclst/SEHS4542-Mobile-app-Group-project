package com.group.groupProject.score.repository;

import com.group.groupProject.score.model.LeaderboardResponse;
import com.group.groupProject.score.model.SubmitScoreRequest;
import com.group.groupProject.score.model.SubmitScoreResponse;
import com.group.groupProject.score.remote.ScoreboardApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScoreboardRepositoryImpl implements ScoreboardRepository {

    private final ScoreboardApiService apiService;

    public ScoreboardRepositoryImpl(ScoreboardApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void getTopScores(RepositoryCallback<LeaderboardResponse> callback) {
        enqueueLeaderboardCall(apiService.getTopScores(), callback);
    }

    @Override
    public void getTopScoresByLevel(int level, RepositoryCallback<LeaderboardResponse> callback) {
        enqueueLeaderboardCall(apiService.getTopScores(level), callback);
    }

    @Override
    public void submitScore(String groupId, SubmitScoreRequest request, RepositoryCallback<SubmitScoreResponse> callback) {
        if (groupId == null || groupId.trim().isEmpty()) {
            callback.onError("Group id is missing.", null);
            return;
        }
        apiService.submitScore("Bearer " + groupId, request).enqueue(new Callback<SubmitScoreResponse>() {
            @Override
            public void onResponse(Call<SubmitScoreResponse> call, Response<SubmitScoreResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Submit failed: HTTP " + response.code(), null);
                }
            }

            @Override
            public void onFailure(Call<SubmitScoreResponse> call, Throwable t) {
                callback.onError(t.getMessage(), t);
            }
        });
    }

    private void enqueueLeaderboardCall(
            Call<LeaderboardResponse> call,
            RepositoryCallback<LeaderboardResponse> callback
    ) {
        call.enqueue(new Callback<LeaderboardResponse>() {
            @Override
            public void onResponse(Call<LeaderboardResponse> call, Response<LeaderboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Leaderboard request failed: HTTP " + response.code(), null);
                }
            }

            @Override
            public void onFailure(Call<LeaderboardResponse> call, Throwable t) {
                callback.onError(t.getMessage(), t);
            }
        });
    }
}

