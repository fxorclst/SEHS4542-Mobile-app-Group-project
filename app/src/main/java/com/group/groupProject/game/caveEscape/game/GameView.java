package com.group.groupProject.game.caveEscape.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.group.groupProject.R;
import com.group.groupProject.caveescape.model.LevelData;

import java.util.ArrayList;
import java.util.List;

public class GameView extends View implements SensorEventListener {

    private float ballX, ballY;
    private float ballVelocityX = 0, ballVelocityY = 0;
    private float ballRadius;
    private float holeX, holeY, holeRadius;

    private float levelWidth, levelHeight;
    private float gravity, friction, bounceFactor;

    private float holePixelX, holePixelY, holePixelRadius;
    private float ballPixelRadius;
    private float ballStartPixelX, ballStartPixelY;

    private List<ObstacleData> obstacles = new ArrayList<>();
    private List<HazardData> hazards = new ArrayList<>();
    private List<float[]> trail = new ArrayList<>();

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float tiltX = 0, tiltY = 0;
    private static final float TILT_SENSITIVITY = 0.8f;
    private static final float MAX_TILT = 10f;

    private boolean gameRunning = false;
    private boolean levelComplete = false;
    private boolean levelCompleteProcessed = false;
    private boolean gameOver = false;
    private boolean physicsInitialized = false;
    private float elapsedTime = 0;
    private float maxTime;

    private Paint ballPaint, holePaint, obstaclePaint, hazardPaint, trailPaint, bgPaint;
    private int bgColor, accentColor, obstacleColor, hazardColor, trailColor;

    private LevelData currentLevel;

    private OnGameEventListener listener;

    public interface OnGameEventListener {
        void onLevelComplete(float time, int score);
        void onGameOver();
        void onTimeUpdate(float time);
        void onWallHit();
        void onObstacleHit();
        void onResetHit();
    }

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bgColor = Color.WHITE;
        accentColor = getContext().getColor(R.color.cave_accent);
        obstacleColor = getContext().getColor(R.color.obstacle_color);
        hazardColor = getContext().getColor(R.color.cave_accent);
        trailColor = getContext().getColor(R.color.trail_color);

        ballPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ballPaint.setColor(accentColor);
        ballPaint.setStyle(Paint.Style.FILL);

        holePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        holePaint.setColor(Color.BLACK);
        holePaint.setStyle(Paint.Style.FILL);

        obstaclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        obstaclePaint.setColor(obstacleColor);
        obstaclePaint.setStyle(Paint.Style.FILL);

        hazardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hazardPaint.setColor(hazardColor);
        hazardPaint.setStyle(Paint.Style.FILL);

        trailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trailPaint.setColor(trailColor);
        trailPaint.setStyle(Paint.Style.FILL);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(bgColor);
        bgPaint.setStyle(Paint.Style.FILL);

        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    public void setLevel(LevelData level) {
        this.currentLevel = level;
        this.gravity = level.getGravity();
        this.friction = level.getFriction();
        this.bounceFactor = level.getBounceFactor();
        this.maxTime = level.getMaxTime();

        ballRadius = level.getBallRadius();
        holeRadius = level.getHoleRadius();
        holeX = level.getHoleX();
        holeY = level.getHoleY();

        levelWidth = 1.0f;
        levelHeight = 1.0f;

        resetBall();
        loadObstacles();
        loadHazards();
        trail.clear();
    }

    private void resetBall() {
        ballX = currentLevel.getBallStartX();
        ballY = currentLevel.getBallStartY();
        ballVelocityX = 0;
        ballVelocityY = 0;
    }

    private void loadObstacles() {
        obstacles.clear();
        for (LevelData.Obstacle obs : currentLevel.getObstacles()) {
            obstacles.add(new ObstacleData(obs));
        }
    }

