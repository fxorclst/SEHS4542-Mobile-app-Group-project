package com.group.groupProject.game.minggame;

import com.group.groupProject.game.minggame.levels.Level1Controller;
import com.group.groupProject.game.minggame.levels.Level2Controller;

public final class LevelControllerFactory {

    private LevelControllerFactory() {
        // Utility class.
    }

    public static BaseGameController create(int levelNumber) {
        switch (levelNumber) {
            case 1:
                return new Level1Controller();
            case 2:
                return new Level2Controller();
            default:
                return new Level1Controller();
        }
    }
}

