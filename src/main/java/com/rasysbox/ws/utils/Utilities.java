package com.rasysbox.ws.utils;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class Utilities {

    public static String generateIsoTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS Z");
        OffsetDateTime dateTime = OffsetDateTime.now(ZoneOffset.ofHours(-5));
        return dateTime.format(formatter);
    }

    public static Map<String, Object> refactorStatsEntry(Map<String, Object> entry) {
        String stats = (String) entry.get("stats");
        String containerId = (String) entry.get("container_id");
        String timestamp = (String) entry.get("timestamp");

        // Split stats into lines and process
        String[] lines = stats.split("\r\n");
        if (lines.length < 2) {
            throw new IllegalArgumentException("Invalid stats format");
        }

        // Extract headers and values
        String[] headers = lines[0].trim().split("\\s{2,}"); // Split by two or more spaces
        String[] values = lines[1].trim().split("\\s{2,}");

        // Create a structured JSON object for stats
        Map<String, String> statsJson = new LinkedHashMap<>();
        for (int i = 0; i < headers.length; i++) {
            statsJson.put(headers[i].toLowerCase().replace(" ", "_"), i < values.length ? values[i] : "");
        }

        // Build the refactored entry
        Map<String, Object> refactoredEntry = new LinkedHashMap<>();
        refactoredEntry.put("container_id", containerId);
        refactoredEntry.put("timestamp", timestamp);
        refactoredEntry.put("stats", statsJson);

        return refactoredEntry;
    }
}
