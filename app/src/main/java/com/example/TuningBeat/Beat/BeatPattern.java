package com.example.TuningBeat.Beat;
import java.util.ArrayList;
import java.util.List;
public class BeatPattern {
    private String name;
    private int numerator;  //拍号分子
    private int denominator; //拍号分母
    private int[] accentPattern; // 强弱模式数组 (1=强拍,0=弱拍)
    public BeatPattern(String name, int numerator, int denominator, int[] accentPattern) {
        this.name = name;
        this.numerator = numerator;
        this.denominator = denominator;
        this.accentPattern = accentPattern;
    }
    public static List<BeatPattern> getCommonPatterns() {
        List<BeatPattern> patterns = new ArrayList<>();
        // 2/4拍 - 强弱
        patterns.add(new BeatPattern("2/4 拍", 2, 4, new int[]{1, 0}));
        // 3/4拍 - 强弱弱
        patterns.add(new BeatPattern("3/4 拍", 3, 4, new int[]{1, 0, 0}));
        // 4/4拍 - 强 弱 强 弱
        patterns.add(new BeatPattern("4/4 拍", 4, 4, new int[]{1, 0, 1, 0}));
        // 5/4拍 - 强 弱 强 弱 弱
        patterns.add(new BeatPattern("5/4 拍", 5, 4, new int[]{1, 0, 2, 0, 0}));
        // 6/8拍 - 强 弱 弱 强 弱 弱
        patterns.add(new BeatPattern("6/8 拍", 6, 8, new int[]{1, 0, 0, 1, 0, 0}));
        // 7/8拍 - 强 弱 弱 强 弱 弱 弱
        patterns.add(new BeatPattern("7/8 拍", 7, 8, new int[]{1, 0, 0, 1, 0, 0, 0}));
        // 12/8拍 - 强 弱 弱 强 弱 弱 强 弱 弱 弱 弱
        patterns.add(new BeatPattern("12/8 拍", 12, 8, new int[]{1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0}));
        return patterns;
    }
    // Getters
    public String getName() { return name; }
    public int getNumerator() { return numerator; }
    public int getDenominator() { return denominator; }
    public int[] getAccentPattern() { return accentPattern; }
    public int getPatternLength() { return accentPattern.length; }

    @Override
    public String toString() {
        return name;
    }
}