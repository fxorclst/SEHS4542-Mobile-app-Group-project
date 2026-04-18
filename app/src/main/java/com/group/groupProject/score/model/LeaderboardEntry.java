package com.group.groupProject.score.model;

import com.google.gson.annotations.SerializedName;

public class LeaderboardEntry {

    @SerializedName("id")
    public String id;

    @SerializedName("timestamp")
    public String timestamp;

    @SerializedName("playerName")
    public String playerName;

    @SerializedName("groupId")
    public String groupId;

    @SerializedName("score")
    public int score;

    @SerializedName("level")
    public int level;

    @SerializedName("metadata")
    public String metadata;

    @SerializedName("rank")
    public int rank;
}

