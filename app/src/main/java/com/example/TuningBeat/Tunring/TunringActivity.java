package com.example.TuningBeat.Tunring;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.TuningBeat.Beat.BeatActivity;
import com.example.TuningBeat.MainActivity;
import com.example.TuningBeat.R;

public class TunringActivity extends AppCompatActivity {
    private static final int AUDIO_PERMISSION_CODE = 100;
    // UI组件
    private TextView noteTextView;
    private TextView frequencyTextView;
    private TextView accuracyTextView;
    private TextView instrumentTextView;
    private TextView tuningStatusTextView;
    private Spinner instrumentSpinner;
    // 业务组件
    private AudioRecorderService audioRecorder;
    private PitchDetector pitchDetector;
    private TuningAnalyzer tuningAnalyzer;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;
    // 支持的乐器
    private String[] instruments = {"吉他", "钢琴", "小提琴", "贝斯", "尤克里里", "萨克斯", "长笛","通用"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tunring);
        initializeUI();
        initializeComponents();
        checkPermissions();
    }
    private void initializeUI() {
        noteTextView = findViewById(R.id.noteTextView);
        frequencyTextView = findViewById(R.id.frequencyTextView);
        accuracyTextView = findViewById(R.id.accuracyTextView);
        instrumentTextView = findViewById(R.id.instrumentTextView);
        tuningStatusTextView = findViewById(R.id.tuningStatusTextView);
        instrumentSpinner = findViewById(R.id.instrumentSpinner);
        // 设置乐器下拉菜单
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, instruments);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        instrumentSpinner.setAdapter(adapter);
        instrumentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedInstrument = instruments[position];
                instrumentTextView.setText("当前乐器: " + selectedInstrument);
                if (tuningAnalyzer != null) {
                    tuningAnalyzer.setCurrentInstrument(selectedInstrument);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    private void initializeComponents() {
        pitchDetector = new PitchDetector();
        tuningAnalyzer = new TuningAnalyzer();
        // 初始化乐器配置文件
        InstrumentProfile.initializeProfiles();
        tuningAnalyzer.setCurrentInstrument(instruments[0]);
    }
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    AUDIO_PERMISSION_CODE);
        } else {
            startTuner();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTuner();
            }
        }
    }
    private void startTuner() {
        audioRecorder = new AudioRecorderService();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        audioRecorder.startRecording();
        isRunning = true;
        // 开始音频处理循环
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    processAudioData();
                    try {
                        Thread.sleep(50); // 毫秒更新率
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    private void processAudioData() {
        if (audioRecorder == null) return;
        short[] audioData = audioRecorder.getAudioData();
        if (audioData.length > 0) {
            // 检测基频
            double frequency = pitchDetector.detectPitch(audioData,
                    audioRecorder.getSampleRate());
            if (frequency > 0) {
                // 分析音调
                TuningAnalyzer.TuningResult result = tuningAnalyzer.analyze(frequency);
                // 更新UI
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(result);
                    }
                });
            }
        }
    }
    private void updateUI(TuningAnalyzer.TuningResult result) {
        noteTextView.setText(result.getNoteName());
        frequencyTextView.setText(String.format("%.1f Hz", result.getFrequency()));
        accuracyTextView.setText(String.format("%.1f cents", result.getCentsOff()));
        // 设置调音状态
        String status;
        int color;
        if (Math.abs(result.getCentsOff()) <= 5) {
            status = "完美";
            color = getResources().getColor(android.R.color.holo_green_dark);
        } else if (Math.abs(result.getCentsOff()) <= 20) {
            status = "合格";
            color = getResources().getColor(android.R.color.holo_orange_light);
        } else {
            status = result.getCentsOff() > 0 ? "偏高" : "偏低";
            color = getResources().getColor(android.R.color.holo_red_dark);
        }
        tuningStatusTextView.setText(status);
        tuningStatusTextView.setTextColor(color);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (audioRecorder != null) {
            audioRecorder.stopRecording();
        }
    }
}