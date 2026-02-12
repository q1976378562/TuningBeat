package com.example.TuningBeat.Tunring;

public class TuningAnalyzer {

    private String currentInstrument = "吉他";

    // 十二平均律计算
    private static final double[] NOTE_FREQUENCIES;
    private static final String[] NOTE_NAMES = {
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };
    static {
        NOTE_FREQUENCIES = new double[108]; // A0到C8
        for (int i = 0; i < NOTE_FREQUENCIES.length; i++) {
            NOTE_FREQUENCIES[i] = 27.5 * Math.pow(2, (i - 9) / 12.0);
        }
    }
    public TuningResult analyze(double detectedFrequency) {
        // 使用乐器特定分析
        InstrumentProfile.InstrumentTuning tuning =
                InstrumentProfile.getInstrumentTuning(currentInstrument);
        String closestNote;
        double targetFrequency;

        if (tuning != null) {
            closestNote = tuning.findClosestNote(detectedFrequency);
            targetFrequency = tuning.getTargetFrequency(closestNote);
        } else {
            // 通用十二平均律分析
            int noteIndex = findClosestNoteIndex(detectedFrequency);
            closestNote = getNoteName(noteIndex);
            targetFrequency = NOTE_FREQUENCIES[noteIndex];
        }
        // 计算音分差
        double cents = calculateCents(detectedFrequency, targetFrequency);

        return new TuningResult(closestNote, detectedFrequency, cents);
    }
    private int findClosestNoteIndex(double frequency) {
        if (frequency < NOTE_FREQUENCIES[0]) return 0;
        if (frequency > NOTE_FREQUENCIES[NOTE_FREQUENCIES.length - 1])
            return NOTE_FREQUENCIES.length - 1;
        int left = 0;
        int right = NOTE_FREQUENCIES.length - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (NOTE_FREQUENCIES[mid] < frequency) {
                left = mid + 1;
            } else if (NOTE_FREQUENCIES[mid] > frequency) {
                right = mid - 1;
            } else {
                return mid;
            }
        }
        return (Math.abs(NOTE_FREQUENCIES[left] - frequency) <
                Math.abs(NOTE_FREQUENCIES[right] - frequency)) ? left : right;
    }
    private String getNoteName(int index) {
        int octave = 0 + (index + 9) / 12;
        int noteIndex = (index + 9) % 12;
        return NOTE_NAMES[noteIndex] + octave;
    }
    private double calculateCents(double detected, double target) {
        if (target == 0) return 0;
        return 1200 * Math.log(detected / target) / Math.log(2);
    }
    public void setCurrentInstrument(String instrument) {
        this.currentInstrument = instrument;
    }
    public static class TuningResult {
        private String noteName;
        private double frequency;
        private double centsOff;
        public TuningResult(String noteName, double frequency, double centsOff) {
            this.noteName = noteName;
            this.frequency = frequency;
            this.centsOff = centsOff;
        }
        public String getNoteName() { return noteName; }
        public double getFrequency() { return frequency; }
        public double getCentsOff() { return centsOff; }
    }
}
