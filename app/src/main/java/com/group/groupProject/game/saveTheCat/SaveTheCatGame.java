package com.group.groupProject.game.saveTheCat;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.group.groupProject.R;
import com.group.groupProject.core.MainActivity;

/**
 * SaveTheCatGame
 *
 * WIN  — drag 💣 into the 🗑️ trash can → detonates after 1 s (safe, outside).
 * LOSE — drag 💣 to the 🍳 stove → instant detonation.
 * LOSE — timer hits 0 → on-screen detonation.
 *
 * Timer uses a Handler loop so intervalMs changes (scissors) take effect
 * on the very next tick.
 *
 * Furniture (sofa, table, stove, trash) are all draggable.
 *
 * Tools:
 *   ✂  Scissors — 2× speed (intervalMs = 500, restarts immediately)
 *   🔨 Hammer  — squash anim, timer continues
 *   💧 Water   — ripple anim, timer continues
 *   🔥 Fire    — instant detonation
 */
public class SaveTheCatGame extends AppCompatActivity {

    private static final int INITIAL_MS   = 30_000;
    private static final int HIT_DIST     = 180;   // px — overlap threshold for trash/stove

    // ── Timer state ────────────────────────────────────────────────────
    private int     timeLeftMs   = INITIAL_MS;
    private int     intervalMs   = 1000;
    private boolean timerRunning = false;
    private boolean gameOver     = false;
    private boolean bombDisposed = false;   // bomb put in trash

    // Handler-based timer — intervalMs is re-read every tick
    private final Handler timerHandler = new Handler();
    private Runnable      timerRunnable;

    // ── Drag offsets ───────────────────────────────────────────────────
    private float catDragDx,   catDragDy;
    private float bombDragDx,  bombDragDy;
    private float sofaDragDx,  sofaDragDy;
    private float tableDragDx, tableDragDy;
    private float stoveDragDx, stoveDragDy;
    private float trashDragDx, trashDragDy;

    // ── Original furniture positions (captured after first layout) ─────
    private float origSofaX,  origSofaY;
    private float origTableX, origTableY;
    private float origStoveX, origStoveY;
    private float origTrashX, origTrashY;

    // ── Tool flags ─────────────────────────────────────────────────────
    private boolean scissorsUsed = false;
    private boolean hammerUsed   = false;
    private boolean waterUsed    = false;

    // ── Views ──────────────────────────────────────────────────────────
    private FrameLayout gameArea;
    private TextView    tvCat, tvBomb, tvTimer;
    private TextView    tvSofa, tvTable, tvStove, tvTrash;
    private View        flashView;
    private TextView    toolScissors, toolHammer, toolWater, toolFire;

    private Vibrator vibrator;

