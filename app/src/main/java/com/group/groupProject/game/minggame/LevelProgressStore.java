package com.group.groupProject.game.minggame;

import android.content.Context;
import android.content.SharedPreferences;

public final class LevelProgressStore {

    private static final String PREFS_NAME = "level_progress";
    public static final String KEY_MAX_UNLOCK_LEVEL = "MAX_UNLOCK_LEVEL";
    public static final int TOTAL_LEVEL_COUNT = 2;
    private static final int DEFAULT_MAX_UNLOCK_LEVEL = 1;

    private LevelProgressStore() {
        // Utility class.
    }

    public static int getMaxUnlockedLevel(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int maxUnlockedLevel = preferences.getInt(KEY_MAX_UNLOCK_LEVEL, DEFAULT_MAX_UNLOCK_LEVEL);
        if (maxUnlockedLevel < DEFAULT_MAX_UNLOCK_LEVEL) {
            maxUnlockedLevel = DEFAULT_MAX_UNLOCK_LEVEL;
        } else if (maxUnlockedLevel > TOTAL_LEVEL_COUNT) {
            maxUnlockedLevel = TOTAL_LEVEL_COUNT;
        }
        if (preferences.getInt(KEY_MAX_UNLOCK_LEVEL, DEFAULT_MAX_UNLOCK_LEVEL) != maxUnlockedLevel) {
            preferences.edit().putInt(KEY_MAX_UNLOCK_LEVEL, maxUnlockedLevel).apply();
        }
        return maxUnlockedLevel;
    }

    public static void ensureInitialized(Context context) {
        getMaxUnlockedLevel(context);
    }

    public static boolean isLevelUnlocked(Context context, int levelNumber) {
        return levelNumber <= getMaxUnlockedLevel(context);
    }

    public static void unlockLevel(Context context, int levelNumber) {
        if (levelNumber < DEFAULT_MAX_UNLOCK_LEVEL) {
            return;
        }

        if (levelNumber > TOTAL_LEVEL_COUNT) {
            levelNumber = TOTAL_LEVEL_COUNT;
        }

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int currentMaxUnlockedLevel = getMaxUnlockedLevel(context);
        if (levelNumber > currentMaxUnlockedLevel) {
            preferences.edit().putInt(KEY_MAX_UNLOCK_LEVEL, levelNumber).apply();
        }
    }
}


