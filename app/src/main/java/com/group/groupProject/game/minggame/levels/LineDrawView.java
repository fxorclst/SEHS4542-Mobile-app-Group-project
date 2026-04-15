package com.group.groupProject.game.minggame.levels;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LineDrawView extends View {

    public interface OnDrawResultListener {
        void onDrawResult(DrawResult result);
    }

    public enum DrawResult {
        CORRECT_END,
        FAKE_END,
        NOWHERE
    }

    private static final float LINE_WIDTH_DP = 16f;
    private static final int LINE_COLOR = Color.BLACK;
    private static final float ENDPOINT_RADIUS_DP = 50f;
    private static final int ENDPOINT_CIRCLE_COLOR = Color.parseColor("#00FF00");
    private static final float ENDPOINT_CIRCLE_ALPHA = 0.3f;
    private static final float START_POINT_RADIUS_DP = 8f;
    private static final int START_POINT_COLOR = Color.parseColor("#0000FF");
    private static final int FAKE_HIT_FLASH_COLOR = Color.RED;
    private static final long FAKE_HIT_FLASH_DURATION_MS = 180L;

    private Paint linePaint;
    private Paint endpointCirclePaint;
    private Paint startPointPaint;
    private Paint fakeHitFlashPaint;
    private Path currentPath;
    private RectF viewBounds;

    private float startPointX;
    private float startPointY;
    private float trueEndPointX;
    private float trueEndPointY;
    private float fakeEndPointX;
    private float fakeEndPointY;

    private float endpointRadiusPx;
    private float startPointRadiusPx;

    private OnDrawResultListener drawResultListener;
    private boolean hasDrawn = false;
    private long fakeHitFlashEndTimeMs;

    public LineDrawView(Context context) {
        super(context);
        init();
    }

    public LineDrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineDrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Convert dp to pixels
        float density = getContext().getResources().getDisplayMetrics().density;
        float lineWidthPx = LINE_WIDTH_DP * density;
        endpointRadiusPx = ENDPOINT_RADIUS_DP * density;
        startPointRadiusPx = START_POINT_RADIUS_DP * density;

        // Initialize line paint
        linePaint = new Paint();
        linePaint.setColor(LINE_COLOR);
        linePaint.setStrokeWidth(lineWidthPx);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.BEVEL);
        linePaint.setAntiAlias(true);

        // Initialize endpoint circle paint
        endpointCirclePaint = new Paint();
        endpointCirclePaint.setColor(ENDPOINT_CIRCLE_COLOR);
        endpointCirclePaint.setStyle(Paint.Style.FILL);
        endpointCirclePaint.setAntiAlias(true);
        endpointCirclePaint.setAlpha((int) (255 * ENDPOINT_CIRCLE_ALPHA));

        // Initialize start point paint
        startPointPaint = new Paint();
        startPointPaint.setColor(START_POINT_COLOR);
        startPointPaint.setStyle(Paint.Style.FILL);
        startPointPaint.setAntiAlias(true);
        startPointPaint.setAlpha((int) (255 * 0.5f));

        fakeHitFlashPaint = new Paint();
        fakeHitFlashPaint.setColor(FAKE_HIT_FLASH_COLOR);
        fakeHitFlashPaint.setStyle(Paint.Style.FILL);
        fakeHitFlashPaint.setAlpha((int) (255 * 0.24f));

        currentPath = new Path();
        viewBounds = new RectF();
        setBackgroundColor(Color.WHITE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewBounds.set(0f, 0f, w, h);
        updateEndpointPositions(w, h);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getWidth() > 0 && getHeight() > 0) {
            updateEndpointPositions(getWidth(), getHeight());
            invalidate();
        }
    }

    private void updateEndpointPositions(int width, int height) {
        if (isLandscape()) {
            // Landscape: left -> right = start, fake, true.
            startPointX = width * 0.1f;
            startPointY = height * 0.5f;
            fakeEndPointX = width * 0.5f;
            fakeEndPointY = height * 0.5f;
            trueEndPointX = width * 0.9f;
            trueEndPointY = height * 0.5f;
        } else {
            // Portrait: fake endpoint is available at the bottom; true endpoint is disabled.
            startPointX = width * 0.5f;
            startPointY = height * 0.15f;
            trueEndPointX = -endpointRadiusPx * 2f;
            trueEndPointY = -endpointRadiusPx * 2f;
            fakeEndPointX = width * 0.5f;
            fakeEndPointY = height * 0.85f;
        }
    }

    private boolean isLandscape() {
        return getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Landscape shows both fake and true endpoints; portrait shows fake only.
        if (isLandscape()) {
            canvas.drawCircle(fakeEndPointX, fakeEndPointY, endpointRadiusPx, endpointCirclePaint);
            canvas.drawCircle(trueEndPointX, trueEndPointY, endpointRadiusPx, endpointCirclePaint);
        } else {
            canvas.drawCircle(fakeEndPointX, fakeEndPointY, endpointRadiusPx, endpointCirclePaint);
        }

        // Draw start point
        canvas.drawCircle(startPointX, startPointY, startPointRadiusPx, startPointPaint);

        // Draw current path
        canvas.drawPath(currentPath, linePaint);

        if (System.currentTimeMillis() < fakeHitFlashEndTimeMs) {
            canvas.drawRect(viewBounds, fakeHitFlashPaint);
            postInvalidateOnAnimation();
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Clear previous path and start new one
                currentPath.reset();
                currentPath.moveTo(x, y);
                hasDrawn = false;
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                currentPath.lineTo(x, y);
                hasDrawn = true;
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                performClick();
                if (hasDrawn) {
                    // Check collision with endpoints
                    DrawResult result = checkCollision(x, y);
                    if (!isLandscape() && result == DrawResult.FAKE_END) {
                        triggerFakeHitFlash();
                    }
                    if (drawResultListener != null) {
                        drawResultListener.onDrawResult(result);
                    }
                }
                return true;

            default:
                return false;
        }
    }

    private DrawResult checkCollision(float endX, float endY) {
        if (isLandscape()) {
            float distanceToFake = (float) Math.sqrt(
                    Math.pow(endX - fakeEndPointX, 2) + Math.pow(endY - fakeEndPointY, 2)
            );
            float distanceToTrue = (float) Math.sqrt(
                    Math.pow(endX - trueEndPointX, 2) + Math.pow(endY - trueEndPointY, 2)
            );
            if (distanceToTrue <= endpointRadiusPx) {
                return DrawResult.CORRECT_END;
            }
            if (distanceToFake <= endpointRadiusPx) {
                return DrawResult.FAKE_END;
            }
            return DrawResult.NOWHERE;
        }

        float distanceToFake = (float) Math.sqrt(
                Math.pow(endX - fakeEndPointX, 2) + Math.pow(endY - fakeEndPointY, 2)
        );
        return distanceToFake <= endpointRadiusPx ? DrawResult.FAKE_END : DrawResult.NOWHERE;
    }

    private void triggerFakeHitFlash() {
        fakeHitFlashEndTimeMs = System.currentTimeMillis() + FAKE_HIT_FLASH_DURATION_MS;
        invalidate();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void setOnDrawResultListener(OnDrawResultListener listener) {
        this.drawResultListener = listener;
    }

    public void reset() {
        currentPath.reset();
        hasDrawn = false;
        invalidate();
    }
}

