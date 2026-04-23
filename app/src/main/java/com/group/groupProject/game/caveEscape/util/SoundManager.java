package com.group.groupProject.caveescape.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;

public class SoundManager {

    private static SoundManager instance;
    private Context context;
    private ToneGenerator toneGenerator;

    private SoundManager(Context context) {
        this.context = context.getApplicationContext();
        initSounds();
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    private void initSounds() {
        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        } catch (Exception e) {
            toneGenerator = null;
        }
    }

    public void playWallHit() {
        if (toneGenerator != null) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 50);
        }
    }

    public void playObstacleHit() {
        if (toneGenerator != null) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 80);
        }
    }

    public void playFinishReached() {
        if (toneGenerator != null) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 150);
        }
    }

    public void playResetHit() {
        if (toneGenerator != null) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, 100);
        }
    }

    public void playButtonClick() {
        if (toneGenerator != null) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 30);
        }
    }

    public void release() {
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
        instance = null;
    }
}
