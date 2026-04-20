package com.group.groupProject.core;

public class LevelInfo {
    int levelNumber;
    int iconResId;
    String levelName;

    LevelInfo(int levelNumber, int iconResId, String levelName) {
        this.levelNumber = levelNumber;
        this.iconResId = iconResId;
        this.levelName = levelName;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getLevelName() {
        return levelName;
    }

}