package com.group.groupProject.game.hideThePhoneGame;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.group.groupProject.R;

/**
 * HideThePhoneGame
 *
 * ── Find the Key ──────────────────────────────────────────────────────
 *  Flip phone 180° face-down → basket tips, key bounces out (stays out).
 *  Drag key to door → door unlocks → YOU WIN.
 *
 * ── Guard patrol ──────────────────────────────────────────────────────
 *  Guard walks across every ~5 s.
 *  While crossing: cover screen with hand (proximity) OR flip face-down.
 *  Not hidden when guard finishes → CAUGHT.
 */
public class HideThePhoneGame extends AppCompatActivity implements SensorEventListener {

    // ── Game state ────────────────────────────────────────────────────
    private boolean keyFallen    = false;
    private boolean keyPickedUp  = false;
    private boolean doorUnlocked = false;
    private boolean guardCrossing = false;
    private boolean isHidden      = false;
    private boolean gameEnded     = false;   // prevents double dialogs

    // ── Drag offsets ──────────────────────────────────────────────────
    private float keyDragDx, keyDragDy;

    // ── Guard timing ──────────────────────────────────────────────────
    private static final int WAIT_BEFORE_FIRST_MS = 4000;
    private static final int CROSSING_DURATION_MS  = 3500;
    private static final int WAIT_BETWEEN_MS       = 5000;

    private final Handler  uiHandler      = new Handler(Looper.getMainLooper());
    private final Runnable startCrossing  = this::beginCrossing;
    private final Runnable endCrossing    = this::finishCrossing;

    // ── Sensors ───────────────────────────────────────────────────────
    private SensorManager sensorManager;
    private Sensor        accelSensor, proximitySensor;

    // ── Views ─────────────────────────────────────────────────────────
    private TextView tvBasket, tvKey, tvDoor, tvHint, tvGuard, tvStatus;
    private Vibrator vibrator;

    // ══════════════════════════════════════════════════════════════════
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        setContentView(R.layout.activity_hide_the_phone);

        vibrator  = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        tvBasket = findViewById(R.id.tvBox);
        tvKey    = findViewById(R.id.tvKey);
        tvDoor   = findViewById(R.id.tvDoor);
        tvHint   = findViewById(R.id.tvP1Hint);
        tvGuard  = findViewById(R.id.tvGuard);
        tvStatus = findViewById(R.id.tvStatus);

        // Hide the old survive-counter view if it exists
        View escapes = findViewById(R.id.tvEscapes);
        if (escapes != null) escapes.setVisibility(View.GONE);

        initViews();
        setupKeyTouch();
        setupDoorTouch();

