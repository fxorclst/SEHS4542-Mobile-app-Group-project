package com.group.groupProject.core;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.group.groupProject.game.caveEscape.game.CaveEscapeGameActivity;
import com.group.groupProject.game.funMath.QuizGameActivity;
import com.group.groupProject.game.hideThePhoneGame.HideThePhoneGame;
import com.group.groupProject.game.appleActivity.HomeActivity_sing;
import com.group.groupProject.game.colorGame.ColorGameDifficulty;
import com.group.groupProject.R;
import com.group.groupProject.auth.LoginActivity;
import com.group.groupProject.game.mingGame.GameActivity;
import com.group.groupProject.game.mingGame.LevelSelectActivity;
import com.group.groupProject.game.saveTheCat.SaveTheCatGame;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    ImageView btn_logout;
    TextView tv_username;
    LinearLayout ll_game1, ll_game2, ll_game3, ll_game4, ll_game5, ll_game6, ll_game7, ll_game8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_logout = findViewById(R.id.imageView_logout);
        tv_username = findViewById(R.id.tv_username);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        ll_game1 = findViewById(R.id.ll_game1);
        ll_game2 = findViewById(R.id.ll_game2);
        ll_game3 = findViewById(R.id.ll_game3);
        ll_game4 = findViewById(R.id.ll_game4);
        ll_game5 = findViewById(R.id.ll_game5);
        ll_game6 = findViewById(R.id.ll_game6);
        ll_game7 = findViewById(R.id.ll_game7);
        ll_game8 = findViewById(R.id.ll_game8);

        if(user!=null){
            String name = user.getDisplayName();
            tv_username.setText(name);
        }else{
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

        btn_logout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });

        ll_game1.setOnClickListener(view -> {
            Intent intent = new Intent(this, ColorGameDifficulty.class);
            intent.putExtra("game", "brain");
            startActivity(intent);
            finish();
        });

        ll_game2.setOnClickListener(view -> {
            Intent intent = new Intent(this, ColorGameDifficulty.class);
            intent.putExtra("game", "memory");
            startActivity(intent);
            finish();
        });

        ll_game3.setOnClickListener(view -> {
            Intent intent = new Intent(this, HomeActivity_sing.class);
            startActivity(intent);
            finish();
        });

        ll_game4.setOnClickListener(view -> {
            Intent intent = new Intent(this, LevelSelectActivity.class);
            startActivity(intent);
            finish();
        });

        ll_game5.setOnClickListener(view -> {
            Intent intent = new Intent(this, HideThePhoneGame.class);
            startActivity(intent);
            finish();
        });

        ll_game6.setOnClickListener(view -> {
            Intent intent = new Intent(this, SaveTheCatGame.class);
            startActivity(intent);
            finish();
        });
        ll_game7.setOnClickListener(view -> {
            Intent intent = new Intent(this, QuizGameActivity.class);
            startActivity(intent);
            finish();
        });
        ll_game8.setOnClickListener(view -> {
            Intent intent = new Intent(this, CaveEscapeGameActivity.class);
            startActivity(intent);
            finish();
        });
    }
}