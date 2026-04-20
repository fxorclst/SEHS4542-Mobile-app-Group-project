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
import com.group.groupProject.score.LeaderboardActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private TextView tv_username;

    private TextView progressText;
    private LinearLayout ll_game1, ll_game2, ll_game3, ll_game4, ll_game5, ll_game6, ll_game7, ll_game8, ll_game9, ll_game10;

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
            boolean unlocked = level <= maxUnlockedLevel;
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

    private void openLevel(int levelNumber) {
        Toast.makeText(this, "Opening Level " + levelNumber, Toast.LENGTH_SHORT).show();
    }

}