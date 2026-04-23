package com.group.groupProject.game.colorGame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.group.groupProject.R;
import com.group.groupProject.core.MainActivity;

public class ColorGameDifficulty extends AppCompatActivity {

    RadioGroup radioGroupDifficulty;
    RadioButton rb_Easy, rb_Normal, rb_Hard, rb_Hell;
    Button btn_StartGame, btn_back_to_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_game_difficulty);

        radioGroupDifficulty = findViewById(R.id.radioGroupDifficulty);
        rb_Easy = findViewById(R.id.rb_Easy);
        rb_Normal = findViewById(R.id.rb_Normal);
        rb_Hard = findViewById(R.id.rb_Hard);
        rb_Hell = findViewById(R.id.rb_Hell);
        btn_StartGame = findViewById(R.id.btn_StartGame);
        btn_back_to_home = findViewById(R.id.btn_back_to_home);

        Intent intent = getIntent();
        String gameName = intent.getStringExtra("game");

        if(!"memory".equalsIgnoreCase(gameName)){
            rb_Normal.setVisibility(View.GONE);
            rb_Hell.setVisibility(View.GONE);
        }

        btn_StartGame.setOnClickListener(view -> {
            String difficulty = "easy";

            if (rb_Normal.isChecked()) {
                difficulty = "normal";
            } else if (rb_Hard.isChecked()) {
                difficulty = "hard";
            } else if (rb_Hell.isChecked()) {
                difficulty = "hell";
            }

            Intent intent_game = "memory".equalsIgnoreCase(gameName)?
                    new Intent(ColorGameDifficulty.this, ColorMemoryGameActivity.class):
                    new Intent(ColorGameDifficulty.this, ColorBrainGameActivity.class);

            intent_game.putExtra("difficulty", difficulty);
            intent_game.putExtra("game", gameName);
            startActivity(intent_game);
            finish();
        });

        btn_back_to_home.setOnClickListener(view -> {
//            Intent intentBackHome = new Intent(this, MainActivity.class);
//            startActivity(intentBackHome);
            finish();
        });
    }
}