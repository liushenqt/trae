package com.qianqian.music.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LrcParser {

    public static class LrcLine implements Comparable<LrcLine> {
        public long time;
        public String text;

        public LrcLine(long time, String text) {
            this.time = time;
            this.text = text;
        }

        @Override
        public int compareTo(LrcLine other) {
            return Long.compare(this.time, other.time);
        }
    }

    private static final Pattern TIME_PATTERN = Pattern.compile(
            "\\[(\\d{2}):(\\d{2})[.:](\\d{2,3})\\]"
    );

    public static List<LrcLine> parseLrc(File lrcFile) {
        List<LrcLine> lines = new ArrayList<>();
        if (lrcFile == null || !lrcFile.exists()) {
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(lrcFile), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                List<Long> times = new ArrayList<>();
                Matcher matcher = TIME_PATTERN.matcher(line);

                while (matcher.find()) {
                    long minutes = Long.parseLong(matcher.group(1));
                    long seconds = Long.parseLong(matcher.group(2));
                    String millisStr = matcher.group(3);
                    long millis = Long.parseLong(millisStr);
                    if (millisStr.length() == 2) {
                        millis *= 10;
                    }
                    long time = minutes * 60 * 1000 + seconds * 1000 + millis;
                    times.add(time);
                }

                String text = line.replaceAll("\\[\\d{2}:\\d{2}[.:]\\d{2,3}\\]", "").trim();
                if (text.isEmpty() && times.size() > 1) {
                    continue;
                }

                for (Long time : times) {
                    lines.add(new LrcLine(time, text));
                }
            }

            Collections.sort(lines);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lines;
    }

    public static File findLrcFile(String musicFilePath) {
        if (musicFilePath == null) return null;

        int dotIndex = musicFilePath.lastIndexOf('.');
        if (dotIndex > 0) {
            String lrcPath = musicFilePath.substring(0, dotIndex) + ".lrc";
            File lrcFile = new File(lrcPath);
            if (lrcFile.exists()) {
                return lrcFile;
            }
        }

        return null;
    }

    public static int findCurrentLine(List<LrcLine> lines, long currentTime) {
        if (lines == null || lines.isEmpty()) return -1;

        int result = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).time <= currentTime) {
                result = i;
            } else {
                break;
            }
        }
        return result;
    }
}