    private void loadHazards() {
        hazards.clear();
        for (LevelData.Hazard haz : currentLevel.getHazards()) {
            hazards.add(new HazardData(haz));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void startGame() {
        gameRunning = true;
        levelComplete = false;
        levelCompleteProcessed = false;
        gameOver = false;
        physicsInitialized = false;
        elapsedTime = 0;
        resetBall();
        trail.clear();

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        invalidate();
    }

    public void pauseGame() {
        gameRunning = false;
        if (accelerometer != null) {
            sensorManager.unregisterListener(this);
        }
    }

    public void resumeGame() {
        if (!levelComplete && !gameOver) {
            gameRunning = true;
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
            }
        }
    }

    public void stopGame() {
        gameRunning = false;
        if (accelerometer != null) {
            sensorManager.unregisterListener(this);
        }
    }

    public void reset() {
        stopGame();
        startGame();
    }

    public void setOnGameEventListener(OnGameEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            tiltX = event.values[0];
            tiltY = event.values[1];

            tiltX = Math.max(-MAX_TILT, Math.min(MAX_TILT, tiltX));
            tiltY = Math.max(-MAX_TILT, Math.min(MAX_TILT, tiltY));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        float minDim = Math.min(w, h);

        canvas.drawRect(0, 0, w, h, bgPaint);

        for (ObstacleData obs : obstacles) {
            float obsX = obs.x * w - obs.width * w / 2;
            float obsY = obs.y * h - obs.height * h / 2;
            RectF rect = new RectF(obsX, obsY, obsX + obs.width * w, obsY + obs.height * h);
            canvas.drawRoundRect(rect, 8, 8, obstaclePaint);
        }

        for (HazardData haz : hazards) {
            float hazX = haz.x * w;
            float hazY = haz.y * h;
            float hazRadius = haz.radius * (float) Math.sqrt(w * h);
            canvas.drawCircle(hazX, hazY, hazRadius, hazardPaint);
        }

        holePixelX = holeX * w;
        holePixelY = holeY * h;
        holePixelRadius = (float) (holeRadius * Math.sqrt(w * w + h * h) * 0.707f);
        canvas.drawCircle(holePixelX, holePixelY, holePixelRadius, holePaint);

        for (float[] point : trail) {
            float trailX = point[0] * w;
            float trailY = point[1] * h;
            float trailRadius = ballPixelRadius * 0.5f;
            canvas.drawCircle(trailX, trailY, trailRadius, trailPaint);
        }

        ballPixelRadius = (float) (ballRadius * Math.sqrt(w * w + h * h) * 0.707f);
        canvas.drawCircle(ballX * w, ballY * h, ballPixelRadius, ballPaint);
    }

    public void update(float deltaTime) {
        if (!gameRunning || levelComplete || gameOver) return;

        elapsedTime += deltaTime;
        if (listener != null) {
            listener.onTimeUpdate(elapsedTime);
        }

        if (elapsedTime >= maxTime) {
            gameOver = true;
            gameRunning = false;
            if (listener != null) {
                listener.onGameOver();
            }
            return;
        }

        if (!physicsInitialized) {
            if (elapsedTime < 0.15f) {
                invalidate();
                return;
            }
            physicsInitialized = true;
        }

        ballVelocityX += (-tiltX / MAX_TILT) * TILT_SENSITIVITY * gravity * deltaTime;
        ballVelocityY += (tiltY / MAX_TILT) * TILT_SENSITIVITY * gravity * deltaTime;

        ballVelocityX *= friction;
        ballVelocityY *= friction;

        ballX += ballVelocityX * deltaTime;
        ballY += ballVelocityY * deltaTime;

        boolean hitWall = checkWallCollisions();
        if (hitWall && listener != null) {
            listener.onWallHit();
        }
        boolean hitObstacle = checkObstacleCollisions();
        if (hitObstacle && listener != null) {
            listener.onObstacleHit();
        }
        boolean hitReset = checkHazardCollisions();
        if (hitReset && listener != null) {
            listener.onResetHit();
        }
        checkHoleReached();

        if (trail.size() > 20) {
            trail.remove(0);
        }
        trail.add(new float[]{ballX, ballY});

        invalidate();
    }

    private boolean checkWallCollisions() {
        boolean hitWall = false;

        if (ballX - ballRadius <= 0) {
            ballX = ballRadius;
            ballVelocityX = -ballVelocityX * bounceFactor;
            hitWall = true;
        }
        if (ballX + ballRadius >= 1.0f) {
            ballX = 1.0f - ballRadius;
            ballVelocityX = -ballVelocityX * bounceFactor;
            hitWall = true;
        }
        if (ballY - ballRadius <= 0) {
            ballY = ballRadius;
            ballVelocityY = -ballVelocityY * bounceFactor;
            hitWall = true;
        }
        if (ballY + ballRadius >= 1.0f) {
            ballY = 1.0f - ballRadius;
            ballVelocityY = -ballVelocityY * bounceFactor;
            hitWall = true;
        }
        return hitWall;
    }

    private float getPixelRadius(float normalizedRadius) {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return 20f;
        float aspectRatio = (float) w / h;
        if (aspectRatio >= 1.0f) {
            return normalizedRadius * w;
        } else {
            return normalizedRadius * h;
        }
    }

    private boolean checkObstacleCollisions() {
        boolean hitObstacle = false;
        float pixelBallRadius = getPixelRadius(ballRadius);
        float w = getWidth();
        float h = getHeight();

        for (ObstacleData obs : obstacles) {
            float obsLeft = (obs.x - obs.width / 2) * w;
            float obsRight = (obs.x + obs.width / 2) * w;
            float obsTop = (obs.y - obs.height / 2) * h;
            float obsBottom = (obs.y + obs.height / 2) * h;

            float closestX = Math.max(obsLeft, Math.min(ballX * w, obsRight));
            float closestY = Math.max(obsTop, Math.min(ballY * h, obsBottom));

            float distX = ballX * w - closestX;
            float distY = ballY * h - closestY;
            float dist = (float) Math.sqrt(distX * distX + distY * distY);

            if (dist <= pixelBallRadius && dist > 0) {
                hitObstacle = true;
                float overlap = pixelBallRadius - dist;
                ballX += (distX / dist) * overlap / w;
                ballY += (distY / dist) * overlap / h;

                float normalX = distX / dist;
                float normalY = distY / dist;

                float dotProduct = ballVelocityX * normalX + ballVelocityY * normalY;
                ballVelocityX = (ballVelocityX - 2 * dotProduct * normalX) * bounceFactor;
                ballVelocityY = (ballVelocityY - 2 * dotProduct * normalY) * bounceFactor;
            }
        }
        return hitObstacle;
    }

    private boolean checkHazardCollisions() {
        boolean hitReset = false;
        float pixelBallRadius = getPixelRadius(ballRadius);
        float w = getWidth();
        float h = getHeight();

        for (HazardData haz : hazards) {
            float dx = ballX * w - haz.x * w;
            float dy = ballY * h - haz.y * h;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            float pixelHazardRadius = getPixelRadius(haz.radius);

            if (dist <= pixelBallRadius + pixelHazardRadius) {
                hitReset = true;
                resetBall();
                ballVelocityX = 0;
                ballVelocityY = 0;
                trail.clear();
                return hitReset;
            }
        }
        return hitReset;
    }

    private void checkHoleReached() {
        if (!physicsInitialized) return;

        float w = getWidth();
        float h = getHeight();
        float pixelBallRadius = getPixelRadius(ballRadius);
        float pixelHoleRadius = getPixelRadius(holeRadius);

        float dx = ballX * w - holeX * w;
        float dy = ballY * h - holeY * h;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (!levelCompleteProcessed && dist <= pixelHoleRadius + pixelBallRadius) {
            float speed = (float) Math.sqrt(ballVelocityX * ballVelocityX + ballVelocityY * ballVelocityY);
            if (speed < 0.3f) {
                levelComplete = true;
                levelCompleteProcessed = true;
                gameRunning = false;
                int score = calculateScore(elapsedTime);
                if (listener != null) {
                    listener.onLevelComplete(elapsedTime, score);
                }
            }
        }
    }

    private int calculateScore(float time) {
        int parTime = currentLevel.getParTime();
        if (time <= parTime) {
            return 100;
        }
        float ratio = (float) parTime / time;
        return Math.max(1, (int) (100 * ratio));
    }

    public boolean isLevelComplete() {
        return levelComplete;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public float getElapsedTime() {
        return elapsedTime;
    }

    public int getMaxTime() {
        return (int) maxTime;
    }

    public void release() {
        stopGame();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    private static class ObstacleData {
        float x, y, width, height;

        ObstacleData(LevelData.Obstacle obs) {
            this.x = obs.getX();
            this.y = obs.getY();
            this.width = obs.getWidth();
            this.height = obs.getHeight();
        }
    }

    private static class HazardData {
        float x, y, radius;
        float pulsePhase;

        HazardData(LevelData.Hazard haz) {
            this.x = haz.getX();
            this.y = haz.getY();
            this.radius = haz.getRadius();
            this.pulsePhase = haz.getPulsePhase();
        }
    }
}
