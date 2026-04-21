package com.group.groupProject.core;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.group.groupProject.R;
import com.group.groupProject.auth.LoginActivity;
import com.group.groupProject.game.mingGame.GameActivity;
import com.group.groupProject.score.LeaderboardActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private TextView tv_username;

    private TextView progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        tv_username = findViewById(R.id.tv_username);
        progressText = findViewById(R.id.txt_unlock_progress);

        String name = user.getDisplayName();
        tv_username.setText(name != null ? name : "User");

        findViewById(R.id.go_to_scoreboard).setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), LeaderboardActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.start_game).setOnClickListener(view -> {
            Toast.makeText(this, "Start Game Clicked", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.log_out).setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });

        LevelProgressStore.ensureInitialized(this);
        refreshLevelGrid();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLevelGrid();
    }

    private void refreshLevelGrid() {
        int maxUnlockedLevel = LevelProgressStore.getMaxUnlockedLevel(this);
        progressText.setText(getString(
                R.string.level_select_progress,
                maxUnlockedLevel,
                LevelProgressStore.TOTAL_LEVEL_COUNT
        ));
        populateLevelGrid(maxUnlockedLevel);
    }

    private void populateLevelGrid(int maxUnlockedLevel) {
        GridLayout gridLayout = findViewById(R.id.grid_levels);
        gridLayout.removeAllViews();

        ArrayList<LevelInfo> levelInfos = new ArrayList<>();
        levelInfos.add(new LevelInfo(1, R.drawable.game_ic1,"Color Brain"));
        levelInfos.add(new LevelInfo(2, R.drawable.game_ic1,"Memory"));
        levelInfos.add(new LevelInfo(3, R.drawable.game_ic3,"Apple Game"));
        levelInfos.add(new LevelInfo(4, R.drawable.game_ic4,"Drawing"));
        levelInfos.add(new LevelInfo(5, R.drawable.game_ic5,"Hidden Object"));
        levelInfos.add(new LevelInfo(6, R.drawable.game_ic6,"Hide Phone"));
        levelInfos.add(new LevelInfo(7, R.drawable.game_ic7,"Save Cat"));
        levelInfos.add(new LevelInfo(8, R.drawable.startgame,"level 8"));
        levelInfos.add(new LevelInfo(9, R.drawable.startgame,"level 9"));

        for (int level = 1; level <= LevelProgressStore.TOTAL_LEVEL_COUNT; level++) {
            final int selectedLevel = level;
//            boolean unlocked = level <= maxUnlockedLevel; temporary unlock all levels for testing
            boolean unlocked = true;
            LevelInfo levelInfo = levelInfos.get(level - 1);

            View itemView = getLayoutInflater().inflate(
                    R.layout.item_level_select_grid,
                    gridLayout,
                    false
            );

            ImageView imgIcon = itemView.findViewById(R.id.img_icon);
            TextView txtIcon = itemView.findViewById(R.id.txt_icon);
            TextView txtLocked = itemView.findViewById(R.id.txt_level_not_unlocked);

            imgIcon.setImageResource(levelInfo.getIconResId());
            imgIcon.setAlpha(unlocked ? 1f : 0.2f);
            txtIcon.setText(levelInfo.getLevelName());
            txtLocked.setVisibility(unlocked ? View.GONE : View.VISIBLE);

            itemView.setEnabled(unlocked);
            itemView.setAlpha(unlocked ? 1f : 0.55f);
            itemView.setOnClickListener(v -> {
                if (unlocked) {
                    openLevel(selectedLevel);
                }
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f)
            );
            params.width = 0; // 等分 4 欄
            params.setMargins(
                    0, UiUtils.dpToPx(this, 6), 0, UiUtils.dpToPx(this, 6)
            );

            itemView.setLayoutParams(params);
            gridLayout.addView(itemView);
        }

    }

    private Intent getLevelIntent(int levelNumber) {
        switch (levelNumber) {
            case 1:
                return new Intent(this, com.group.groupProject.game.colorGame.ColorGameDifficulty.class).putExtra("game", "brain");
            case 2:
                return new Intent(this, com.group.groupProject.game.colorGame.ColorGameDifficulty.class).putExtra("game", "memory");
            case 3:
                return new Intent(this, com.group.groupProject.game.appleActivity.AppleActivity_sing.class);
            case 4:
            case 5:
                return new Intent(this, com.group.groupProject.game.mingGame.GameActivity.class).putExtra("LEVEL_NUMBER", levelNumber);
            case 6:
                return new Intent(this, com.group.groupProject.game.hideThePhoneGame.HideThePhoneGame.class);
            case 7:
                return new Intent(this, com.group.groupProject.game.saveTheCat.SaveTheCatGame.class);
            default:
                return null;
        }
    }



    private void openLevel(int levelNumber) {
        Intent activity = getLevelIntent(levelNumber);
        if (activity == null) {
            Toast.makeText(this, "Level " + levelNumber + " is not available yet.", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(activity);
    }
}