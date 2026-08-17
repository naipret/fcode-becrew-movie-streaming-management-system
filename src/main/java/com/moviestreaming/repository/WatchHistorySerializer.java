package com.moviestreaming.repository;

import com.moviestreaming.model.WatchHistoryItem;
import com.moviestreaming.util.CsvSanitizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * CSV Serializer for WatchHistoryItem entity.
 */
public class WatchHistorySerializer implements CsvSerializer<WatchHistoryItem, String> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public String getHeader() {
        return "id|userId|movieId|watchedDurationMinutes|totalDurationMinutes|lastWatchedTimestamp|isCompleted";
    }

    @Override
    public String serialize(WatchHistoryItem item) {
        if (item == null) {
            return "";
        }
        String timestampStr = item.getLastWatchedTimestamp() != null
                ? item.getLastWatchedTimestamp().format(DATE_FORMATTER)
                : LocalDateTime.now().format(DATE_FORMATTER);

        return CsvSanitizer.join(Arrays.asList(
                item.getId(),
                item.getUserId(),
                item.getMovieId(),
                String.valueOf(item.getWatchedDurationMinutes()),
                String.valueOf(item.getTotalDurationMinutes()),
                timestampStr,
                String.valueOf(item.isCompleted())
        ));
    }

    @Override
    public WatchHistoryItem deserialize(String csvLine) {
        List<String> tokens = CsvSanitizer.split(csvLine);
        if (tokens.size() < 7) {
            throw new IllegalArgumentException("Invalid WatchHistory CSV format, expected 7 columns but got: " + tokens.size());
        }

        String id = tokens.get(0);
        String userId = tokens.get(1);
        String movieId = tokens.get(2);
        int watchedDurationMinutes = Integer.parseInt(tokens.get(3).trim());
        int totalDurationMinutes = Integer.parseInt(tokens.get(4).trim());
        LocalDateTime timestamp = LocalDateTime.parse(tokens.get(5).trim(), DATE_FORMATTER);
        boolean completed = Boolean.parseBoolean(tokens.get(6).trim());

        return new WatchHistoryItem(id, userId, movieId, watchedDurationMinutes, totalDurationMinutes, timestamp, completed);
    }

    @Override
    public String extractId(WatchHistoryItem item) {
        return item != null ? item.getId() : null;
    }
}
