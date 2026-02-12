package com.example.TuningBeat.Beat;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.TuningBeat.R;

import java.util.List;

public class BeatActivity extends AppCompatActivity implements MetronomeEngine.MetronomeListener {
    private MetronomeEngine metronomeEngine;
    private SoundManager soundManager;

    private TextView bpmTextView;
    private TextView beatIndicator;
    private TextView patternInfo;
    private SeekBar bpmSeekBar;
    private Spinner patternSpinner;
    private Button startStopButton;
    private List<BeatPattern> patterns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beat);
        initViews();
        initSoundManager();
        initMetronomeEngine();
        setupSpinner();
        setupListeners();
    }
    private void initViews() {
        bpmTextView = findViewById(R.id.bpm_text);
        beatIndicator = findViewById(R.id.beat_indicator);
        patternInfo = findViewById(R.id.pattern_info);
        bpmSeekBar = findViewById(R.id.bpm_seekbar);
        patternSpinner = findViewById(R.id.pattern_spinner);
        startStopButton = findViewById(R.id.start_stop_button);
    }
    private void initSoundManager() {
        soundManager = new SoundManager(this);
    }
    private void initMetronomeEngine() {
        metronomeEngine = new MetronomeEngine(soundManager);
        metronomeEngine.setListener(this);
        // 设置默认BPM 120
        bpmSeekBar.setProgress(metronomeEngine.getBPM());
        updateBPMText(metronomeEngine.getBPM());
    }
    private void setupSpinner() {
        patterns = BeatPattern.getCommonPatterns();
        ArrayAdapter<BeatPattern> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, patterns);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        patternSpinner.setAdapter(adapter);
        // 默认选择2/4拍
        patternSpinner.setSelection(0);//选择控件
        updatePatternInfo(patterns.get(0));//文本展示
    }
    private void setupListeners() {
        // BPM进度条调节监听
        bpmSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateBPMText(progress);
                metronomeEngine.setBPM(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        // 节拍模式选择监听
        patternSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BeatPattern selectedPattern = patterns.get(position);
                metronomeEngine.setPattern(selectedPattern);
                updatePatternInfo(selectedPattern);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        // 开始/停止按钮监听
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (metronomeEngine.isRunning()) {
                    metronomeEngine.stop();
                    startStopButton.setText("开始");
                    beatIndicator.setText("●");
                } else {
                    metronomeEngine.start();
                    startStopButton.setText("停止");
                }
            }
        });
    }
    private void updateBPMText(int bpm) {
        bpmTextView.setText(bpm + " BPM");
    }
    private void updatePatternInfo(BeatPattern pattern) {
        patternInfo.setText(pattern.getName() + " - " + pattern.getNumerator() + "/" + pattern.getDenominator());
    }
    @Override
    public void onBeat(int beatIndex, int accentType) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 更新节拍指示器
                String indicator;
                switch (accentType) {
                    case 1:
                        indicator = "●"; // 强拍
                        beatIndicator.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        break;
                    default:
                        indicator = "•"; // 弱拍
                        beatIndicator.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                        break;
                }
                beatIndicator.setText(indicator);
                // 跳动动画效果，区分每拍
                beatIndicator.setScaleX(1.3f);
                beatIndicator.setScaleY(1.3f);
                beatIndicator.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .start();
            }
        });
    }
    @Override
    public void onPatternComplete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 每小节完成
                beatIndicator.setBackground(getResources().getDrawable(R.drawable.circle_background));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        metronomeEngine.stop();
        soundManager.release();
    }
}