package com.example.TuningBeat.Tunring;
import java.util.Arrays;
public class PitchDetector {

    // 使用自相关法进行音高检测
    public double detectPitch(short[] audioData, int sampleRate) {
        if (audioData.length < 4096) {
            return 0;
        }
        // 方法1: 自相关法
        double frequency = detectByAutocorrelation(audioData, sampleRate);
        // 方法2: YIN算法
        if (frequency <= 0 || frequency > 2000) {
            frequency = detectByYIN(audioData, sampleRate);
        }
        // 使用HPS(谐波积频谱)进一步验证
        if (frequency > 0 && frequency < 1000) {
            double hpsFrequency = detectByHPS(audioData, sampleRate);
            if (Math.abs(hpsFrequency - frequency) / frequency < 0.1) {
                frequency = (frequency + hpsFrequency) / 2;
            }
        }
        return frequency;
    }

    private double detectByAutocorrelation(short[] data, int sampleRate) {
        int maxLag = sampleRate / 50; // 最低频率20Hz
        int minLag = sampleRate / 2000; // 最高频率2000Hz
        double[] autocorrelation = new double[maxLag];
        // 计算自相关
        for (int lag = minLag; lag < maxLag; lag++) {
            double sum = 0;
            for (int i = 0; i < data.length - lag; i++) {
                sum += data[i] * data[i + lag];
            }
            autocorrelation[lag] = sum;
        }
        // 寻找第一个峰值
        int peakIndex = findFirstPeak(autocorrelation, minLag);
        //频率 = 采样率 / 滞后值（峰值索引）
        if (peakIndex > minLag) {
            // 使用抛物线插值提高精度
            peakIndex = parabolicInterpolation(autocorrelation, peakIndex);
            return (double) sampleRate / peakIndex;
        }
        return 0;
    }

    private int findFirstPeak(double[] data, int startIndex) {
        // 寻找超过阈值的第一个峰值
        double maxValue = 0;
        int maxIndex = startIndex;
        for (int i = startIndex; i < data.length - 1; i++) {
            if (data[i] > maxValue && data[i] > data[i-1] && data[i] > data[i+1]) {
                maxValue = data[i];
                maxIndex = i;
                break;
            }
        }
        return maxIndex;
    }

    private int parabolicInterpolation(double[] data, int peakIndex) {
        if (peakIndex <= 0 || peakIndex >= data.length - 1) {
            return peakIndex;
        }

        double alpha = data[peakIndex - 1];
        double beta = data[peakIndex];
        double gamma = data[peakIndex + 1];

        return (int) (peakIndex + (alpha - gamma) / (2 * (alpha - 2*beta + gamma)));
    }

    private double detectByYIN(short[] data, int sampleRate) {
        int tau;
        int bufferSize = Math.min(data.length, 8192);
        double[] difference = new double[bufferSize/2];
        // YIN算法实现
        for (tau = 1; tau < bufferSize/2; tau++) {
            double sum = 0;
            for (int j = 0; j < bufferSize/2; j++) {
                double delta = data[j] - data[j + tau];
                sum += delta * delta;
            }
            difference[tau] = sum;
        }
        // 寻找第一个谷值
        int tauEstimate = findFirstTrough(difference);

        if (tauEstimate > 0) {
            return (double) sampleRate / tauEstimate;
        }
        return 0;
    }

    private int findFirstTrough(double[] difference) {
        for (int tau = 1; tau < difference.length - 1; tau++) {
            if (difference[tau] < difference[tau-1] &&
                    difference[tau] < difference[tau+1]) {
                return tau;
            }
        }
        return -1;
    }

    private double detectByHPS(short[] data, int sampleRate) {
        // 谐波积频谱法
        int n = 2048;
        double[] spectrum = computeFFT(data, n);
        // 下采样并相乘
        double[] hps = spectrum.clone();

        for (int downsample = 2; downsample <= 6; downsample++) {
            for (int i = 0; i < n/(2*downsample); i++) {
                hps[i] *= spectrum[i * downsample];
            }
        }
        // 寻找最大峰值
        int maxIndex = 0;
        double maxValue = 0;
        for (int i = 10; i < n/2; i++) {
            if (hps[i] > maxValue) {
                maxValue = hps[i];
                maxIndex = i;
            }
        }
        return maxIndex * (double) sampleRate / n;
    }

    private double[] computeFFT(short[] data, int n) {
        // 简化FFT实现（实际中应使用库如JTransform）
        double[] real = new double[n];
        double[] imag = new double[n];
        for (int i = 0; i < Math.min(n, data.length); i++) {
            real[i] = data[i];
        }
        // 这里应实现FFT算法
        // 为简化，返回幅度谱
        double[] spectrum = new double[n/2];
        for (int i = 0; i < n/2; i++) {
            spectrum[i] = Math.sqrt(real[i]*real[i] + imag[i]*imag[i]);
        }
        return spectrum;
    }
}