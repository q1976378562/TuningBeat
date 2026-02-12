package com.example.TuningBeat.Tunring;
import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import androidx.annotation.RequiresPermission;
public class AudioRecorderService {

    private static final int SAMPLE_RATE = 44100;
    //单声道配置
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 2;

    private AudioRecord audioRecord;
    private short[] audioBuffer;
    private boolean isRecording = false;
    private Thread recordingThread;
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public void startRecording() {
        if (isRecording) return;
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
        );
        audioRecord.startRecording();
        isRecording = true;
        audioBuffer = new short[BUFFER_SIZE];
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRecording) {
                    int bytesRead = audioRecord.read(audioBuffer, 0, BUFFER_SIZE);
                    if (bytesRead > 0) {
                        // 数据已存储在audioBuffer中
                    }
                }
            }
        });
        recordingThread.start();
    }
    public short[] getAudioData() {
        if (audioBuffer != null) {
            applyHanningWindow(audioBuffer);
            //返回副本，避免竞争
            return audioBuffer.clone();
        }
        return new short[0];
    }
    private void applyHanningWindow(short[] data) {
        for (int i = 0; i < data.length; i++) {
            double window = 0.5 * (1 - Math.cos(2 * Math.PI * i / (data.length - 1)));
            data[i] = (short)(data[i] * window);
        }
    }
    //返回采样率，实际频率（频率 = 采样率 × 频谱峰值位置 / 数据长度）
    public int getSampleRate() {
        return SAMPLE_RATE;
    }
    public void stopRecording() {
        isRecording = false;
        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            audioRecord = null;
        }
        if (recordingThread != null) {
            try {
                recordingThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            recordingThread = null;
        }
    }
}