        sensorManager    = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelSensor      = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        proximitySensor  = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        scheduleNextCrossing(WAIT_BEFORE_FIRST_MS);
    }

    // ── View initialisation ───────────────────────────────────────────

    private void initViews() {
        tvBasket.setText("🧺");
        tvBasket.setRotation(0f);
        tvBasket.setAlpha(1f);

        tvKey.setVisibility(View.INVISIBLE);
        tvKey.setAlpha(1f);
        tvKey.setScaleX(1f); tvKey.setScaleY(1f);
        tvKey.setTranslationX(0f); tvKey.setTranslationY(0f);
        tvKey.setX(0f); tvKey.setY(0f);

        tvDoor.setText("🚪");

        tvGuard.setVisibility(View.INVISIBLE);
        tvGuard.setTranslationX(0f);

        tvHint.setText("Flip phone face-down 180° to tip the basket 🧺");
        tvStatus.setText("Guard is patrolling…");
    }

    // ══════════════════════════════════════════════════════════════════
    // Key drop
    // ══════════════════════════════════════════════════════════════════

    private void dropKey() {
        // Tip basket
        tvBasket.animate()
                .rotation(40f).setDuration(280)
                .withEndAction(() ->
                        tvBasket.animate().rotation(0f).setDuration(200).start()
                ).start();

        // Key bounces down
        tvKey.setVisibility(View.VISIBLE);
        tvKey.setTranslationY(-300f);
        tvKey.animate()
                .translationY(0f)
                .setDuration(900)
                .setInterpolator(new BounceInterpolator())
                .start();

        tvHint.setText("Key fell out! Drag it to the door 🗝 🚪");
        vibrate(120);
    }

    // ══════════════════════════════════════════════════════════════════
    // Touch handlers
    // ══════════════════════════════════════════════════════════════════

    @SuppressLint("ClickableViewAccessibility")
    private void setupKeyTouch() {
        tvKey.setOnTouchListener((v, ev) -> {
            if (!keyFallen) return false;
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    tvKey.animate().cancel();
                    if (!keyPickedUp) {
                        keyPickedUp = true;
                        tvKey.animate()
                                .scaleX(1.3f).scaleY(1.3f).setDuration(100)
                                .withEndAction(() ->
                                        tvKey.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                                ).start();
                        vibrate(50);
                    }
                    keyDragDx = ev.getRawX() - tvKey.getX();
                    keyDragDy = ev.getRawY() - tvKey.getY();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (!keyPickedUp) return false;
                    tvKey.setX(ev.getRawX() - keyDragDx);
                    tvKey.setY(ev.getRawY() - keyDragDy);
                    tvHint.setText("Drag the key to the door 🚪");
                    return true;

                case MotionEvent.ACTION_UP:
                    if (keyPickedUp && !doorUnlocked) checkKeyOnDoor();
                    return true;
            }
            return false;
        });
    }

    private void setupDoorTouch() {
        tvDoor.setOnClickListener(v -> {
            if (!keyFallen) {
                shakeView(tvDoor);
                tvHint.setText("You need the key first! Flip the phone face-down.");
            }
        });
    }

    private void checkKeyOnDoor() {
        int[] kXY = new int[2]; tvKey.getLocationOnScreen(kXY);
        int[] dXY = new int[2]; tvDoor.getLocationOnScreen(dXY);
        double dist = Math.hypot(kXY[0] - dXY[0], kXY[1] - dXY[1]);
        if (dist < 220) {
            doorUnlocked = true;
            tvDoor.setText("🔓");
            tvKey.animate().alpha(0f).setDuration(300)
                    .withEndAction(() -> tvKey.setVisibility(View.GONE)).start();
            tvHint.setText("Door unlocked! 🎉");
            vibrate(200);
            stopAllGuardCallbacks();
            tvDoor.postDelayed(this::showWinDialog, 600);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Guard patrol
    // ══════════════════════════════════════════════════════════════════

    private void scheduleNextCrossing(int delayMs) {
        if (gameEnded) return;
        uiHandler.postDelayed(startCrossing, delayMs);
    }

    /** Called when the guard starts walking across. */
    private void beginCrossing() {
        if (gameEnded || doorUnlocked) return;

        isHidden      = false;
        guardCrossing = true;

        tvStatus.setText("⚠️  GUARD! Cover screen or flip face-down!");
        vibrate(250);
        animateGuardCross();

        // Check result after guard finishes crossing
        uiHandler.postDelayed(endCrossing, CROSSING_DURATION_MS);
    }

    /** Called when the guard finishes crossing — check if player hid. */
    private void finishCrossing() {
        if (gameEnded || doorUnlocked) return;
        guardCrossing = false;

        if (!isHidden) {
            gameEnded = true;
            tvStatus.setText("😱  CAUGHT! The guard saw your screen!");
            vibrate(700);
            uiHandler.postDelayed(this::showCaughtDialog, 1000);
        } else {
            tvStatus.setText("Guard is patrolling…");
            scheduleNextCrossing(WAIT_BETWEEN_MS);
        }
    }

    private void animateGuardCross() {
        int screenW = getResources().getDisplayMetrics().widthPixels;
        tvGuard.setVisibility(View.VISIBLE);
        tvGuard.setTranslationX(-200f);
        tvGuard.animate()
                .translationX(screenW + 200f)
                .setDuration(CROSSING_DURATION_MS)
                .withEndAction(() -> tvGuard.setVisibility(View.INVISIBLE))
                .start();
    }

    private void stopAllGuardCallbacks() {
        uiHandler.removeCallbacks(startCrossing);
        uiHandler.removeCallbacks(endCrossing);
        guardCrossing = false;
        if (tvGuard != null) {
            tvGuard.animate().cancel();
            tvGuard.setVisibility(View.INVISIBLE);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Dialogs
    // ══════════════════════════════════════════════════════════════════

    private void showWinDialog() {
        if (isFinishing() || isDestroyed()) return;
        vibrate(300);
        new AlertDialog.Builder(this)
                .setTitle("You Win! 🎊")
                .setMessage("You found the key, unlocked the door,\nand hid from the guard!\nMission complete!")
                .setPositiveButton("Play Again", (d, w) -> resetGame())
                .setNegativeButton("Back",       (d, w) -> backToHomePage())
                .setCancelable(false)
                .show();
    }

    private void showCaughtDialog() {
        if (isFinishing() || isDestroyed()) return;
        new AlertDialog.Builder(this)
                .setTitle("Busted! 🚨")
                .setMessage("The guard saw your phone!\nCover the screen with your hand or flip it face-down next time.")
                .setPositiveButton("Try Again", (d, w) -> resetGame())
                .setNegativeButton("Quit",      (d, w) -> backToHomePage())
                .setCancelable(false)
                .show();
    }

    // ══════════════════════════════════════════════════════════════════
    // Reset
    // ══════════════════════════════════════════════════════════════════

    private void resetGame() {
        // Stop everything first
        stopAllGuardCallbacks();
        uiHandler.removeCallbacks(this::showWinDialog);
        uiHandler.removeCallbacks(this::showCaughtDialog);

        // Reset state flags
        keyFallen     = false;
        keyPickedUp   = false;
        doorUnlocked  = false;
        guardCrossing = false;
        isHidden      = false;
        gameEnded     = false;

        // Reset views
        initViews();

        // Restart patrol
        scheduleNextCrossing(WAIT_BEFORE_FIRST_MS);
    }

    // ══════════════════════════════════════════════════════════════════
    // Sensors
    // ══════════════════════════════════════════════════════════════════

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (gameEnded) return;

        int type = event.sensor.getType();

        if (type == Sensor.TYPE_ACCELEROMETER) {
            float z = event.values[2];
            boolean isFaceDown = z < -8f;

            // Drop key only once
            if (isFaceDown && !keyFallen) {
                keyFallen = true;
                runOnUiThread(this::dropKey);
            }

            // Hide from guard only once per crossing
            if (guardCrossing && isFaceDown && !isHidden) {
                isHidden = true;
                runOnUiThread(() -> tvStatus.setText("✋ Hiding… 🤫"));
            }
        }

        else if (type == Sensor.TYPE_PROXIMITY) {
            boolean isCovered = event.values[0] < event.sensor.getMaximumRange();

            if (guardCrossing && isCovered && !isHidden) {
                isHidden = true;
                runOnUiThread(() -> tvStatus.setText("✋ Hiding… 🤫"));
            }
        }
    }

    @Override public void onAccuracyChanged(Sensor s, int a) {}

    // ══════════════════════════════════════════════════════════════════
    // Lifecycle
    // ══════════════════════════════════════════════════════════════════

    @Override protected void onResume() {
        super.onResume();
        if (accelSensor != null)
            sensorManager.registerListener(this, accelSensor,     SensorManager.SENSOR_DELAY_GAME);
        if (proximitySensor != null)
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        stopAllGuardCallbacks();
    }

    // ══════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════

    private void shakeView(View v) {
        v.animate().translationX(-18f).setDuration(55)
                .withEndAction(() -> v.animate().translationX(18f).setDuration(55)
                        .withEndAction(() -> v.animate().translationX(-10f).setDuration(45)
                                .withEndAction(() -> v.animate().translationX(0f).setDuration(45)
                                        .start()).start()).start()).start();
    }

    private void vibrate(int ms) {
        if (vibrator == null) return;
        if (!vibrator.hasVibrator()) return;

        vibrator.vibrate(
                VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE)
        );
    }
        private void backToHomePage(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
