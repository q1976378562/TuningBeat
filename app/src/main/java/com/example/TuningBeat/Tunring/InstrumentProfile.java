package com.example.TuningBeat.Tunring;

import java.util.HashMap;
import java.util.Map;
public class InstrumentProfile {

    private static Map<String, InstrumentTuning> instrumentTunings;

    public static class InstrumentTuning {
        private String name;
        private String[] standardNotes;
        private double[] standardFrequencies;
        private double minFrequency;
        private double maxFrequency;

        public InstrumentTuning(String name, String[] notes, double[] frequencies) {
            this.name = name;
            this.standardNotes = notes;
            this.standardFrequencies = frequencies;
            this.minFrequency = frequencies[0] * 0.5;
            this.maxFrequency = frequencies[frequencies.length - 1] * 2;
        }

        public String findClosestNote(double frequency) {
            if (frequency < minFrequency ) {
                return "过低";
            }
            if(frequency > maxFrequency){
                return "过高";
            }

            int closestIndex = 0;
            double minDifference = Double.MAX_VALUE;

            for (int i = 0; i < standardFrequencies.length; i++) {
                double diff = Math.abs(frequency - standardFrequencies[i]);
                if (diff < minDifference) {
                    minDifference = diff;
                    closestIndex = i;
                }
            }

            return standardNotes[closestIndex];
        }

        public double getTargetFrequency(String note) {
            for (int i = 0; i < standardNotes.length; i++) {
                if (standardNotes[i].equals(note)) {
                    return standardFrequencies[i];
                }
            }
            return 0;
        }
    }

    public static void initializeProfiles() {
        instrumentTunings = new HashMap<>();

        // 1. 吉他 (6弦标准调弦)
        instrumentTunings.put("吉他",
                new InstrumentTuning("吉他",
                        new String[]{"E2", "A2", "D3", "G3", "B3", "E4"},
                        new double[]{82.41, 110.00, 146.83, 196.00, 246.94, 329.63}
                ));

        // 2. 钢琴 (常用音域，中央C附近两个八度)
        instrumentTunings.put("钢琴",
                new InstrumentTuning("钢琴",
                        new String[]{"C3", "D3", "E3", "F3", "G3", "A3", "B3",
                                "C4", "D4", "E4", "F4", "G4", "A4", "B4",
                                "C5", "D5", "E5", "F5", "G5", "A5", "B5"},
                        new double[]{130.81, 146.83, 164.81, 174.61, 196.00, 220.00, 246.94,
                                261.63, 293.66, 329.63, 349.23, 392.00, 440.00, 493.88,
                                523.25, 587.33, 659.25, 698.46, 783.99, 880.00, 987.77}
                ));

        // 3. 小提琴 (4弦标准调弦)
        instrumentTunings.put("小提琴",
                new InstrumentTuning("小提琴",
                        new String[]{"G3", "D4", "A4", "E5"},
                        new double[]{196.00, 293.66, 440.00, 659.25}
                ));

        // 4. 贝斯 (4弦标准调弦- 比吉他低一个八度)
        instrumentTunings.put("贝斯",
                new InstrumentTuning("贝斯",
                        new String[]{"E1", "A1", "D2", "G2"},
                        new double[]{41.20, 55.00, 73.42, 98.00}
                ));

        // 5. 尤克里里 (4弦标准调弦-高音G调弦)
        instrumentTunings.put("尤克里里",
                new InstrumentTuning("尤克里里",
                        new String[]{"G4", "C4", "E4", "A4"},
                        new double[]{392.00, 261.63, 329.63, 440.00}
                ));

        // 6. 萨克斯 (降B调或降E调乐器，这里以最常用的中音萨克斯降B调指法表音高)
        instrumentTunings.put("萨克斯",
                new InstrumentTuning("萨克斯",
                        new String[]{"Bb3", "C4", "D4", "Eb4", "F4", "G4", "A4", "Bb4", "C5", "D5", "Eb5", "F5", "G5"},
                        new double[]{233.08, 261.63, 293.66, 311.13, 349.23, 392.00, 440.00,
                                466.16, 523.25, 587.33, 622.25, 698.46, 783.99}
                ));

        // 7. 长笛 (C调乐器，实际音高)
        instrumentTunings.put("长笛",
                new InstrumentTuning("长笛",
                        new String[]{"C4", "D4", "E4", "F4", "G4", "A4", "B4",
                                "C5", "D5", "E5", "F5", "G5", "A5", "B5",
                                "C6", "D6", "E6", "F6", "G6"},
                        new double[]{261.63, 293.66, 329.63, 349.23, 392.00, 440.00, 493.88,
                                523.25, 587.33, 659.25, 698.46, 783.99, 880.00, 987.77,
                                1046.50, 1174.66, 1318.51, 1396.91, 1567.98}
                ));
    }

    public static InstrumentTuning getInstrumentTuning(String instrumentName) {
        return instrumentTunings.get(instrumentName);
    }
}
