package com.group.groupProject.score.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LeaderboardResponse {

    @SerializedName("entries")
    public List<LeaderboardEntry> entries;

    @SerializedName("total")
    public int total;
}

