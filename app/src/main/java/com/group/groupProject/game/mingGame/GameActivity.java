package com.group.groupProject.game.mingGame;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.group.groupProject.R;
import com.group.groupProject.core.LevelProgressStore;

public class GameActivity extends AppCompatActivity {

    public static final String EXTRA_LEVEL_NUMBER = "extra_level_number";

    private BaseGameController controller;
    private int levelNumber;
    private View hintButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

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
        levelNumber = getIntent().getIntExtra("LEVEL_NUMBER", 4);


        controller = LevelControllerFactory.create(levelNumber);

        hintButton = findViewById(R.id.btn_hint);

        FrameLayout contentContainer = findViewById(R.id.game_content_container);
        TextView levelNumberView = findViewById(R.id.txt_level_number);
        TextView levelDescriptionView = findViewById(R.id.txt_level_description);
        TextView mistakeCountView = findViewById(R.id.txt_mistake_count);

        controller.attach(this, contentContainer, levelNumberView, levelDescriptionView, mistakeCountView);

        if (controller.allowRotation()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        refreshHintButtonState();

        findViewById(R.id.btn_menu).setOnClickListener(view -> controller.showMenuDialog());
        hintButton.setOnClickListener(view -> controller.showHintDialog());
    }

    public void restartCurrentLevel() {
        recreate();
    }

    public void finishToLevelSelect() {
        finish();
    }

    public void openLevel(int targetLevel) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(EXTRA_LEVEL_NUMBER, targetLevel);
        startActivity(intent);
        finish();
    }

    public void refreshHintButtonState() {
        if (hintButton != null && controller != null) {
            hintButton.setEnabled(controller.canUseHintNow());
            hintButton.setAlpha(controller.canUseHintNow() ? 1f : 0.45f);
        }
    }
}

