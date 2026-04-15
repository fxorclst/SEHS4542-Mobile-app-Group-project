package com.group.groupProject.game.minggame.levels;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.group.groupProject.R;

import java.util.Locale;

public final class Level2RooftopPuzzleView extends View {

    private static final long LEVEL_DURATION_MS = 3 * 60 * 1000L;
    private static final long TIMER_TICK_MS = 250L;
    private static final int TOTAL_TARGET_COUNT = 4;
    private static final long MISTAKE_FLASH_DURATION_MS = 180L;

    private enum TargetArea {
        NONE,
        BOARD,
        DOOR,
        DOOR_FOUND,
        ROPE,
        ROPE_FOUND,
        WINDOW_A,
        WINDOW_A_OPENED,
        WINDOW_B,
        WINDOW_B_OPENED,
        COSPLAY,
        COSPLAY_FOUND,
        PARACHUTE,
        PARACHUTE_FOUND
    }

    private final Level2Controller controller;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timerTickRunnable = new Runnable() {
        @Override
        public void run() {
            if (levelResolved) {
                return;
            }

            if (getRemainingTimeMs() <= 0L) {
                finishAsTimeout();
                return;
            }

            invalidate();
            handler.postDelayed(this, TIMER_TICK_MS);
        }
    };

    private final Paint skyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint buildingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint floorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint roofPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint roofLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint windowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hudPanelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hudTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint flashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF buildingRect = new RectF();
    private final RectF roofRect = new RectF();
    private final RectF upperFloorRect = new RectF();
    private final RectF lowerFloorRect = new RectF();
    private final RectF boardRect = new RectF();
    private final RectF doorRect = new RectF();
    private final RectF ropeRect = new RectF();
    private final RectF windowLeftRect = new RectF();
    private final RectF windowRightRect = new RectF();
    private final RectF cosplayRect = new RectF();
    private final RectF parachuteRect = new RectF();

    private final float density;
    private final float touchSlopPx;

    private long timerEndRealtimeMs;
    private boolean timerRunning;
    private boolean levelResolved;
    private boolean boardDraggedAway;
    private boolean doorFound;
    private boolean ropeFound;
    private boolean cosWindowOpened;
    private boolean parachuteWindowOpened;
    private boolean cosplayFound;
    private boolean parachuteFound;
    private int foundCount;

    private float boardWidth;
    private float boardHeight;
    private float boardTargetCenterX;
    private float boardTargetCenterY;
    private float boardDragOffsetX;
    private float boardDragOffsetY;
    private boolean draggingBoard;
    private TargetArea activeTarget = TargetArea.NONE;
    private float downX;
    private float downY;
    private long mistakeFlashUntilRealtimeMs;

    public Level2RooftopPuzzleView(@NonNull Context context, @NonNull Level2Controller controller) {
        super(context);
        this.controller = controller;
        density = getResources().getDisplayMetrics().density;
        touchSlopPx = ViewConfiguration.get(context).getScaledTouchSlop();
        initPaints();
        setWillNotDraw(false);
        setFocusable(true);
        setClickable(true);
    }

    public Level2RooftopPuzzleView(@NonNull Context context) {
        this(context, new Level2Controller());
    }