    // ══════════════════════════════════════════════════════════════════
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_the_cat);

        vibrator     = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        gameArea     = findViewById(R.id.gameArea);
        tvCat        = findViewById(R.id.tvCat);
        tvBomb       = findViewById(R.id.tvBomb);
        tvTimer      = findViewById(R.id.tvTimer);
        flashView    = findViewById(R.id.flashView);
        tvSofa       = findViewById(R.id.tvSofa);
        tvTable      = findViewById(R.id.tvTable);
        tvStove      = findViewById(R.id.tvStove);
        tvTrash      = findViewById(R.id.tvTrash);
        toolScissors = findViewById(R.id.toolScissors);
        toolHammer   = findViewById(R.id.toolHammer);
        toolWater    = findViewById(R.id.toolWater);
        toolFire     = findViewById(R.id.toolFire);

        updateTimerDisplay();
        setupCatDrag();
        setupBombDrag();
        setupFurnitureDrag();
        setupTools();

        // Capture original furniture positions after layout is complete
        gameArea.post(() -> {
            origSofaX  = tvSofa.getX();  origSofaY  = tvSofa.getY();
            origTableX = tvTable.getX(); origTableY = tvTable.getY();
            origStoveX = tvStove.getX(); origStoveY = tvStove.getY();
            origTrashX = tvTrash.getX(); origTrashY = tvTrash.getY();
        });

        tvBomb.postDelayed(this::startTimer, 600);
    }

    // ══════════════════════════════════════════════════════════════════
    // Handler-based timer — intervalMs re-read every tick
    // ══════════════════════════════════════════════════════════════════

    private void startTimer() {
        if (timerRunning || gameOver || bombDisposed) return;
        timerRunning = true;

        timerRunnable = new Runnable() {
            @Override public void run() {
                if (gameOver || bombDisposed) return;

                timeLeftMs -= intervalMs;

                if (timeLeftMs <= 0) {
                    timeLeftMs = 0;
                    updateTimerDisplay();
                    detonate(false, "Time's up!");
                    return;
                }

                updateTimerDisplay();
                beep();

                if      (timeLeftMs <= 5000)  tvTimer.setTextColor(0xFFFF2222);
                else if (timeLeftMs <= 10000) tvTimer.setTextColor(0xFFFF8800);
                else                          tvTimer.setTextColor(0xFFFFFFFF);

                // Re-schedule using CURRENT intervalMs (scissors takes effect here)
                timerHandler.postDelayed(this, intervalMs);
            }
        };

        timerHandler.postDelayed(timerRunnable, intervalMs);
    }

    /** Kills the current tick chain and immediately restarts with new intervalMs. */
    private void restartTimer() {
        timerHandler.removeCallbacks(timerRunnable);
        timerRunning = false;
        startTimer();
    }

    private void updateTimerDisplay() {
        int secs = (int) Math.ceil(timeLeftMs / 1000.0);
        tvTimer.setText(secs + "s");
    }

    private void beep() {
        vibrate(35);
        tvBomb.animate().scaleX(1.18f).scaleY(1.18f).setDuration(80)
                .withEndAction(() ->
                        tvBomb.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
                ).start();
    }

    // ══════════════════════════════════════════════════════════════════
    // Cat drag
    // ══════════════════════════════════════════════════════════════════

    @SuppressLint("ClickableViewAccessibility")
    private void setupCatDrag() {
        tvCat.setOnTouchListener((v, ev) -> {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    tvCat.animate().cancel();
                    catDragDx = ev.getRawX() - tvCat.getX();
                    catDragDy = ev.getRawY() - tvCat.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    tvCat.setX(ev.getRawX() - catDragDx);
                    tvCat.setY(ev.getRawY() - catDragDy);
                    break;
            }
            return true;
        });
    }

    // ══════════════════════════════════════════════════════════════════
    // Furniture drag (sofa, table, stove, trash — all freely draggable)
    // ══════════════════════════════════════════════════════════════════

    @SuppressLint("ClickableViewAccessibility")
    private void setupFurnitureDrag() {
        makeDraggable(tvSofa,  () -> sofaDragDx,  (v) -> sofaDragDx = v,
                () -> sofaDragDy,  (v) -> sofaDragDy = v);
        makeDraggable(tvTable, () -> tableDragDx, (v) -> tableDragDx = v,
                () -> tableDragDy, (v) -> tableDragDy = v);
        makeDraggable(tvStove, () -> stoveDragDx, (v) -> stoveDragDx = v,
                () -> stoveDragDy, (v) -> stoveDragDy = v);
        makeDraggable(tvTrash, () -> trashDragDx, (v) -> trashDragDx = v,
                () -> trashDragDy, (v) -> trashDragDy = v);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void makeDraggable(TextView view,
                               FloatGetter getDx, FloatSetter setDx,
                               FloatGetter getDy, FloatSetter setDy) {
        view.setOnTouchListener((v, ev) -> {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.animate().cancel();
                    setDx.set(ev.getRawX() - view.getX());
                    setDy.set(ev.getRawY() - view.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    view.setX(ev.getRawX() - getDx.get());
                    view.setY(ev.getRawY() - getDy.get());
                    break;
            }
            return true;
        });
    }

    interface FloatGetter { float get(); }
    interface FloatSetter { void set(float v); }

    // ══════════════════════════════════════════════════════════════════
    // Bomb drag — checks trash & stove overlap on every move + up
    // ══════════════════════════════════════════════════════════════════

    @SuppressLint("ClickableViewAccessibility")
    private void setupBombDrag() {
        tvBomb.setOnTouchListener((v, ev) -> {
            if (gameOver || bombDisposed) return false;

            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    tvBomb.animate().cancel();
                    bombDragDx = ev.getRawX() - tvBomb.getX();
                    bombDragDy = ev.getRawY() - tvBomb.getY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    tvBomb.setX(ev.getRawX() - bombDragDx);
                    tvBomb.setY(ev.getRawY() - bombDragDy);
                    checkBombInteraction();
                    break;

                case MotionEvent.ACTION_UP:
                    checkBombInteraction();
                    break;
            }
            return true;
        });
    }

    /** Returns centre-to-centre distance between two views (screen coords). */
    private float viewDist(View a, View b) {
        int[] pa = new int[2]; a.getLocationOnScreen(pa);
        int[] pb = new int[2]; b.getLocationOnScreen(pb);
        float ax = pa[0] + a.getWidth()  / 2f;
        float ay = pa[1] + a.getHeight() / 2f;
        float bx = pb[0] + b.getWidth()  / 2f;
        float by = pb[1] + b.getHeight() / 2f;
        return (float) Math.hypot(ax - bx, ay - by);
    }

    private void checkBombInteraction() {
        if (gameOver || bombDisposed) return;

        // ── WIN: bomb dropped into trash can ──────────────────────────
        if (viewDist(tvBomb, tvTrash) < HIT_DIST) {
            bombDisposed = true;
            timerHandler.removeCallbacks(timerRunnable);
            vibrate(150);

            // Snap bomb into trash visually
            tvBomb.animate().cancel();
            int[] tp = new int[2]; tvTrash.getLocationOnScreen(tp);
            tvBomb.setX(tp[0]);
            tvBomb.setY(tp[1]);
            tvBomb.setText("🗑️");

            Toast.makeText(this, "Bomb in the trash! 💨 Detonating…", Toast.LENGTH_SHORT).show();
            tvBomb.postDelayed(this::detonateInTrash, 1000);
            return;
        }

        // ── LOSE: bomb dragged onto stove ──────────────────────────────
        if (viewDist(tvBomb, tvStove) < HIT_DIST) {
            detonate(false, "The stove set it off!");
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Tools
    // ══════════════════════════════════════════════════════════════════

    private void setupTools() {
        toolScissors.setOnClickListener(v -> useScissors());
        toolHammer  .setOnClickListener(v -> useHammer());
        toolWater   .setOnClickListener(v -> useWater());
        toolFire    .setOnClickListener(v -> useFire());
    }

    private void useScissors() {
        if (gameOver || bombDisposed) return;
        if (scissorsUsed) {
            Toast.makeText(this, "Wires already cut!", Toast.LENGTH_SHORT).show();
            return;
        }
        scissorsUsed = true;
        disableTool(toolScissors);
        flyToTarget("✂️", tvBomb, () -> {
            // Set new interval THEN kill old tick chain and start fresh
            intervalMs = 500;
            restartTimer();
            tvBomb.setTextColor(0xFFFF4444);
            tvBomb.postDelayed(() -> tvBomb.setTextColor(0xFFFFFFFF), 600);
            Toast.makeText(this, "✂️  Wire cut — 2× speed!", Toast.LENGTH_SHORT).show();
        });
    }

    private void useHammer() {
        if (gameOver || bombDisposed) return;
        if (hammerUsed) {
            Toast.makeText(this, "Already smashed it!", Toast.LENGTH_SHORT).show();
            return;
        }
        hammerUsed = true;
        disableTool(toolHammer);
        flyToTarget("🔨", tvBomb, () -> {
            vibrate(140);
            tvBomb.animate().scaleX(0.65f).scaleY(1.35f).setDuration(120)
                    .withEndAction(() ->
                            tvBomb.animate().scaleX(1.1f).scaleY(0.9f).setDuration(90)
                                    .withEndAction(() ->
                                            tvBomb.animate().scaleX(1f).scaleY(1f).setDuration(90).start()
                                    ).start()
                    ).start();
            Toast.makeText(this, "🔨  Smashed!  Still ticking…", Toast.LENGTH_SHORT).show();
        });
    }

    private void useWater() {
        if (gameOver || bombDisposed) return;
        if (waterUsed) {
            Toast.makeText(this, "Already wet!", Toast.LENGTH_SHORT).show();
            return;
        }
        waterUsed = true;
        disableTool(toolWater);
        flyToTarget("💧", tvBomb, () -> {
            tvBomb.setBackgroundColor(0x660099FF);
            tvBomb.postDelayed(() -> tvBomb.setBackgroundColor(0x00000000), 700);
            ObjectAnimator.ofFloat(tvBomb, "translationX",
                            0f, -12f, 12f, -9f, 9f, -5f, 5f, 0f)
                    .setDuration(450).start();
            Toast.makeText(this, "💧  Splash!  Still ticking…", Toast.LENGTH_SHORT).show();
        });
    }

    private void useFire() {
        if (gameOver || bombDisposed) return;
        disableTool(toolFire);
        Toast.makeText(this, "🔥  What did you do?!", Toast.LENGTH_SHORT).show();
        tvBomb.postDelayed(() -> detonate(false, "You lit it yourself!"), 300);
    }

    private void disableTool(TextView tool) {
        tool.setEnabled(false);
        tool.setAlpha(0.3f);
    }

    // ══════════════════════════════════════════════════════════════════
    // Detonation
    // ══════════════════════════════════════════════════════════════════

    /** Lose-path detonation (timer ran out, stove, or fire tool). */
    private void detonate(boolean silent, String reason) {
        if (gameOver) return;
        gameOver = true;
        timerHandler.removeCallbacks(timerRunnable);

        tvBomb.setText("💥");
        tvTimer.setText("💀");
        tvTimer.setTextColor(0xFFFF2222);
        vibrate(900);
        flashScreen();
        shakeScreen();

        if (!silent) {
            tvBomb.postDelayed(() ->
                    new AlertDialog.Builder(this)
                            .setTitle("💥  BOOM!")
                            .setMessage(reason + "\nPut the bomb in the 🗑️ trash before the timer runs out!")
                            .setPositiveButton("Try Again", (d, w) -> restartGame())
                            .setNegativeButton("Quit",      (d, w) -> backToHomePage())
                            .setCancelable(false)
                            .show(), 1000);
        }
    }

    /** Win-path detonation — bomb safely disposed in trash. */
    private void detonateInTrash() {
        if (gameOver) return;
        gameOver = true;
        flashScreen();
        shakeScreen();
        vibrate(300);

        tvBomb.postDelayed(() ->
                new AlertDialog.Builder(this)
                        .setTitle("🎉  Cat Saved!")
                        .setMessage("The bomb exploded safely in the trash bin!\nThe cat is safe! 🐱")
                        .setPositiveButton("Play Again", (d, w) -> restartGame())
                        .setNegativeButton("Back to home page",       (d, w) -> backToHomePage())
                        .setCancelable(false)
                        .show(), 600);
    }

    private void backToHomePage(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void flashScreen() {
        flashView.setVisibility(View.VISIBLE);
        flashView.setAlpha(1f);
        flashView.animate().alpha(0f).setDuration(650)
                .withEndAction(() -> flashView.setVisibility(View.GONE)).start();
    }

    private void shakeScreen() {
        View root = findViewById(android.R.id.content);
        ObjectAnimator.ofFloat(root, "translationX",
                        0f, -28f, 28f, -22f, 22f, -14f, 14f, -7f, 7f, 0f)
                .setDuration(700).start();
    }

    // ══════════════════════════════════════════════════════════════════
    // Restart
    // ══════════════════════════════════════════════════════════════════

    private void restartGame() {
        gameOver     = false;
        bombDisposed = false;
        scissorsUsed = false;
        hammerUsed   = false;
        waterUsed    = false;
        intervalMs   = 1000;
        timeLeftMs   = INITIAL_MS;
        timerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);

        // Reset bomb
        tvBomb.animate().cancel();
        tvBomb.setText("💣");
        tvBomb.setTextColor(0xFFFFFFFF);
        tvBomb.setAlpha(1f);
        tvBomb.setScaleX(1f); tvBomb.setScaleY(1f);
        tvBomb.setBackgroundColor(0x00000000);
        tvBomb.setTranslationX(0f);
        tvBomb.setTranslationY(0f);
        tvBomb.post(() -> {
            tvBomb.setX(gameArea.getWidth() * 0.55f);
            tvBomb.setY(gameArea.getHeight() * 0.42f);
        });

        // Reset cat
        tvCat.animate().cancel();
        tvCat.setTranslationX(0f); tvCat.setTranslationY(0f);
        tvCat.post(() -> {
            tvCat.setX(gameArea.getWidth() * 0.15f);
            tvCat.setY(gameArea.getHeight() * 0.40f);
        });

        // Reset furniture to their original positions captured at startup
        tvSofa.animate().cancel();  tvSofa.setX(origSofaX);   tvSofa.setY(origSofaY);
        tvTable.animate().cancel(); tvTable.setX(origTableX);  tvTable.setY(origTableY);
        tvStove.animate().cancel(); tvStove.setX(origStoveX);  tvStove.setY(origStoveY);
        tvTrash.animate().cancel(); tvTrash.setX(origTrashX);  tvTrash.setY(origTrashY);

        tvTimer.setTextColor(0xFFFFFFFF);
        updateTimerDisplay();

        for (TextView t : new TextView[]{toolScissors, toolHammer, toolWater, toolFire}) {
            t.setEnabled(true);
            t.setAlpha(1f);
        }

        tvBomb.postDelayed(this::startTimer, 600);
    }

    // ══════════════════════════════════════════════════════════════════
    // Tool fly animation
    // ══════════════════════════════════════════════════════════════════

    private void flyToTarget(String emoji, View target, Runnable onArrival) {
        TextView fly = new TextView(this);
        fly.setText(emoji);
        fly.setTextSize(34f);
        ViewGroup root = (ViewGroup) getWindow().getDecorView();
        root.addView(fly);

        int[] pos = new int[2];
        target.getLocationOnScreen(pos);
        fly.setX(getResources().getDisplayMetrics().widthPixels - 80f);
        fly.setY(pos[1]);

        fly.animate()
                .x(pos[0]).y(pos[1])
                .setDuration(380)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> {
                    root.removeView(fly);
                    if (onArrival != null) onArrival.run();
                }).start();
    }

    // ══════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════

    private void vibrate(int ms) {
        if (vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
        else vibrator.vibrate(ms);
    }

    @Override protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
        timerRunning = false;
    }

    @Override protected void onResume() {
        super.onResume();
        if (!gameOver && !bombDisposed && !timerRunning && timeLeftMs > 0) startTimer();
    }
}
