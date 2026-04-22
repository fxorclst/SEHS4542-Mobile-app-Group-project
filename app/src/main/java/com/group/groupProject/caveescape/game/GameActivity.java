package com.group.groupProject.caveescape.game;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.group.groupProject.R;
import com.group.groupProject.caveescape.model.LevelData;
import com.group.groupProject.caveescape.util.SoundManager;

public class GameActivity extends AppCompatActivity {

    public static final String EXTRA_LEVEL = "extra_level";

    private GameView gameView;
    private LevelData levelData;
    private int levelNumber;
    private float elapsedTime;
    private int score;
    private boolean isPaused = false;

    private Handler gameHandler;
    private Runnable gameRunnable;
    private static final int GAME_TICK_MS = 16;

    private SoundManager soundManager;
    
    private TextView tvLevelName, tvTime, tvMaxTime;
    private ImageButton btnPause, btnRestart;
    private FrameLayout gameContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cave_escape_game);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tvLevelName = findViewById(R.id.tvLevelName);
        tvTime = findViewById(R.id.tvTime);
        tvMaxTime = findViewById(R.id.tvMaxTime);
        btnPause = findViewById(R.id.btnPause);
        btnRestart = findViewById(R.id.btnRestart);
        gameContainer = findViewById(R.id.gameContainer);

        levelNumber = getIntent().getIntExtra(EXTRA_LEVEL, 1);
        levelData = LevelData.createLevel(levelNumber);

        soundManager = SoundManager.getInstance(this);
        gameHandler = new Handler(Looper.getMainLooper());

        setupUI();
        setupGameView();
    }

    private void setupUI() {
        tvLevelName.setText(getString(R.string.level, levelNumber) + " - " + levelData.getLevelName());
        tvTime.setText(getString(R.string.time, 0.0f));
        tvMaxTime.setText("/ " + levelData.getParTime() + "s par");

        btnPause.setOnClickListener(v -> {
            soundManager.playButtonClick();
            showPauseMenu();
        });
        btnRestart.setOnClickListener(v -> {
            soundManager.playButtonClick();
            restartLevel();
        });
    }

    private void setupGameView() {
        gameView = new GameView(this);
        gameContainer.addView(gameView);

        gameView.setLevel(levelData);
        gameView.setOnGameEventListener(new GameView.OnGameEventListener() {
            @Override
            public void onLevelComplete(float time, int calculatedScore) {
                elapsedTime = time;
                score = calculatedScore;
                soundManager.playFinishReached();
                showLevelCompleteDialog();
            }

            @Override
            public void onGameOver() {
                showGameOverDialog();
            }

            @Override
            public void onTimeUpdate(float time) {
                tvTime.setText(getString(R.string.time, time));
            }

            @Override
            public void onWallHit() {
                soundManager.playWallHit();
            }

            @Override
            public void onObstacleHit() {
                soundManager.playObstacleHit();
            }

            @Override
            public void onResetHit() {
                soundManager.playResetHit();
            }
        });

        gameRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPaused) {
                    float deltaTime = GAME_TICK_MS / 1000f;
                    gameView.update(deltaTime);
                }
                gameHandler.postDelayed(this, GAME_TICK_MS);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isPaused) {
            gameView.startGame();
            gameHandler.post(gameRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isPaused) {
            showPauseMenu();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameHandler.removeCallbacks(gameRunnable);
        if (gameView != null) {
            gameView.release();
        }
    }

    private void showPauseMenu() {
        isPaused = true;
        gameView.pauseGame();
        gameHandler.removeCallbacks(gameRunnable);

        new AlertDialog.Builder(this)
                .setTitle(R.string.paused)
                .setItems(new String[]{getString(R.string.resume), getString(R.string.retry), getString(R.string.back_to_menu)}, 
                    (dialog, which) -> {
                        switch (which) {
                            case 0:
                                resumeGame();
                                break;
                            case 1:
                                restartLevel();
                                break;
                            case 2:
                                finish();
                                break;
                        }
                    })
                .setCancelable(false)
                .show();
    }

    private void resumeGame() {
        isPaused = false;
        gameView.resumeGame();
        gameHandler.post(gameRunnable);
    }

    private void restartLevel() {
        isPaused = false;
        elapsedTime = 0;
        score = 0;
        tvTime.setText(getString(R.string.time, 0.0f));
        gameView.reset();
        gameHandler.post(gameRunnable);
    }

    private void showLevelCompleteDialog() {
        String message = getString(R.string.your_time, elapsedTime) + "\n" +
                        getString(R.string.score, score);

        new AlertDialog.Builder(this)
                .setTitle(R.string.level_complete)
                .setMessage(message)
                .setPositiveButton(R.string.retry, (dialog, which) -> {
                    restartLevel();
                })
                .setNegativeButton(R.string.back_to_menu, (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showGameOverDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Time's Up!")
                .setMessage("You ran out of time. Try again!")
                .setPositiveButton(R.string.retry, (dialog, which) -> {
                    restartLevel();
                })
                .setNegativeButton(R.string.back_to_menu, (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onBackPressed() {
        showPauseMenu();
    }
}