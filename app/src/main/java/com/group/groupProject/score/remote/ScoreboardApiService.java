package com.group.groupProject.score.remote;

import com.group.groupProject.score.model.LeaderboardResponse;
import com.group.groupProject.score.model.SubmitScoreRequest;
import com.group.groupProject.score.model.SubmitScoreResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ScoreboardApiService {

    @POST("leaderboard/submit")
    Call<SubmitScoreResponse> submitScore(
            @Header("Authorization") String authorization,
            @Body SubmitScoreRequest request
    );

    @GET("leaderboard/top")
    Call<LeaderboardResponse> getTopScores();

    @GET("leaderboard/top")
    Call<LeaderboardResponse> getTopScores(@Query("level") int level);
}

