package com.muimi.kichat.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Kitimer {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    private static volatile long lastCachedSecond = -1;
    private static volatile String cachedTimeString = "";

    public static String getCurrentTime() {
        long currentMillis = System.currentTimeMillis();
        long currentSecond = currentMillis / 1000;
        if (currentSecond == lastCachedSecond) return cachedTimeString;
        return updateCache(currentSecond, currentMillis);
    }

    private static String updateCache(long currentSecond, long currentMillis) {
        if (currentSecond == lastCachedSecond) return cachedTimeString;
        String newTimeString = FORMATTER.format(Instant.ofEpochMilli(currentMillis));
        cachedTimeString = newTimeString;
        lastCachedSecond = currentSecond;
        return newTimeString;
    }
}