package com.group.groupProject.game.mingGame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.group.groupProject.R;

public class LevelSelectActivity extends AppCompatActivity {

    private TextView progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_level_select);

        View root = findViewById(R.id.main);
        int initialLeft = root.getPaddingLeft();
        int initialTop = root.getPaddingTop();
        int initialRight = root.getPaddingRight();
        int initialBottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    initialLeft + systemBars.left,
                    initialTop + systemBars.top,
                    initialRight + systemBars.right,
                    initialBottom + systemBars.bottom
            );
            return insets;
        });

        LevelProgressStore.ensureInitialized(this);

        progressText = findViewById(R.id.txt_unlock_progress);

        findViewById(R.id.btn_back).setOnClickListener(view -> finish());
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

        for (int level = 1; level <= LevelProgressStore.TOTAL_LEVEL_COUNT; level++) {
            final int selectedLevel = level;
            Button levelButton = new Button(this);
            levelButton.setAllCaps(false);
            levelButton.setText(level <= maxUnlockedLevel
                    ? getString(R.string.level_button_label, level)
                    : getString(R.string.level_button_locked_label, level));
            levelButton.setEnabled(level <= maxUnlockedLevel);
            levelButton.setAlpha(level <= maxUnlockedLevel ? 1f : 0.45f);
            levelButton.setOnClickListener(view -> openLevel(selectedLevel));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f)
            );
            params.width = 0;
            params.height = UiUtils.dpToPx(this, 88);
            params.setMargins(
                    UiUtils.dpToPx(this, 6),
                    UiUtils.dpToPx(this, 6),
                    UiUtils.dpToPx(this, 6),
                    UiUtils.dpToPx(this, 6)
            );
            levelButton.setLayoutParams(params);
            gridLayout.addView(levelButton);
        }
    }

    private void openLevel(int levelNumber) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_LEVEL_NUMBER, levelNumber);
        startActivity(intent);
    }
}




