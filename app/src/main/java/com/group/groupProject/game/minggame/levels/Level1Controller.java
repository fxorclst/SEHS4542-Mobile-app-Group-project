package com.group.groupProject.game.minggame.levels;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.group.groupProject.game.minggame.BaseGameController;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Level1Controller extends BaseGameController {

    private static final int LEVEL_INDEX = 1;
    private static final String LEVEL_TITLE = "Drawing the line!";
    private static final String LEVEL_DESCRIPTION = "Draw a line from start to end!";

    public Level1Controller() {
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
        return 3;
    }

    @Override
    protected Map<Integer, String> getHintThresholdMap() {
        Map<Integer, String> thresholdMap = new LinkedHashMap<>();
        thresholdMap.put(9, "Hint C: Follow the shortest path to the end node first, then refine your line.");
        thresholdMap.put(5, "Hint B: Draw one single line; avoid lifting your finger midway.");
        thresholdMap.put(3, "Hint A: Start from the highlighted start node.");
        return thresholdMap;
    }

    @Override
    protected boolean allowRotation() {
        return true;
    }

    @Override
    protected int getLevelScore() {
        int baseScore = 100;
        int mistakePenalty = 10;
        int score = baseScore - (getMistakeCount() * mistakePenalty);
        return Math.max(score, 0);
    }


    @Override
    protected View createGameContent(LayoutInflater inflater, ViewGroup parent) {
        LineDrawView lineDrawView = new LineDrawView(requireHost());
        lineDrawView.setOnDrawResultListener(result -> {
            switch (result) {
                case CORRECT_END:
                    showLevelClearDialog();
                    break;
                case FAKE_END:
                case NOWHERE:
                    addMistake();
                    lineDrawView.reset();
                    break;
            }
        });
        return lineDrawView;
    }
}