    public Level2RooftopPuzzleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context);
    }

    public Level2RooftopPuzzleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context);
    }

    private void initPaints() {
        skyPaint.setColor(Color.parseColor("#BEE9FF"));
        buildingPaint.setColor(Color.parseColor("#8A8F99"));
        floorPaint.setColor(Color.parseColor("#6D7480"));
        roofPaint.setColor(Color.parseColor("#5B616C"));
        roofLinePaint.setColor(Color.parseColor("#D7DCE2"));
        roofLinePaint.setStrokeWidth(dp(2f));
        roofLinePaint.setStyle(Paint.Style.STROKE);
        roofLinePaint.setPathEffect(null);

        windowPaint.setColor(Color.parseColor("#31455B"));
        labelPaint.setColor(Color.parseColor("#D6B07A"));
        labelStrokePaint.setStyle(Paint.Style.STROKE);
        labelStrokePaint.setStrokeWidth(dp(2f));
        labelStrokePaint.setColor(Color.parseColor("#3C2A18"));
        labelTextPaint.setColor(Color.WHITE);
        labelTextPaint.setTextAlign(Paint.Align.CENTER);
        labelTextPaint.setTextSize(sp(15f));
        labelTextPaint.setFakeBoldText(true);

        hudPanelPaint.setColor(Color.parseColor("#1E2430"));
        hudPanelPaint.setAlpha(190);
        hudTextPaint.setColor(Color.WHITE);
        hudTextPaint.setTextSize(sp(16f));
        hudTextPaint.setFakeBoldText(true);
        hudTextPaint.setTextAlign(Paint.Align.LEFT);

        flashPaint.setColor(Color.parseColor("#FF4A4A"));
        flashPaint.setAlpha(38);
        textShadowPaint.setColor(Color.BLACK);
        textShadowPaint.setTextAlign(Paint.Align.CENTER);
        textShadowPaint.setTextSize(sp(15f));
        textShadowPaint.setFakeBoldText(true);
        textShadowPaint.setShadowLayer(dp(2f), 0f, dp(1f), Color.BLACK);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startTimer();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopTimer();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        layoutScene(w, h);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);
        drawScene(canvas);
        drawHud(canvas);

        if (SystemClock.elapsedRealtime() < mistakeFlashUntilRealtimeMs) {
            canvas.drawRect(0f, 0f, getWidth(), getHeight(), flashPaint);
            postInvalidateOnAnimation();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (levelResolved) {
            return true;
        }

        float x = event.getX();
        float y = event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                activeTarget = hitTest(x, y);
                downX = x;
                downY = y;
                draggingBoard = false;
                if (activeTarget == TargetArea.BOARD && !boardDraggedAway) {
                    boardDragOffsetX = x - boardRect.left;
                    boardDragOffsetY = y - boardRect.top;
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (activeTarget == TargetArea.BOARD && !boardDraggedAway) {
                    float moveDistance = distance(downX, downY, x, y);
                    if (!draggingBoard && moveDistance >= touchSlopPx) {
                        draggingBoard = true;
                    }
                    if (draggingBoard) {
                        moveBoardTo(x - boardDragOffsetX, y - boardDragOffsetY);
                        invalidate();
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (draggingBoard) {
                    finishBoardDrag();
                } else {
                    performClick();
                    handleTap(activeTarget);
                }
                activeTarget = TargetArea.NONE;
                draggingBoard = false;
                return true;

            case MotionEvent.ACTION_CANCEL:
                activeTarget = TargetArea.NONE;
                draggingBoard = false;
                return true;

            default:
                return super.onTouchEvent(event);
        }
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawRect(0f, 0f, getWidth(), getHeight(), skyPaint);
    }

    private void drawScene(Canvas canvas) {
        canvas.drawRoundRect(buildingRect, dp(14f), dp(14f), buildingPaint);
        canvas.drawRoundRect(roofRect, dp(14f), dp(14f), roofPaint);
        canvas.drawLine(buildingRect.left, roofRect.bottom, buildingRect.right, roofRect.bottom, roofLinePaint);

        drawFloorWindows(canvas);

        // 出入口始終可見，只是起初被木板覆蓋
        drawBox(canvas, doorRect,
                doorFound ? getContext().getString(R.string.level2_item_door_found) : getContext().getString(R.string.level2_item_door),
                doorFound ? foundLabelPaint() : activeLabelPaint(),
                doorFound,
                false);

        drawBox(canvas, ropeRect,
                ropeFound ? getContext().getString(R.string.level2_item_rope_found) : getContext().getString(R.string.level2_item_rope),
                ropeFound ? foundLabelPaint() : activeLabelPaint(),
                ropeFound,
                false);

        if (!boardDraggedAway) {
            drawBox(canvas, boardRect,
                    getContext().getString(R.string.level2_item_board),
                    activeLabelPaint(),
                    false,
                    false);
        } else {
            drawBox(canvas, boardRect,
                    getContext().getString(R.string.level2_item_leap_point),
                    leapLabelPaint(),
                    false,
                    false);
        }

        drawBox(canvas, windowLeftRect,
                "",
                cosWindowOpened ? openedWindowLabelPaint() : closedWindow(),
                false,
                true);

        drawBox(canvas, windowRightRect,
                "",
                parachuteWindowOpened ? openedWindowLabelPaint() : closedWindow(),
                false,
                true);

        if (cosWindowOpened) {
            drawBox(canvas, cosplayRect,
                    cosplayFound ? getContext().getString(R.string.level2_item_cosplay_found) : getContext().getString(R.string.level2_item_cosplay),
                    cosplayFound ? foundLabelPaint() : activeLabelPaint(),
                    cosplayFound,
                    false);
        }

        if (parachuteWindowOpened) {
            drawBox(canvas, parachuteRect,
                    parachuteFound ? getContext().getString(R.string.level2_item_parachute_found) : getContext().getString(R.string.level2_item_parachute),
                    parachuteFound ? foundLabelPaint() : activeLabelPaint(),
                    parachuteFound,
                    false);
        }
    }

    private void drawFloorWindows(Canvas canvas) {
        float gapX = dp(28f);
        float windowWidth = dp(120f);
        float windowHeight = dp(72f);
        float windowTop = upperFloorRect.top + dp(24f);

        float totalWidth = windowWidth * 2 + gapX;
        float startX = upperFloorRect.centerX() - totalWidth / 2f;

        for (int i = 0; i < 2; i++) {
            float left = startX + i * (windowWidth + gapX);
            RectF temp = new RectF(left, windowTop, left + windowWidth, windowTop + windowHeight);
            canvas.drawRoundRect(temp, dp(8f), dp(8f), windowPaint);
            canvas.drawRoundRect(temp, dp(8f), dp(8f), labelStrokePaint);
        }
    }

    private void drawHud(Canvas canvas) {
        float panelLeft = dp(16f);
        float panelTop = dp(14f);
        float panelRight = getWidth() - dp(16f);
        float panelBottom = dp(80f);
        RectF panel = new RectF(panelLeft, panelTop, panelRight, panelBottom);
        canvas.drawRoundRect(panel, dp(16f), dp(16f), hudPanelPaint);

        long remainingMs = getRemainingTimeMs();
        String timerText = getContext().getString(
                R.string.level2_timer_format,
                formatMinutes((int) (remainingMs / 1000L / 60L)),
                formatSeconds((int) ((remainingMs / 1000L) % 60L))
        );
        String foundText = getContext().getString(R.string.level2_found_format, foundCount, TOTAL_TARGET_COUNT);
        drawTextWithBaseline(canvas, timerText, panel.left + dp(16f), panel.top + dp(28f), hudTextPaint);
        drawTextWithBaseline(canvas, foundText, panel.left + dp(16f), panel.top + dp(53f), hudTextPaint);

    }

    private void drawBox(Canvas canvas, RectF rect, String text, Paint fillPaint, boolean isFound, boolean isWindow) {
        Paint borderPaint = isFound ? foundBorderPaint() : labelStrokePaint;
        canvas.drawRoundRect(rect, dp(12f), dp(12f), fillPaint);
        canvas.drawRoundRect(rect, dp(12f), dp(12f), borderPaint);

        Paint paintToUse = isFound ? foundTextPaint() : labelTextPaint;
        if (text == null || text.isEmpty()) {
            return;
        }

        float contentPadding = dp(6f);
        int layoutWidth = Math.max(1, Math.round(rect.width() - (contentPadding * 2f)));

        TextPaint textPaint = new TextPaint(paintToUse);
        textPaint.setTextAlign(Paint.Align.LEFT); // StaticLayout handles center alignment internally.

        StaticLayout layout = new StaticLayout(
                text,
                textPaint,
                layoutWidth,
                Layout.Alignment.ALIGN_CENTER,
                1.0f,
                0f,
                false
        );

        float startX = rect.left + contentPadding;
        float startY = rect.top + (rect.height() - layout.getHeight()) / 2f;

        canvas.save();
        canvas.translate(startX, startY);
        layout.draw(canvas);
        canvas.restore();
    }

    private void handleTap(TargetArea targetArea) {
        switch (targetArea) {
            case DOOR:
                if (!doorFound) {
                    doorFound = true;
                    incrementFoundCount();
                    invalidate();
                    maybeFinishLevel();
                }
                break;
            case ROPE:
                if (!ropeFound) {
                    ropeFound = true;
                    incrementFoundCount();
                    invalidate();
                    maybeFinishLevel();
                }
                break;
            case WINDOW_A:
                if (!cosWindowOpened) {
                    cosWindowOpened = true;
                    invalidate();
                }
                break;
            case WINDOW_B:
                if (!parachuteWindowOpened) {
                    parachuteWindowOpened = true;
                    invalidate();
                }
                break;
            case COSPLAY:
                if (!boardDraggedAway) {
                    registerMistakeTap();
                    break;
                }
                if (!cosplayFound) {
                    cosplayFound = true;
                    incrementFoundCount();
                    invalidate();
                    maybeFinishLevel();
                }
                break;
            case PARACHUTE:
                if (!parachuteFound) {
                    parachuteFound = true;
                    incrementFoundCount();
                    invalidate();
                    maybeFinishLevel();
                }
                break;
            case BOARD:
            case DOOR_FOUND:
            case ROPE_FOUND:
            case WINDOW_A_OPENED:
            case WINDOW_B_OPENED:
            case COSPLAY_FOUND:
            case PARACHUTE_FOUND:
            case NONE:
            default:
                if (targetArea == TargetArea.NONE) {
                    registerMistakeTap();
                }
                break;
        }
    }

    private void registerMistakeTap() {
        controller.recordMistake();
        mistakeFlashUntilRealtimeMs = SystemClock.elapsedRealtime() + MISTAKE_FLASH_DURATION_MS;
        invalidate();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void finishBoardDrag() {
        if (isBoardDroppedAtEdge()) {
            boardDraggedAway = true;
            snapBoardToEdge();
            invalidate();
            maybeFinishLevel();
        } else {
            invalidate();
        }
    }


    private void moveBoardTo(float left, float top) {
        float maxLeft = roofRect.right - boardWidth - dp(6f);
        float minLeft = roofRect.left + dp(6f);
        float maxTop = roofRect.bottom - boardHeight - dp(6f);
        float minTop = roofRect.top + dp(6f);

        float clampedLeft = clamp(left, minLeft, maxLeft);
        float clampedTop = clamp(top, minTop, maxTop);
        boardRect.set(clampedLeft, clampedTop, clampedLeft + boardWidth, clampedTop + boardHeight);
        boardTargetCenterX = boardRect.centerX();
    }

    private boolean isBoardDroppedAtEdge() {
        float edgeThreshold = dp(14f);
        return boardRect.bottom >= roofRect.bottom - edgeThreshold;
    }

    private void snapBoardToEdge() {
        float snappedTop = roofRect.bottom - boardHeight - dp(6f);
        float snappedLeft = clamp(boardTargetCenterX - boardWidth / 2f, roofRect.left + dp(6f), roofRect.right - boardWidth - dp(6f));
        boardRect.set(snappedLeft, snappedTop, snappedLeft + boardWidth, snappedTop + boardHeight);
    }

    private void maybeFinishLevel() {
        if (levelResolved) {
            return;
        }
        if (foundCount >= TOTAL_TARGET_COUNT && doorFound && ropeFound && cosplayFound && parachuteFound) {
            levelResolved = true;
            stopTimer();
            controller.finishLevelCleared();
        }
    }

    private void finishAsTimeout() {
        if (levelResolved) {
            return;
        }
        levelResolved = true;
        stopTimer();
        int unfoundCount = Math.max(TOTAL_TARGET_COUNT - foundCount, 0);
        controller.finishLevelFailed(unfoundCount);
    }

    private void incrementFoundCount() {
        foundCount = Math.min(foundCount + 1, TOTAL_TARGET_COUNT);
    }

    private void startTimer() {
        if (timerRunning || levelResolved) {
            return;
        }
        timerRunning = true;
        timerEndRealtimeMs = SystemClock.elapsedRealtime() + LEVEL_DURATION_MS;
        handler.removeCallbacks(timerTickRunnable);
        handler.post(timerTickRunnable);
    }

    private void stopTimer() {
        timerRunning = false;
        handler.removeCallbacks(timerTickRunnable);
    }

    private long getRemainingTimeMs() {
        if (!timerRunning) {
            return Math.max(timerEndRealtimeMs - SystemClock.elapsedRealtime(), 0L);
        }
        return Math.max(timerEndRealtimeMs - SystemClock.elapsedRealtime(), 0L);
    }

    private TargetArea hitTest(float x, float y) {
        if (boardRect.contains(x, y)) {
            return TargetArea.BOARD;
        }
        if (isDoorClickable() && doorRect.contains(x, y)) {
            return doorFound ? TargetArea.DOOR_FOUND : TargetArea.DOOR;
        }
        if (ropeRect.contains(x, y)) {
            return ropeFound ? TargetArea.ROPE_FOUND : TargetArea.ROPE;
        }
        if (cosWindowOpened && cosplayRect.contains(x, y)) {
            return cosplayFound ? TargetArea.COSPLAY_FOUND : TargetArea.COSPLAY;
        }
        if (parachuteWindowOpened && parachuteRect.contains(x, y)) {
            return parachuteFound ? TargetArea.PARACHUTE_FOUND : TargetArea.PARACHUTE;
        }
        if (cosplayFound && cosplayRect.contains(x, y)) {
            return TargetArea.COSPLAY_FOUND;
        }
        if (parachuteFound && parachuteRect.contains(x, y)) {
            return TargetArea.PARACHUTE_FOUND;
        }
        if (cosWindowOpened && windowLeftRect.contains(x, y)) {
            return TargetArea.WINDOW_A_OPENED;
        }
        if (parachuteWindowOpened && windowRightRect.contains(x, y)) {
            return TargetArea.WINDOW_B_OPENED;
        }
        if (windowLeftRect.contains(x, y)) {
            return cosWindowOpened ? TargetArea.WINDOW_A_OPENED : TargetArea.WINDOW_A;
        }
        if (windowRightRect.contains(x, y)) {
            return parachuteWindowOpened ? TargetArea.WINDOW_B_OPENED : TargetArea.WINDOW_B;
        }
        return TargetArea.NONE;
    }

    private boolean isDoorClickable() {
        return !RectF.intersects(boardRect, doorRect);
    }

    private void layoutScene(int width, int height) {
        float sideMargin = dp(18f);
        float hudHeight = dp(92f);
        float buildingTop = hudHeight + dp(18f);
        float buildingBottom = height - dp(34f);
        float buildingRight = width - sideMargin;

        buildingRect.set(sideMargin, buildingTop, buildingRight, buildingBottom);

        // 天台佔 60% 的空間
        float roofHeight = (buildingBottom - buildingTop) * 0.6f;
        roofRect.set(sideMargin, buildingTop, buildingRight, buildingTop + roofHeight);

        // 下層樓層佔 40%
        float floorTop = roofRect.bottom;
        upperFloorRect.set(sideMargin, floorTop, buildingRight, buildingBottom);

        boardWidth = dp(100f);
        boardHeight = dp(60f);
        float boardStartLeft = sideMargin + (buildingRight - sideMargin) * 0.35f;
        float boardStartTop = roofRect.top + dp(25f);
        boardRect.set(boardStartLeft, boardStartTop, boardStartLeft + boardWidth, boardStartTop + boardHeight);
        boardTargetCenterX = boardRect.centerX();

        // 出入口固定在原木板正下方，木板一開始會蓋住它
        float doorWidth = boardWidth - 20f;
        float doorHeight = boardHeight - 10f;
        doorRect.set(boardStartLeft, boardStartTop, boardStartLeft + doorWidth + 20f / 2, boardStartTop + doorHeight + 10f / 2);

        float ropeWidth = dp(86f);
        float ropeHeight = dp(40f);
        ropeRect.set(buildingRight - dp(26f) - ropeWidth, roofRect.top + dp(20f), buildingRight - dp(26f), roofRect.top + dp(20f) + ropeHeight);

        float windowWidth = dp(124f);
        float windowHeight = dp(76f);
        float windowTop = upperFloorRect.top + dp(20f);

        float totalWindowWidth = windowWidth * 2 + dp(24f);
        float windowStartX = sideMargin + (buildingRight - sideMargin - totalWindowWidth) / 2f;

        windowLeftRect.set(windowStartX, windowTop, windowStartX + windowWidth, windowTop + windowHeight);
        windowRightRect.set(windowStartX + windowWidth + dp(24f), windowTop, windowStartX + windowWidth * 2 + dp(24f), windowTop + windowHeight);

        float itemPadding = dp(8f);
        cosplayRect.set(windowLeftRect.left + itemPadding, windowLeftRect.top + itemPadding, windowLeftRect.right - itemPadding, windowLeftRect.bottom - itemPadding);
        parachuteRect.set(windowRightRect.left + itemPadding, windowRightRect.top + itemPadding, windowRightRect.right - itemPadding, windowRightRect.bottom - itemPadding);

        if (!boardDraggedAway) {
            boardRect.set(boardStartLeft, boardStartTop, boardStartLeft + boardWidth, boardStartTop + boardHeight);
        }
    }

    private void drawTextWithBaseline(Canvas canvas, String text, float x, float baseline, Paint paint) {
        canvas.drawText(text, x, baseline, paint);
    }

    private Paint activeLabelPaint() {
        Paint paint = new Paint(labelPaint);
        paint.setColor(Color.parseColor("#E0A95A"));
        return paint;
    }

    private Paint leapLabelPaint() {
        Paint paint = new Paint(labelPaint);
        paint.setColor(Color.parseColor("#8FD18E"));
        return paint;
    }

    private Paint foundLabelPaint() {
        Paint paint = new Paint(labelPaint);
        paint.setColor(Color.parseColor("#6AA8E8"));
        return paint;
    }

    private Paint openedWindowLabelPaint() {
        Paint paint = new Paint(labelPaint);
        paint.setColor(Color.parseColor("#8E8DB7"));
        return paint;
    }

    private Paint foundBorderPaint() {
        Paint paint = new Paint(labelStrokePaint);
        paint.setColor(Color.parseColor("#1E4B3B"));
        return paint;
    }

    private Paint foundTextPaint() {
        Paint paint = new Paint(labelTextPaint);
        paint.setColor(Color.parseColor("#0D1B26"));
        return paint;
    }

    private Paint closedWindow() {
        Paint paint = new Paint(labelPaint);
        paint.setColor(Color.parseColor("#9fc5e8"));
        return paint;
    }

    private float dp(float value) {
        return value * density;
    }

    private float sp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private String formatMinutes(int minutes) {
        return String.format(Locale.getDefault(), "%02d", Math.max(minutes, 0));
    }

    private String formatSeconds(int seconds) {
        return String.format(Locale.getDefault(), "%02d", Math.max(seconds, 0));
    }
}







