package com.group.groupProject.score.model;

import com.google.gson.annotations.SerializedName;

public class SubmitScoreRequest {

    @SerializedName("playerName")
    public String playerName;

    @SerializedName("score")
    public int score;

    @SerializedName("level")
    public int level;

    @SerializedName("metadata")
    public String metadata;
}

