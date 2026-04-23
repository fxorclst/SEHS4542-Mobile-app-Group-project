package com.group.groupProject.game.colorGame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.group.groupProject.R;
import com.group.groupProject.core.MainActivity;

import java.util.Objects;

public class ScoreActivity extends AppCompatActivity {
    TextView tv_score;
    Button btn_playAgain, btn_backToHome, btn_difficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        tv_score = findViewById(R.id.tv_total_score);

        btn_playAgain = findViewById(R.id.btn_play_again);
        btn_backToHome = findViewById(R.id.btn_back_to_home);
        btn_difficulty = findViewById(R.id.btn_difficulty);

        Intent intent = getIntent();

        String score = intent.getStringExtra("score");
        String difficulty = intent.getStringExtra("difficulty");
        String gameName = intent.getStringExtra("game");

        tv_score.setText(score);

        btn_backToHome.setOnClickListener(view -> {
//            Intent intent_back_home = new Intent(this, MainActivity.class);
//            startActivity(intent_back_home);
            finish();
        });

        btn_playAgain.setOnClickListener(view -> {
            Intent intent_start_again = Objects.equals(gameName, "memory")?
                    new Intent(this, ColorMemoryGameActivity.class):
                    new Intent(this, ColorBrainGameActivity.class);

            intent_start_again.putExtra("difficulty", difficulty);
            startActivity(intent_start_again);
            finish();
        });

        btn_difficulty.setOnClickListener(view -> {
            Intent intent_difficulty = new Intent(this, ColorGameDifficulty.class);
            intent_difficulty.putExtra("game",gameName);
            startActivity(intent_difficulty);
            finish();
        });
    }
}