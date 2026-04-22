package com.group.groupProject.funmath.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderboardManager {
    private static final String PREFS_NAME = "funmath_leaderboard";
    private static final String KEY_SCORES = "scores";
    private static LeaderboardManager instance;
    private SharedPreferences prefs;

    private LeaderboardManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized LeaderboardManager getInstance(Context context) {
        if (instance == null) {
            instance = new LeaderboardManager(context.getApplicationContext());
        }
        return instance;
    }

    public void addScore(String username, int level, int score, String time) {
        List<ScoreEntry> scores = getAllScores();
        scores.add(new ScoreEntry(username, level, score, time));
        saveScores(scores);
    }

    public List<ScoreEntry> getTopScores(int level, int limit) {
        List<ScoreEntry> scores = getAllScores();
        List<ScoreEntry> filtered = new ArrayList<>();

        for (ScoreEntry entry : scores) {
            if (entry.level == level) {
                filtered.add(entry);
            }
        }

        Collections.sort(filtered, new Comparator<ScoreEntry>() {
            @Override
            public int compare(ScoreEntry a, ScoreEntry b) {
                return b.score - a.score;
            }
        });

        if (filtered.size() > limit) {
            return filtered.subList(0, limit);
        }
        return filtered;
    }

    public List<ScoreEntry> getAllScoresForLevel(int level) {
        return getTopScores(level, 100);
    }

    private List<ScoreEntry> getAllScores() {
        String json = prefs.getString(KEY_SCORES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<ScoreEntry>>(){}.getType();
        return new Gson().fromJson(json, type);
    }

    private void saveScores(List<ScoreEntry> scores) {
        String json = new Gson().toJson(scores);
        prefs.edit().putString(KEY_SCORES, json).apply();
    }

    public static class ScoreEntry {
        public String username;
        public int level;
        public int score;
        public String time;

        ScoreEntry(String username, int level, int score, String time) {
            this.username = username;
            this.level = level;
            this.score = score;
            this.time = time;
        }
    }
}