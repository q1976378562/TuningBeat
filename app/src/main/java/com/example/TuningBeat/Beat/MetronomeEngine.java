package com.example.TuningBeat.Beat;
import android.os.Handler;
import android.os.Looper;
public class MetronomeEngine {
    private Handler handler;
    private Runnable beatRunnable;
    private SoundManager soundManager;
    private volatile boolean isRunning = false;
    private int bpm = 120; // 默认速度
    private BeatPattern currentPattern;
    private int currentBeatIndex = 0;//一小节中的节拍索引
    // 监听器接口
    public interface MetronomeListener {
        void onBeat(int beatIndex, int accentType);
        void onPatternComplete();
    }
    private MetronomeListener listener;
    public MetronomeEngine(SoundManager soundManager) {
        this.soundManager = soundManager;
        this.handler = new Handler(Looper.getMainLooper());
        this.currentPattern = BeatPattern.getCommonPatterns().get(0); // 默认2/4拍
        initBeatRunnable();
    }
    private void initBeatRunnable() {
        beatRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRunning) return;
                // 播放当前拍
                int accentType = currentPattern.getAccentPattern()[currentBeatIndex];
                soundManager.playBeat(accentType);
                // 通知监听器
                if (listener != null) {
                    listener.onBeat(currentBeatIndex, accentType);
                }
                // 更新节拍索引
                currentBeatIndex++;
                if (currentBeatIndex >= currentPattern.getPatternLength()) {
                    currentBeatIndex = 0;
                    if (listener != null) {
                        listener.onPatternComplete();
                    }
                }
                // 计算延迟并安排下一次节拍
                long delay = calculateDelay();
                handler.postDelayed(this, delay);
            }
        };
    }
    private long calculateDelay() {
        // 计算每拍的时间（毫秒）
        // 60,000毫秒 / BPM = 每拍的毫秒数
        double beatDuration = 60000.0 / bpm;
        // 根据拍号分母调整（如4/4拍的分母4表示四分音符为一拍）
        // 这里我们假设都是以四分音符为基础
        if (currentPattern.getDenominator() == 8) {
            // 对于8分音符为基础的拍号，时间减半
            beatDuration /= 2;
        }
        return (long) beatDuration;
    }
    public void start() {
        if (isRunning) return;
        isRunning = true;
        currentBeatIndex = 0;
        handler.post(beatRunnable);
    }
    public void stop() {
        isRunning = false;
        handler.removeCallbacks(beatRunnable);
        currentBeatIndex = 0;
    }
    public void setBPM(int bpm) {
        if (bpm < 30) bpm = 30;
        if (bpm > 240) bpm = 240;
        this.bpm = bpm;
    }
    public void setPattern(BeatPattern pattern) {
        this.currentPattern = pattern;
        this.currentBeatIndex = 0;
    }
    public boolean isRunning() {
        return isRunning;
    }
    public int getBPM() {
        return bpm;
    }
    public BeatPattern getCurrentPattern() {
        return currentPattern;
    }
    public int getCurrentBeatIndex() {
        return currentBeatIndex;
    }
    public void setListener(MetronomeListener listener) {
        this.listener = listener;
    }
}
