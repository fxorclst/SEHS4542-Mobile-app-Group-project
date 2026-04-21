package com.group.groupProject.game.mingGame;

import com.group.groupProject.game.mingGame.levels.Level1Controller;
import com.group.groupProject.game.mingGame.levels.Level2Controller;

public final class LevelControllerFactory {

    private LevelControllerFactory() {
        // Utility class.
    }

    public static BaseGameController create(int levelNumber) {
        switch (levelNumber) {
            case 4:
                return new Level1Controller();
            case 5:
                return new Level2Controller();
            default:
                return new Level1Controller();
        }
    }
}

