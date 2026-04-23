package com.group.groupProject.caveescape.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LevelData implements Serializable {
    private int levelNumber;
    private String levelName;
    private float ballStartX;
    private float ballStartY;
    private float holeX;
    private float holeY;
    private float holeRadius;
    private float ballRadius;
    private float maxTime;
    private List<Obstacle> obstacles;
    private List<Hazard> hazards;
    private float gravity;
    private float friction;
    private float bounceFactor;
    private int parTime;
    private boolean hasMovingObstacles;
    private boolean hasTeleporters;
    private float[][] teleportPairs;
    private int[][] wallLayout;

    public LevelData() {
        this.obstacles = new ArrayList<>();
        this.hazards = new ArrayList<>();
    }

    public static List<LevelData> createAllLevels() {
        List<LevelData> levels = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            levels.add(createLevel(i));
        }

        return levels;
    }

    public static LevelData createLevel(int levelNum) {
        LevelData level = new LevelData();
        level.setLevelNumber(levelNum);

        switch (levelNum) {
            case 1:
                level.setLevelName("Cave Entrance");
                level.setBallStartX(0.15f);
                level.setBallStartY(0.5f);
                level.setHoleX(0.5f);
                level.setHoleY(0.5f);
                level.setHoleRadius(0.05f);
                level.setBallRadius(0.025f);
                level.setMaxTime(60f);
                level.setParTime(5);
                level.setGravity(0.3f);
                level.setFriction(0.98f);
                level.setBounceFactor(0.7f);
                break;

            case 2:
                level.setLevelName("First Descent");
                level.setBallStartX(0.5f);
                level.setBallStartY(0.1f);
                level.setHoleX(0.5f);
                level.setHoleY(0.5f);
                level.setHoleRadius(0.05f);
                level.setBallRadius(0.025f);
                level.setMaxTime(60f);
                level.setParTime(6);
                level.setGravity(0.3f);
                level.setFriction(0.98f);
                level.setBounceFactor(0.7f);
                addWall(level, 0.25f, 0.5f, 0.02f, 0.3f);
                addWall(level, 0.75f, 0.5f, 0.02f, 0.3f);
                break;

            case 3:
                level.setLevelName("Narrow Passage");
                level.setBallStartX(0.1f);
                level.setBallStartY(0.2f);
                level.setHoleX(0.5f);
                level.setHoleY(0.5f);
                level.setHoleRadius(0.05f);
                level.setBallRadius(0.025f);
                level.setMaxTime(60f);
                level.setParTime(8);
                level.setGravity(0.3f);
                level.setFriction(0.98f);
                level.setBounceFactor(0.7f);
                addWall(level, 0.3f, 0.35f, 0.4f, 0.02f);
                addWall(level, 0.7f, 0.65f, 0.4f, 0.02f);
                addWall(level, 0.3f, 0.65f, 0.02f, 0.3f);
                addWall(level, 0.7f, 0.35f, 0.02f, 0.3f);
                break;

            case 4:
                level.setLevelName("The Maze");
                level.setBallStartX(0.1f);
                level.setBallStartY(0.1f);
                level.setHoleX(0.5f);
                level.setHoleY(0.5f);
                level.setHoleRadius(0.05f);
                level.setBallRadius(0.025f);
                level.setMaxTime(60f);
                level.setParTime(12);
                level.setGravity(0.3f);
                level.setFriction(0.98f);
                level.setBounceFactor(0.7f);
                addWall(level, 0.3f, 0.3f, 0.02f, 0.4f);
                addWall(level, 0.5f, 0.7f, 0.4f, 0.02f);
                addWall(level, 0.7f, 0.3f, 0.02f, 0.4f);
                break;

            case 5:
                level.setLevelName("Speed Drop");
                level.setBallStartX(0.5f);
                level.setBallStartY(0.1f);
                level.setHoleX(0.5f);
                level.setHoleY(0.9f);
                level.setHoleRadius(0.05f);
                level.setBallRadius(0.025f);
                level.setMaxTime(60f);
                level.setParTime(4);
                level.setGravity(0.3f);
                level.setFriction(0.98f);
                level.setBounceFactor(0.7f);
                addWall(level, 0.3f, 0.35f, 0.4f, 0.02f);
                addWall(level, 0.7f, 0.5f, 0.02f, 0.3f);
                addWall(level, 0.3f, 0.65f, 0.4f, 0.02f);
                addWall(level, 0.5f, 0.8f, 0.02f, 0.2f);
                break;

            case 6:
                level.setLevelName("Bounce House");
                level.setBallStartX(0.15f);
                level.setBallStartY(0.5f);
                level.setHoleX(0.85f);
                level.setHoleY(0.5f);
                level.setHoleRadius(0.05f);
                level.setBallRadius(0.025f);
                level.setMaxTime(60f);
                level.setParTime(7);
                level.setGravity(0.3f);
                level.setFriction(0.98f);
                level.setBounceFactor(0.7f);
                addWall(level, 0.35f, 0.25f, 0.2f, 0.02f);
                addWall(level, 0.6f, 0.25f, 0.2f, 0.02f);
                addWall(level, 0.35f, 0.75f, 0.2f, 0.02f);
                addWall(level, 0.6f, 0.75f, 0.2f, 0.02f);
                break;

            case 7:
                level.setLevelName("Gravity Wells");
                level.setBallStartX(0.5f);
                level.setBallStartY(0.15f);
                level.setHoleX(0.85f);
                level.setHoleY(0.85f);
                level.setHoleRadius(0.05f);
                level.setBallRadius(0.025f);
                level.setMaxTime(60f);
                level.setParTime(10);
                level.setGravity(0.3f);
                level.setFriction(0.98f);
                level.setBounceFactor(0.7f);
                addHazard(level, 0.25f, 0.4f, 0.05f);
                addHazard(level, 0.4f, 0.6f, 0.05f);
                addHazard(level, 0.6f, 0.25f, 0.05f);
                addHazard(level, 0.75f, 0.5f, 0.05f);
                addHazard(level, 0.5f, 0.75f, 0.05f);
                addHazard(level, 0.25f, 0.85f, 0.04f);
                addHazard(level, 0.85f, 0.25f, 0.04f);
                break;

            case 8:
                level.setLevelName("Precision Path");
                level.setBallStartX(0.15f);
                level.setBallStartY(0.85f);
                level.setHoleX(0.85f);
                level.setHoleY(0.15f);
                level.setHoleRadius(0.05f);
                level.setBallRadius(0.025f);
                level.setMaxTime(60f);
                level.setParTime(15);
                level.setGravity(0.3f);
                level.setFriction(0.98f);
                level.setBounceFactor(0.7f);
                addWall(level, 0.25f, 0.7f, 0.02f, 0.3f);
                addWall(level, 0.4f, 0.4f, 0.3f, 0.02f);
                addWall(level, 0.65f, 0.7f, 0.02f, 0.25f);
                addWall(level, 0.75f, 0.35f, 0.25f, 0.02f);
                addHazard(level, 0.5f, 0.25f, 0.04f);
                addHazard(level, 0.35f, 0.85f, 0.04f);
                break;

            case 9:
                level.setLevelName("Chaos Chamber");
                level.setBallStartX(0.15f);
                level.setBallStartY(0.15f);
                level.setHoleX(0.85f);
                level.setHoleY(0.85f);
                level.setHoleRadius(0.05f);
                level.setBallRadius(0.025f);
                level.setMaxTime(60f);
                level.setParTime(12);
                level.setGravity(0.3f);
                level.setFriction(0.98f);
                level.setBounceFactor(0.7f);
                addWall(level, 0.3f, 0.3f, 0.25f, 0.02f);
                addWall(level, 0.7f, 0.3f, 0.02f, 0.25f);
                addWall(level, 0.3f, 0.7f, 0.25f, 0.02f);
                addWall(level, 0.7f, 0.7f, 0.02f, 0.25f);
                addWall(level, 0.5f, 0.15f, 0.02f, 0.2f);
                addHazard(level, 0.4f, 0.5f, 0.04f);
                addHazard(level, 0.6f, 0.5f, 0.04f);
                addHazard(level, 0.5f, 0.6f, 0.04f);
                addHazard(level, 0.15f, 0.85f, 0.04f);
                addHazard(level, 0.85f, 0.15f, 0.04f);
                break;

            case 10:
                level.setLevelName("The Gauntlet");
                level.setBallStartX(0.15f);
                level.setBallStartY(0.85f);
                level.setHoleX(0.85f);
                level.setHoleY(0.15f);
                level.setHoleRadius(0.05f);
                level.setBallRadius(0.025f);
                level.setMaxTime(60f);
                level.setParTime(20);
                level.setGravity(0.3f);
                level.setFriction(0.98f);
                level.setBounceFactor(0.7f);
                addWall(level, 0.3f, 0.7f, 0.02f, 0.25f);
                addWall(level, 0.3f, 0.3f, 0.02f, 0.2f);
                addWall(level, 0.5f, 0.5f, 0.02f, 0.35f);
                addWall(level, 0.7f, 0.3f, 0.02f, 0.25f);
                addWall(level, 0.7f, 0.7f, 0.25f, 0.02f);
                addWall(level, 0.5f, 0.25f, 0.3f, 0.02f);
                addHazard(level, 0.4f, 0.4f, 0.04f);
                addHazard(level, 0.6f, 0.4f, 0.04f);
                addHazard(level, 0.4f, 0.6f, 0.04f);
                addHazard(level, 0.6f, 0.6f, 0.04f);
                addHazard(level, 0.85f, 0.5f, 0.04f);
                addHazard(level, 0.15f, 0.15f, 0.035f);
                break;
        }

        return level;
    }

    private static void addWall(LevelData level, float x, float y, float width, float height) {
        level.getObstacles().add(new Obstacle(x, y, width, height, Obstacle.RECTANGLE));
    }

    private static void addHazard(LevelData level, float x, float y, float radius) {
        level.getHazards().add(new Hazard(x, y, radius));
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public float getBallStartX() {
        return ballStartX;
    }

    public void setBallStartX(float ballStartX) {
        this.ballStartX = ballStartX;
    }

    public float getBallStartY() {
        return ballStartY;
    }

    public void setBallStartY(float ballStartY) {
        this.ballStartY = ballStartY;
    }

    public float getHoleX() {
        return holeX;
    }

    public void setHoleX(float holeX) {
        this.holeX = holeX;
    }

    public float getHoleY() {
        return holeY;
    }

    public void setHoleY(float holeY) {
        this.holeY = holeY;
    }

    public float getHoleRadius() {
        return holeRadius;
    }

    public void setHoleRadius(float holeRadius) {
        this.holeRadius = holeRadius;
    }

    public float getBallRadius() {
        return ballRadius;
    }

    public void setBallRadius(float ballRadius) {
        this.ballRadius = ballRadius;
    }

    public float getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(float maxTime) {
        this.maxTime = maxTime;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public void setObstacles(List<Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    public List<Hazard> getHazards() {
        return hazards;
    }

    public void setHazards(List<Hazard> hazards) {
        this.hazards = hazards;
    }

    public float getGravity() {
        return gravity;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public float getFriction() {
        return friction;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public float getBounceFactor() {
        return bounceFactor;
    }

    public void setBounceFactor(float bounceFactor) {
        this.bounceFactor = bounceFactor;
    }

    public int getParTime() {
        return parTime;
    }

    public void setParTime(int parTime) {
        this.parTime = parTime;
    }

    public boolean isHasMovingObstacles() {
        return hasMovingObstacles;
    }

    public void setHasMovingObstacles(boolean hasMovingObstacles) {
        this.hasMovingObstacles = hasMovingObstacles;
    }

    public float[][] getTeleportPairs() {
        return teleportPairs;
    }

    public void setTeleportPairs(float[][] teleportPairs) {
        this.teleportPairs = teleportPairs;
    }

    public int[][] getWallLayout() {
        return wallLayout;
    }

    public void setWallLayout(int[][] wallLayout) {
        this.wallLayout = wallLayout;
    }

    public static class Obstacle implements Serializable {
        public static final int RECTANGLE = 0;
        public static final int CIRCLE = 1;

        private float x, y, width, height, radius;
        private int type;
        private float velocityX, velocityY;
        private float minX, maxX, minY, maxY;

        public Obstacle(float x, float y, float width, float height, int type) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.type = type;
            this.radius = Math.max(width, height) / 2;
            this.minX = x - width / 2;
            this.maxX = x + width / 2;
            this.minY = y - height / 2;
            this.maxY = y + height / 2;
        }

        public float getX() { return x; }
        public void setX(float x) { this.x = x; }
        public float getY() { return y; }
        public void setY(float y) { this.y = y; }
        public float getWidth() { return width; }
        public float getHeight() { return height; }
        public float getRadius() { return radius; }
        public int getType() { return type; }
        public float getVelocityX() { return velocityX; }
        public void setVelocityX(float velocityX) { this.velocityX = velocityX; }
        public float getVelocityY() { return velocityY; }
        public void setVelocityY(float velocityY) { this.velocityY = velocityY; }
        public float getMinX() { return minX; }
        public float getMaxX() { return maxX; }
        public float getMinY() { return minY; }
        public float getMaxY() { return maxY; }
    }

    public static class Hazard implements Serializable {
        private float x, y, radius;
        private float pulsePhase;

        public Hazard(float x, float y, float radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.pulsePhase = (float) (Math.random() * Math.PI * 2);
        }

        public float getX() { return x; }
        public float getY() { return y; }
        public float getRadius() { return radius; }
        public float getPulsePhase() { return pulsePhase; }
        public void setPulsePhase(float pulsePhase) { this.pulsePhase = pulsePhase; }
    }
}