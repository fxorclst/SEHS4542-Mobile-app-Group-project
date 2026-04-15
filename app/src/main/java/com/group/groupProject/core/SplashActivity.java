package com.group.groupProject.core;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import com.group.groupProject.R;
import com.group.groupProject.game.colorGame.ColorBrainGameActivity;
import com.group.groupProject.game.mingGame.GameActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, GameActivity.class);
            startActivity(intent);
            finish();
        },2000);
    }
}