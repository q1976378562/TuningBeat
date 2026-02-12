package com.example.TuningBeat.Beat;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import com.example.TuningBeat.R;

public class SoundManager {
    private SoundPool soundPool;
    private int strongBeatSoundId;
    private int weakBeatSoundId;
    private boolean loaded = false;
    public SoundManager(Context context) {
        // 创建SoundPool(进行版本适配)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(3)
                    .setAudioAttributes(attributes)
                    .build();
        } else {
            soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        }
        // 加载声音文件
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    loaded = true;
                }
            }
        });
        // 加载音频文件 - 在res/raw目录下
        strongBeatSoundId = soundPool.load(context, R.raw.strong, 1);
        weakBeatSoundId = soundPool.load(context, R.raw.weak, 1);
    }
    public void playBeat(int accentType) {
        if (!loaded) return;

        switch (accentType) {
            case 0: // 弱拍
                soundPool.play(weakBeatSoundId, 0.4f, 0.4f, 1, 0, 1.0f);
                break;
            case 1: // 强拍
                soundPool.play(strongBeatSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
                break;
        }
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}