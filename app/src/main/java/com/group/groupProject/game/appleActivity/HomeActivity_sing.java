package com.group.groupProject.game.appleActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.group.groupProject.R;

public class HomeActivity_sing extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_sing);

        Button startGameButton = findViewById(R.id.start_game_button);

        startGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity_sing.this, AppleActivity_sing.class);
            startActivity(intent);
            finish();
        });
    }
}
