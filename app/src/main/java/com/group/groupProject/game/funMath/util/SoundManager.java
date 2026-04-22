package com.group.groupProject.funmath.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;

public class SoundManager {
    private static SoundManager instance;
    private Context context;
    private ToneGenerator toneGenerator;
    private boolean soundEnabled = true;

    private SoundManager(Context context) {
        this.context = context;
        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        } catch (Exception e) {
            toneGenerator = null;
        }
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context.getApplicationContext());
        }
        return instance;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public void playButtonClick() {
        if (!soundEnabled || toneGenerator == null) return;
        try {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 50);
        } catch (Exception e) {
        }
    }

    public void playCorrect() {
        if (!soundEnabled || toneGenerator == null) return;
        try {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 150);
        } catch (Exception e) {
        }
    }

    public void playWrong() {
        if (!soundEnabled || toneGenerator == null) return;
        try {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, 200);
        } catch (Exception e) {
        }
    }

    public void playLevelComplete() {
        if (!soundEnabled || toneGenerator == null) return;
        try {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 300);
            Thread.sleep(100);
            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
        }
    }

    public void release() {
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }
}