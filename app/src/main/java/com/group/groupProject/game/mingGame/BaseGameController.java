package com.group.groupProject.game.mingGame;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.group.groupProject.R;
import com.group.groupProject.core.LevelProgressStore;
import com.group.groupProject.core.MainActivity;
import com.group.groupProject.score.model.SubmitScoreRequest;
import com.group.groupProject.score.model.SubmitScoreResponse;
import com.group.groupProject.score.remote.NetworkModule;
import com.group.groupProject.score.repository.RepositoryCallback;
import com.group.groupProject.score.repository.ScoreboardRepository;
import com.group.groupProject.score.repository.ScoreboardRepositoryImpl;

import org.json.JSONObject;

import java.util.Collections;
import java.util.Map;

public abstract class BaseGameController {

    private GameActivity hostActivity;
    private FrameLayout contentContainer;
    private TextView levelNumberView;
    private TextView levelDescriptionView;
    private TextView mistakeCountView;
    private int mistakeCount;
    private final ScoreboardRepository repository = NetworkModule.createRepository();

    public final void attach(GameActivity hostActivity,
                             FrameLayout contentContainer,
                             TextView levelNumberView,
                             TextView levelDescriptionView,
                             TextView mistakeCountView) {
        this.hostActivity = hostActivity;
        this.contentContainer = contentContainer;
        this.levelNumberView = levelNumberView;
        this.levelDescriptionView = levelDescriptionView;
        this.mistakeCountView = mistakeCountView;
        resetMistakes();

        renderLevelInfo();
        renderContent();
    }

    public final int getLevelNumber() {
        return getLevelIndex();
    }

    public final void showMenuDialog() {
        new MaterialAlertDialogBuilder(requireHost())
                .setTitle(requireHost().getString(R.string.menu_dialog_title))
                .setMessage(requireHost().getString(R.string.menu_dialog_message))
                .setCancelable(true)
                .setPositiveButton(R.string.menu_continue, (dialog, which) -> dialog.dismiss())
                .setNeutralButton(R.string.menu_restart, (dialog, which) -> requireHost().restartCurrentLevel())
                .setNegativeButton(R.string.menu_back_to_levels, (dialog, which) -> requireHost().finishToLevelSelect())
                .show();
    }

