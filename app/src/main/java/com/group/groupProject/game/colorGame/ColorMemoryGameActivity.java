package com.group.groupProject.game.colorGame;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import androidx.gridlayout.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.group.groupProject.R;
import com.group.groupProject.core.MainActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ColorMemoryGameActivity extends AppCompatActivity {
    private static final long QUESTION_TIME_MILLIS = 30000;

    private ImageButton btnPause;
    private TextView tvStageTitle, tvScore;
    private ProgressBar progressBar;
    private GridLayout gridCards;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = QUESTION_TIME_MILLIS;
    private boolean timerRunning = false;
    private boolean isPaused = false;
    private boolean isGameOver = false;
    private int score = 0;
    private int stage = 1;

    private final List<Button> cardButtons = new ArrayList<>();
    private final List<Integer> colors = new ArrayList<>();
    private Button firstSelected = null;
    private Button secondSelected = null;
    private boolean isBusy = false;
    private int matchedPairs = 0;
    private String difficulty;
    private int pairCount;

    private boolean showResumePopup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_memory_game);

        initViews();
        setupUI();
        setupListeners();
        startStage();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!isGameOver && timerRunning) {
            cancelTimer();
            isPaused = true;
            showResumePopup = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (showResumePopup && isPaused && !isGameOver && timeLeftInMillis > 0) {
            gamePause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
    }

    private void gamePause() {
        new AlertDialog.Builder(ColorMemoryGameActivity.this)
                .setTitle("Game Paused")
                .setMessage("Tap below to continue.")
                .setCancelable(false)
                .setPositiveButton("Continue Play", (dialog, which) -> {
                    resumeGame();
                    showResumePopup = false;
                    dialog.dismiss();
                })
                .setNegativeButton("Back to Home",(dialog,which)->{
                    goToMainActivity();
                })
                .show();
    }

    private void goToMainActivity() {
        cancelTimer();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void resumeGame() {
        if (isPaused && timeLeftInMillis > 0 && !isGameOver) {
            startTimer(timeLeftInMillis);
            isPaused = false;
        }
    }

    private void initViews() {
        btnPause = findViewById(R.id.btn_pause);
        tvStageTitle = findViewById(R.id.tv_stage_title);
        tvScore = findViewById(R.id.tv_score);
        progressBar = findViewById(R.id.progress_time);
        gridCards = findViewById(R.id.grid_cards);

        difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) {
            difficulty = "easy";
        }

        pairCount = getPairCountByDifficulty(difficulty);
    }

    private int getPairCountByDifficulty(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "easy":
                return 6;
            case "normal":
                return 10;
            case "hard":
                return 20;
            case "hell":
                return 51;
            default:
                return 6;
        }
    }

    private void setupUI() {
        tvStageTitle.setText("Color Memory - Stage " + stage+"("+difficulty+")");
        tvScore.setText("Score: " + score);

        progressBar.setMax((int) (QUESTION_TIME_MILLIS / 1000));
        progressBar.setProgress((int) (QUESTION_TIME_MILLIS / 1000));
    }

    private void setupListeners() {
        btnPause.setOnClickListener(v -> {
            if (isGameOver || isPaused) return;

            if (timerRunning) {
                cancelTimer();
                isPaused = true;
            }

            showResumePopup = false;
            gamePause();
        });
    }

    private void startStage() {
        matchedPairs = 0;
        firstSelected = null;
        secondSelected = null;
        isBusy = false;

        tvStageTitle.setText("Color Memory - Stage " + stage);
        createCards();
        startNewQuestionTimer();
    }

    private void createCards() {
        gridCards.removeAllViews();
        cardButtons.clear();
        colors.clear();

        setupGridSize(pairCount);

        List<Integer> baseColors = generateColorPairs(pairCount);
        colors.addAll(baseColors);
        Collections.shuffle(colors);

        for (int i = 0; i < colors.size(); i++) {
            Button card = new Button(this);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = getCardSize(pairCount);
            params.height = getCardSize(pairCount);
            params.setMargins(8, 8, 8, 8);
            card.setLayoutParams(params);

            card.setBackgroundColor(Color.LTGRAY);
            card.setTag(colors.get(i));

            int index = i;
            card.setOnClickListener(v -> onCardClicked(card, index));

            cardButtons.add(card);
            gridCards.addView(card);
        }
    }

    private List<Integer> generateColorPairs(int pairCount) {
        List<Integer> list = new ArrayList<>();

        for (int i = 0; i < pairCount; i++) {
            int color = Color.rgb(
                    50 + (int)(Math.random() * 206),
                    50 + (int)(Math.random() * 206),
                    50 + (int)(Math.random() * 206)
            );
            list.add(color);
            list.add(color);
        }

        return list;
    }

    private void setupGridSize(int pairCount) {
        int totalCards = pairCount * 2;

        if (totalCards <= 12) {
            gridCards.setColumnCount(4);
        } else if (totalCards <= 20) {
            gridCards.setColumnCount(4);
        } else if (totalCards <= 40) {
            gridCards.setColumnCount(5);
        } else {
            gridCards.setColumnCount(6);
        }

        int columns = gridCards.getColumnCount();
        int rows = (int) Math.ceil((double) totalCards / columns);
        gridCards.setRowCount(rows);
    }

    private int getCardSize(int pairCount) {
        if (pairCount <= 6) {
            return 160;
        } else if (pairCount <= 10) {
            return 130;
        } else if (pairCount <= 20) {
            return 100;
        } else {
            return 80;
        }
    }

    private void onCardClicked(Button card, int index) {
        if (isPaused || isBusy || isGameOver) return;
        if (card == firstSelected || card.getVisibility() != Button.VISIBLE) return;

        int color = (int) card.getTag();
        card.setBackgroundColor(color);

        if (firstSelected == null) {
            firstSelected = card;
            return;
        }

        secondSelected = card;
        isBusy = true;

        card.postDelayed(this::checkMatch, 500);
    }

    private void checkMatch() {
        if (firstSelected == null || secondSelected == null) {
            isBusy = false;
            return;
        }

        int firstColor = (int) firstSelected.getTag();
        int secondColor = (int) secondSelected.getTag();

        if (firstColor == secondColor) {
            firstSelected.setVisibility(Button.INVISIBLE);
            secondSelected.setVisibility(Button.INVISIBLE);

            matchedPairs++;
            score += 10;
            tvScore.setText("Score: " + score);

            if (matchedPairs == pairCount) {
                nextStage();
            }
        } else {
            firstSelected.setBackgroundColor(Color.LTGRAY);
            secondSelected.setBackgroundColor(Color.LTGRAY);
        }

        firstSelected = null;
        secondSelected = null;
        isBusy = false;
    }

    private void nextStage() {
        cancelTimer();
        stage++;
        startStage();
    }

    private void startNewQuestionTimer() {
        cancelTimer();
        timeLeftInMillis = QUESTION_TIME_MILLIS;
        progressBar.setProgress((int) (QUESTION_TIME_MILLIS / 1000));
        startTimer(timeLeftInMillis);
    }

    private void startTimer(long durationInMillis) {
        countDownTimer = new CountDownTimer(durationInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                timerRunning = true;

                int secondsLeft = (int) Math.ceil(millisUntilFinished / 1000.0);
                progressBar.setProgress(secondsLeft);
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                isPaused = false;
                timeLeftInMillis = 0;
                progressBar.setProgress(0);
                gameOver();
            }
        }.start();
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        timerRunning = false;
    }

    private void gameOver() {
        if (isGameOver) return;

        isGameOver = true;
        cancelTimer();

        Intent intent = new Intent(this, ScoreActivity.class);
        intent.putExtra("score", String.valueOf(score));
        intent.putExtra("difficulty", difficulty);
        intent.putExtra("game", "memory");
        startActivity(intent);
        finish();
    }



}