package com.group.groupProject.game.funMath;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.group.groupProject.R;
import com.group.groupProject.core.MainActivity;
import com.group.groupProject.funmath.util.SoundManager;
import com.group.groupProject.score.model.SubmitScoreRequest;
import com.group.groupProject.score.model.SubmitScoreResponse;
import com.group.groupProject.score.remote.NetworkModule;
import com.group.groupProject.score.repository.RepositoryCallback;
import com.group.groupProject.score.repository.ScoreboardRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuizGameActivity extends AppCompatActivity {

    public static final String EXTRA_QUIZ_LEVEL = "quiz_level";

    private TextView tvQuestion;
    private final Button[] optionButtons = new Button[4];
    private TextView tvFeedback;
    private Button btnHome;
    private TextView tvLevel;
    private TextView tvTimer;
    private TextView tvScore;

    private int currentLevel;
    private int currentQuestion;
    private int score;
    private int correctAnswers;
    private boolean inputLocked;
    private Random random = new Random();
    private Handler handler = new Handler();
    private static final int QUESTIONS_PER_LEVEL = 10;
    private SoundManager soundManager;
    private int correctIndex;
    private long questionStartTime;
    private int elapsedSeconds = 0;
    private boolean timerRunning = true;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private ScoreboardRepository scoreboardRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_game);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        scoreboardRepository = NetworkModule.createRepository();

        currentLevel = getIntent().getIntExtra(EXTRA_QUIZ_LEVEL, 1);
        soundManager = SoundManager.getInstance(this);
        initViews();
        startLevel(currentLevel);
    }

    private void initViews() {
        tvQuestion = findViewById(R.id.tvQuestion);
        tvFeedback = findViewById(R.id.tvFeedback);
        tvLevel = findViewById(R.id.tvLevel);
        tvTimer = findViewById(R.id.tvTimer);
        tvScore = findViewById(R.id.tvScore);
        btnHome = findViewById(R.id.btnHome);
        int[] ids = {R.id.opt1, R.id.opt2, R.id.opt3, R.id.opt4};
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = findViewById(ids[i]);
            final int idx = i;
            optionButtons[i].setOnClickListener(v -> handleAnswer(idx));
        }
        btnHome.setOnClickListener(v -> {
            showExitConfirmation();
        });

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (timerRunning) {
                    elapsedSeconds++;
                    int mins = elapsedSeconds / 60;
                    int secs = elapsedSeconds % 60;
                    tvTimer.setText(String.format("%02d:%02d", mins, secs));
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void showExitConfirmation() {
        timerRunning = false;
        new AlertDialog.Builder(this)
                .setTitle("Exit Game")
                .setMessage("Are you sure you want to exit? Your progress will be lost.")
                .setPositiveButton("Exit", (dialog, which) -> {
                    Intent intent = new Intent(QuizGameActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    timerRunning = true;
                })
                .setCancelable(false)
                .show();
    }

    private void startLevel(int level) {
        currentLevel = level;
        currentQuestion = 0;
        score = 0;
        correctAnswers = 0;
        elapsedSeconds = 0;
        timerRunning = true;
        tvLevel.setText("Level " + level);
        tvScore.setText("Score: 0");
        tvTimer.setText("00:00");
        nextQuestion();
    }

    private void nextQuestion() {
        inputLocked = true;
        tvFeedback.setText("");
        questionStartTime = System.currentTimeMillis();

        for (int i = 0; i < 4; i++) {
            optionButtons[i].setEnabled(true);
        }

        Question q = generateQuestion(currentLevel);
        tvQuestion.setText(q.text);
        correctIndex = q.correctIndex;
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(String.valueOf(q.options[i]));
            optionButtons[i].setBackgroundColor(Color.parseColor("#4CAF50"));
        }
        currentQuestion++;
        inputLocked = false;
    }

    private void handleAnswer(int index) {
        if (inputLocked) return;
        inputLocked = true;

        long timeTaken = System.currentTimeMillis() - questionStartTime;

        if (index == correctIndex) {
            optionButtons[index].setBackgroundColor(Color.BLUE);
            correctAnswers++;
            soundManager.playCorrect();

            int baseScore = 100;
            int timeBonus = Math.max(0, 50 - (int)(timeTaken / 1000));
            int accuracyBonus = (correctAnswers * 10);
            score += baseScore + timeBonus + accuracyBonus;

            tvScore.setText("Score: " + score);
            tvFeedback.setText("Correct");

            handler.postDelayed(() -> {
                if (currentQuestion < QUESTIONS_PER_LEVEL) {
                    nextQuestion();
                } else {
                    showLevelCompleteDialog();
                }
            }, 1000);
        } else {
            optionButtons[index].setBackgroundColor(Color.RED);
            optionButtons[correctIndex].setBackgroundColor(Color.BLUE);
            soundManager.playWrong();

            score += 5;
            tvScore.setText("Score: " + score);
            tvFeedback.setText("Correct is the Blue one");

            handler.postDelayed(() -> {
                if (currentQuestion < QUESTIONS_PER_LEVEL) {
                    nextQuestion();
                } else {
                    showLevelCompleteDialog();
                }
            }, 1500);
        }
    }

    private void showLevelCompleteDialog() {
        timerRunning = false;
        boolean hasNext = currentLevel < 6;
        soundManager.playLevelComplete();

        String timeFormatted = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60);

        submitScore(timeFormatted);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Level Complete");
        builder.setMessage("Level " + currentLevel + " complete!\n\nScore: " + score + "\nTime: " + timeFormatted + "\nCorrect: " + correctAnswers + "/" + QUESTIONS_PER_LEVEL);

        if (hasNext) {
            builder.setPositiveButton("Next Level", (dialog, which) -> {
                Intent intent = new Intent(QuizGameActivity.this, QuizGameActivity.class);
                intent.putExtra(EXTRA_QUIZ_LEVEL, currentLevel + 1);
                startActivity(intent);
                finish();
            });
            builder.setNegativeButton("Home", (dialog, which) -> {
                Intent intent = new Intent(QuizGameActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        } else {
            builder.setPositiveButton("Home", (dialog, which) -> {
                Intent intent = new Intent(QuizGameActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }
        builder.setCancelable(false);
        builder.show();
    }

    private void submitScore(String timeFormatted) {
        if (user == null) {
            Toast.makeText(this, "Please login to submit score", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = user.getDisplayName();
        if (username == null || username.isEmpty()) {
            username = user.getEmail();
            if (username != null && username.contains("@")) {
                username = username.substring(0, username.indexOf("@"));
            }
        }

        String metadata = "Time: " + timeFormatted + ", Correct: " + correctAnswers + "/" + QUESTIONS_PER_LEVEL;

        SubmitScoreRequest request = new SubmitScoreRequest();
        request.playerName = username;
        request.score = score;
        request.level = currentLevel;
        request.metadata = metadata;

        String idToken = user.getIdToken(false).toString();

        scoreboardRepository.submitScore(idToken, request, new RepositoryCallback<SubmitScoreResponse>() {
            @Override
            public void onSuccess(SubmitScoreResponse data) {
                runOnUiThread(() -> {
                    Toast.makeText(QuizGameActivity.this, "Score submitted!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String message, Throwable throwable) {
                runOnUiThread(() -> {
                    Toast.makeText(QuizGameActivity.this, "Failed to submit score", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private Question generateQuestion(int level) {
        int min = 1, max = 9;
        boolean allowMul = false, allowDiv = false;
        switch (level) {
            case 1: min = 1; max = 9; allowMul = false; allowDiv = false; break;
            case 2: min = 1; max = 9; allowMul = true; allowDiv = true; break;
            case 3: min = 1; max = 50; allowMul = false; allowDiv = false; break;
            case 4: min = 1; max = 50; allowMul = true; allowDiv = true; break;
            case 5: min = 1; max = 99; allowMul = false; allowDiv = false; break;
            case 6: min = 1; max = 99; allowMul = true; allowDiv = true; break;
        }
        List<Integer> ops = new ArrayList<>();
        ops.add(0); ops.add(1);
        if (allowMul) ops.add(2);
        if (allowDiv) ops.add(3);
        int op = ops.get(random.nextInt(ops.size()));
        int a, b, answer;
        String opStr;
        if (op == 0) {
            a = randomInt(min, max);
            b = randomInt(min, max);
            answer = a + b;
            opStr = "+";
        } else if (op == 1) {
            a = randomInt(min, max);
            b = randomInt(min, max);
            answer = a - b;
            opStr = "-";
        } else if (op == 2) {
            a = randomInt(min, Math.min(max, 12));
            b = randomInt(min, Math.min(max, 12));
            answer = a * b;
            opStr = "*";
        } else {
            int denom = randomInt(1, Math.max(1, max / 2));
            int quot = randomInt(1, max / denom);
            a = denom * quot;
            b = denom;
            answer = quot;
            opStr = "/";
        }
        List<Integer> options = new ArrayList<>();
        options.add(answer);
        while (options.size() < 4) {
            int dist = random.nextInt(10) - 5;
            if (dist != 0 && !options.contains(answer + dist)) {
                options.add(answer + dist);
            }
        }
        Collections.shuffle(options);
        int correctIdx = options.indexOf(answer);
        int[] opts = new int[4];
        for (int i = 0; i < 4; i++) opts[i] = options.get(i);
        return new Question(a + " " + opStr + " " + b + " = ?", opts, correctIdx);
    }

    private int randomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    private static class Question {
        String text;
        int[] options;
        int correctIndex;
        Question(String text, int[] options, int correctIndex) {
            this.text = text;
            this.options = options;
            this.correctIndex = correctIndex;
        }
    }
}