package com.group.groupProject.game.colorGame;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.group.groupProject.R;
import com.group.groupProject.core.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ColorBrainGameActivity extends AppCompatActivity {
    private static final long QUESTION_TIME_MILLIS = 30000;

    private final Map<String, Integer> colorMap = new HashMap<>();
    private final Random random = new Random();
    private final List<ColorOption> colorOptions = new ArrayList<>();

    private Button btn_1, btn_2, btn_3, btn_4;
    private ImageButton btnPause;
    private TextView tv_question, tv_score, tvStageTitle;
    private ProgressBar progressBar;

    private int score = 0;
    private int correctIndex;
    private String difficulty;
    private boolean isGameOver = false;
    private boolean isPaused = false;
    private boolean timerRunning = false;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = QUESTION_TIME_MILLIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_brain_game);

        initViews();
        initGame();
        setupListeners();
        startGame();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseGame();
        if (!isGameOver && timerRunning) {
            cancelTimer();
            isPaused = true;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();

        isPaused = false;
        timerRunning = false;
    }

    private void initViews() {
        btn_1 = findViewById(R.id.btn_red);
        btn_2 = findViewById(R.id.btn_blue);
        btn_3 = findViewById(R.id.btn_green);
        btn_4 = findViewById(R.id.btn_yellow);

        btnPause = findViewById(R.id.btn_pause);

        tv_question = findViewById(R.id.tv_question);
        tv_score = findViewById(R.id.tv_score);
        tvStageTitle = findViewById(R.id.tv_stage_title);

        progressBar = findViewById(R.id.progress_bar);
    }

    private void initGame() {
        difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) {
            difficulty = "easy";
        }

        setBtnColor();

        tv_score.setText("Score: 0");
        tvStageTitle.setText("Color Brain - " + capitalize(difficulty));

        progressBar.setMax((int) (QUESTION_TIME_MILLIS / 1000));
        progressBar.setProgress((int) (QUESTION_TIME_MILLIS / 1000));
    }

    private void setupListeners() {
        btnPause.setOnClickListener(view -> pauseGame());
    }

    private void startGame() {
        if ("easy".equalsIgnoreCase(difficulty)) {
            setEasyQuestion();
        } else {
            generateHardQuestion();
        }
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

    private void pauseGame() {
        if (isGameOver || isPaused) return;

        if (timerRunning) {
            cancelTimer();
            isPaused = true;
        }

        new AlertDialog.Builder(ColorBrainGameActivity.this)
                .setTitle("Paused")
                .setMessage("Tap below to continue.")
                .setCancelable(false)
                .setPositiveButton("Continue Play", (dialog, which) -> {
                    resumeGame();
                    dialog.dismiss();
                })
                .setNegativeButton("Back Home", (dialog, which) -> {
                    goToMainActivity();
                })
                .show();
    }

    private void resumeGame() {
        if (isPaused && timeLeftInMillis > 0 && !isGameOver) {
            startTimer(timeLeftInMillis);
            isPaused = false;
        }
    }

    public void setColor(Button btn, String color) {
        if (color == null) color = "";

        Integer colorResId = colorMap.get(color.toLowerCase());
        if (colorResId == null) {
            colorResId = android.R.color.white;
        }

        int realColor = ContextCompat.getColor(this, colorResId);
        btn.setBackgroundTintList(ColorStateList.valueOf(realColor));
        btn.setTextColor(realColor);
    }

    public void setBtnColor() {
        colorMap.put("red", R.color.red);
        colorMap.put("blue", R.color.blue);
        colorMap.put("green", R.color.green);
        colorMap.put("yellow", R.color.yellow);
    }

    private void setEasyQuestion() {
        startNewQuestionTimer();

        List<String> words = new ArrayList<>(Arrays.asList("red", "blue", "green", "yellow"));
        java.util.Collections.shuffle(words);

        btn_1.setText(words.get(0));
        btn_2.setText(words.get(1));
        btn_3.setText(words.get(2));
        btn_4.setText(words.get(3));

        setColor(btn_1, words.get(0));
        setColor(btn_2, words.get(1));
        setColor(btn_3, words.get(2));
        setColor(btn_4, words.get(3));

        String question = words.get(random.nextInt(words.size()));
        String questionColor = getRandomColor();

        tv_question.setText(question);

        Integer colorResId = colorMap.get(questionColor);
        if (colorResId != null) {
            tv_question.setTextColor(ContextCompat.getColor(this, colorResId));
        }
    }

    private String getRandomColor() {
        List<String> colors = Arrays.asList("red", "blue", "green", "yellow");
        return colors.get(random.nextInt(colors.size()));
    }

    public void submitAnswer(View view) {
        if (isGameOver || isPaused) return;

        if ("easy".equalsIgnoreCase(difficulty)) {
            Button btn = (Button) view;
            boolean result = tv_question.getText().toString()
                    .equalsIgnoreCase(btn.getText().toString());

            if (result) {
                score += 10;
                tv_score.setText("Score: " + score);
                setEasyQuestion();
            } else {
                gameOver();
            }

        } else {
            int selectedIndex = (int) view.getTag();

            if (selectedIndex == correctIndex) {
                score += 10;
                tv_score.setText("Score: " + score);
                generateHardQuestion();
            } else {
                gameOver();
            }
        }
    }

    private void gameOver() {
        if (isGameOver) return;

        isGameOver = true;
        cancelTimer();

        Intent intent = new Intent(this, ScoreActivity.class);
        intent.putExtra("score", String.valueOf(score));
        intent.putExtra("difficulty", difficulty);
        intent.putExtra("game", "brain");
        startActivity(intent);
        finish();
    }

    private ColorOption generateRandomColor() {
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return new ColorOption(r, g, b);
    }

    public static class ColorOption {
        int r, g, b;

        public ColorOption(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public int toColorInt() {
            return Color.rgb(r, g, b);
        }

        public String toRgbText() {
            return "(" + r + ", " + g + ", " + b + ")";
        }
    }

    private void generateHardQuestion() {
        startNewQuestionTimer();
        colorOptions.clear();

        for (int i = 0; i < 4; i++) {
            colorOptions.add(generateRandomColor());
        }

        Button[] buttons = {btn_1, btn_2, btn_3, btn_4};

        for (int i = 0; i < 4; i++) {
            ColorOption option = colorOptions.get(i);
            buttons[i].setText("");
            buttons[i].setBackgroundTintList(
                    ColorStateList.valueOf(option.toColorInt())
            );
            buttons[i].setTag(i);
        }

        correctIndex = random.nextInt(4);
        tv_question.setText(colorOptions.get(correctIndex).toRgbText());
        tv_question.setTextColor(Color.BLACK);
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private void goToMainActivity() {
        cancelTimer();
//        Intent intent = new Intent(ColorBrainGameActivity.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        startActivity(intent);
        finish();
    }


}