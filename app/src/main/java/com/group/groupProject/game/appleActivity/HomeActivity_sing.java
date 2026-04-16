package com.group.groupProject.game.appleActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.group.groupProject.R;
import com.group.groupProject.core.MainActivity;

public class HomeActivity_sing extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_sing);

        Button startGameButton = findViewById(R.id.start_game_button);

        Button backHomePageButton = findViewById(R.id.go_back_to_main_page);

        startGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity_sing.this, AppleActivity_sing.class);
            startActivity(intent);
            finish();
        });

        backHomePageButton.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity_sing.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