    public final void showHintDialog() {
        if (!canUseHintNow()) {
            return;
        }

        int currentMistakeCount = getMistakeCount();
        new MaterialAlertDialogBuilder(requireHost())
                .setTitle(R.string.hint_dialog_title)
                .setMessage(getHintTextForMistakeCount(currentMistakeCount))
                .setPositiveButton(R.string.hint_dialog_confirm, (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void showLevelScoreDialog(boolean isCleared) {
        int currentLevel = getLevelNumber();
        int nextLevel = currentLevel + 1;
        boolean isLastLevel = currentLevel >= LevelProgressStore.TOTAL_LEVEL_COUNT;
        final int[] RANK = new int[1];

//        if (isCleared && !isLastLevel) {
//            LevelProgressStore.unlockLevel(requireHost(), nextLevel);
//        }

        SubmitScoreRequest scoreRequest = new SubmitScoreRequest();
        scoreRequest.playerName = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "Anonymous";
        scoreRequest.score = getLevelScore();
        scoreRequest.level = getLevelNumber();
        scoreRequest.metadata = getMetadata(); // You can add any additional data as needed

        repository.submitScore("g99-7890", scoreRequest, new RepositoryCallback<SubmitScoreResponse>()
        {
            @Override
            public void onSuccess(SubmitScoreResponse data) {
                Toast.makeText(requireHost(), "Score submitted successfully! You Rank:" + data.rank, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message, Throwable throwable) {
                Toast.makeText(requireHost(), "Failed to submit score: " + message, Toast.LENGTH_SHORT).show();

            }

        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireHost());

        builder.setPositiveButton(R.string.level_score_back_to_levels,
                        (dialog, which) -> requireHost().finishToLevelSelect())
                .setNeutralButton(R.string.level_score_retry,
                        (dialog, which) -> requireHost().restartCurrentLevel());

        if (isCleared) {
            builder.setTitle(R.string.level_score_clear_title)
                    .setMessage((requireHost().getString(R.string.level_score_message, getMistakeCount(), getLevelScore())) + "\n\n" +
                            requireHost().getString(R.string.level_clear_message))
                    .setCancelable(false);
//            if (!isLastLevel) {
//                builder.setNegativeButton(R.string.level_score_next_level,
//                        (dialog, which) -> requireHost().openLevel(nextLevel));
//            }
        } else {
            builder.setTitle(R.string.level_score_failed_title)
                    .setMessage(requireHost().getString(R.string.level_score_message, getMistakeCount(), getLevelScore())
                            + "\n\n" + requireHost().getString(R.string.level_failed_message))
                    .setCancelable(false);
        }

        builder.show();
    }

    protected abstract int getLevelScore();

    protected String getMetadata() {
        return "{}";
    }

    protected void showLevelClearDialog() {
        showLevelScoreDialog(true);
    }

    protected void showLevelFailedDialog() {
        showLevelScoreDialog(false);
    }

    protected final void addMistake() {
        mistakeCount++;
        renderMistakeCount();
        notifyHintStateChanged();
    }

    protected final void resetMistakes() {
        mistakeCount = 0;
        renderMistakeCount();
        notifyHintStateChanged();
    }

    protected final int getMistakeCount() {
        return mistakeCount;
    }

    protected final GameActivity requireHost() {
        if (hostActivity == null) {
            throw new IllegalStateException("Controller is not attached to a host activity.");
        }
        return hostActivity;
    }

    protected final LayoutInflater getLayoutInflater() {
        return requireHost().getLayoutInflater();
    }

    protected abstract int getLevelIndex();

    protected abstract String getLevelTitle();

    protected abstract String getLevelDescription();

    protected String getHintText() {
        return "something went wrong, no hint available.";
    }

    protected Map<Integer, String> getHintThresholdMap() {
        return Collections.emptyMap();
    }

    protected boolean isHintEnabledForLevel() {
        return true;
    }

    protected int getHintMinimumMistakes() {
        return 0;
    }

    public final boolean canUseHintNow() {
        return isHintEnabledForLevel() && getMistakeCount() >= getHintMinimumMistakes();
    }

    protected String getHintTextForMistakeCount(int mistakeCount) {
        Map<Integer, String> thresholdMap = getHintThresholdMap();
        if (thresholdMap == null || thresholdMap.isEmpty()) {
            return getHintText();
        }

        int bestThreshold = Integer.MIN_VALUE;
        String selectedHint = null;
        for (Map.Entry<Integer, String> entry : thresholdMap.entrySet()) {
            int threshold = entry.getKey();
            if (mistakeCount >= threshold && threshold > bestThreshold) {
                bestThreshold = threshold;
                selectedHint = entry.getValue();
            }
        }

        return selectedHint != null ? selectedHint : getHintText();
    }

    protected abstract View createGameContent(LayoutInflater inflater, ViewGroup parent);

    private void renderLevelInfo() {
        if (levelNumberView != null) {
            levelNumberView.setText(requireHost().getString(R.string.level_info_title_format, getLevelIndex(), getLevelTitle()));
        }
        if (levelDescriptionView != null) {
            levelDescriptionView.setText(getLevelDescription());
        }
        renderMistakeCount();
    }

    private void renderMistakeCount() {
        if (mistakeCountView != null) {
            mistakeCountView.setText(requireHost().getString(R.string.mistake_count_format, mistakeCount));
        }
    }

    private void renderContent() {
        if (contentContainer == null) {
            return;
        }

        contentContainer.removeAllViews();
        View content = createGameContent(getLayoutInflater(), contentContainer);
        if (content != null) {
            contentContainer.addView(content);
        }
    }

    private void notifyHintStateChanged() {
        if (hostActivity != null) {
            hostActivity.refreshHintButtonState();
        }
    }

    protected boolean allowRotation() {
        return false;
    }

}

