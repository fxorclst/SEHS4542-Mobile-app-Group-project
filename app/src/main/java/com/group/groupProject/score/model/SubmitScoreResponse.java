package com.group.groupProject.score.model;

import com.google.gson.annotations.SerializedName;

public class SubmitScoreResponse {

    @SerializedName("success")
    public boolean success;

    @SerializedName("id")
    public String id;

    @SerializedName("timestamp")
    public String timestamp;

    @SerializedName(value = "playName", alternate = {"playerName"})
    public String playerName;

    @SerializedName("groupId")
    public String groupId;

    @SerializedName("score")
    public int score;

    @SerializedName("level")
    public int level;

    @SerializedName("rank")
    public Integer rank;

    @SerializedName("metadata")
    public String metadata;
}

