package com.moviestreaming.repository;

import com.moviestreaming.model.WatchHistoryItem;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete file-backed repository for user watch history.
 */
public class WatchHistoryRepository extends GenericFileRepository<WatchHistoryItem, String> {

    public WatchHistoryRepository(String filePath) {
        super(filePath, new WatchHistorySerializer());
    }

    /**
     * Retrieves viewing history records for a specific user.
     *
     * @param userId the user ID
     * @return list of watch history items for this user
     */
    public List<WatchHistoryItem> findByUserId(String userId) {
        if (userId == null) {
            return java.util.Collections.emptyList();
        }
        synchronized (cache) {
            return cache.values().stream()
                    .filter(h -> h.getUserId() != null && h.getUserId().equals(userId))
                    .collect(Collectors.toList());
        }
    }
}
