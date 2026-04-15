package com.group.groupProject.game.minggame.levels;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.group.groupProject.game.minggame.BaseGameController;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Level2Controller extends BaseGameController {

    private static final int LEVEL_INDEX = 2;
    private static final String LEVEL_TITLE = "Rooftop Escape";
    private static final String LEVEL_DESCRIPTION = "Find all 'safe' ways to return to the ground.";

    private int latestLevelScore = 100;
    private boolean resultDelivered;

    public Level2Controller() {
    }

    @Override
    protected int getLevelIndex() {
        return LEVEL_INDEX;
    }

    @Override
    protected String getLevelTitle() {
        return LEVEL_TITLE;
    }

    @Override
    protected String getLevelDescription() {
        return LEVEL_DESCRIPTION;
    }

    @Override
    protected int getHintMinimumMistakes() {
        return 2;
    }

    @Override
    protected Map<Integer, String> getHintThresholdMap() {
        Map<Integer, String> thresholdMap = new LinkedHashMap<>();
        thresholdMap.put(6, "The board is not just for show; it can be interacted with.");
        thresholdMap.put(4, "Do you tap on windows?");
        thresholdMap.put(2, "Try move something around?");
        return thresholdMap;
    }

    @Override
    protected int getLevelScore() {
        return latestLevelScore;
    }

    public void finishLevelCleared() {
        if (resultDelivered) {
            return;
        }
        resultDelivered = true;
        latestLevelScore = Math.max(100 - (getMistakeCount() * 5), 0);
        showLevelClearDialog();
    }

    public void finishLevelFailed(int unfoundItemCount) {
        if (resultDelivered) {
            return;
        }
        resultDelivered = true;
        latestLevelScore = Math.max(100 - (unfoundItemCount * 10) - (getMistakeCount() * 5), 0);
        showLevelFailedDialog();
    }

    public void recordMistake() {
        addMistake();
    }

    @Override
    protected View createGameContent(LayoutInflater inflater, ViewGroup parent) {
        return new Level2RooftopPuzzleView(requireHost(), this);
    }
}

