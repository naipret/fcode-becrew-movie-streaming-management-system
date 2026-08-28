package com.moviestreaming.command;

import static org.assertj.core.api.Assertions.assertThat;

import com.moviestreaming.repository.MovieRepository;
import com.moviestreaming.repository.UserListRepository;
import com.moviestreaming.service.WatchlistService;
import com.moviestreaming.service.WatchlistUndoRedoService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Watchlist Command Pattern & Undo/Redo Test Suite")
class WatchlistCommandTest {

    private Path tempDir;
    private WatchlistService watchlistService;
    private WatchlistUndoRedoService undoRedoService;
    private MovieRepository movieRepository;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("watchlist_cmd_test");
        String moviesPath = tempDir.resolve("movies.csv").toString();
        String watchlistPath = tempDir.resolve("watchlists.csv").toString();

        List<String> movieLines = Arrays.asList(
                "id|title|categoryId|director|actors|releaseYear|durationMinutes|rating|viewCount|favoriteCount|synopsis",
                "MOV-001|Inception|CAT-01|Christopher Nolan|Leonardo DiCaprio|2010|148|8.8|1000|500|Dream heist.",
                "MOV-002|Interstellar|CAT-01|Christopher Nolan|Matthew McConaughey|2014|169|8.7|2000|800|Space exploration.",
                "MOV-003|The Dark Knight|CAT-02|Christopher Nolan|Christian Bale|2008|152|9.0|3000|1500|Batman vs Joker.",
                "MOV-004|Spirited Away|CAT-03|Hayao Miyazaki|Rumi Hiiragi|2001|125|8.6|1200|400|Spirit bathhouse.",
                "MOV-005|Dune Part Two|CAT-01|Denis Villeneuve|Timothee Chalamet|2024|166|8.6|5000|2000|Arrakis desert.",
                "MOV-006|Oppenheimer|CAT-03|Christopher Nolan|Cillian Murphy|2023|180|8.9|4500|1800|Atomic bomb.");
        Files.write(tempDir.resolve("movies.csv"), movieLines);

        movieRepository = new MovieRepository(moviesPath);
        UserListRepository userListRepository = new UserListRepository(watchlistPath);
        watchlistService = new WatchlistService(userListRepository, movieRepository);
        undoRedoService = new WatchlistUndoRedoService(5); // max 5 for testing bounds
    }

    @AfterEach
    void tearDown() {
        if (tempDir != null) {
            File[] files = tempDir.toFile().listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            tempDir.toFile().delete();
        }
    }

    @Test
    @DisplayName("Should execute, undo and redo AddToWatchlistCommand")
    void shouldExecuteUndoRedoAddCommand() {
        String userId = "USR-001";
        AddToWatchlistCommand cmd =
                new AddToWatchlistCommand(watchlistService, userId, "MOV-001", "Inception");

        undoRedoService.execute(userId, cmd);
        assertThat(watchlistService.isInWatchlist(userId, "MOV-001")).isTrue();
        assertThat(undoRedoService.canUndo(userId)).isTrue();
        assertThat(undoRedoService.canRedo(userId)).isFalse();

        Optional<WatchlistCommand> undone = undoRedoService.undo(userId);
        assertThat(undone).isPresent();
        assertThat(watchlistService.isInWatchlist(userId, "MOV-001")).isFalse();
        assertThat(undoRedoService.canRedo(userId)).isTrue();

        Optional<WatchlistCommand> redone = undoRedoService.redo(userId);
        assertThat(redone).isPresent();
        assertThat(watchlistService.isInWatchlist(userId, "MOV-001")).isTrue();
    }

    @Test
    @DisplayName("Should execute, undo and redo RemoveFromWatchlistCommand")
    void shouldExecuteUndoRedoRemoveCommand() {
        String userId = "USR-001";
        watchlistService.addToWatchlist(userId, "MOV-001");
        assertThat(watchlistService.isInWatchlist(userId, "MOV-001")).isTrue();

        RemoveFromWatchlistCommand cmd =
                new RemoveFromWatchlistCommand(watchlistService, userId, "MOV-001", "Inception");
        undoRedoService.execute(userId, cmd);
        assertThat(watchlistService.isInWatchlist(userId, "MOV-001")).isFalse();

        undoRedoService.undo(userId);
        assertThat(watchlistService.isInWatchlist(userId, "MOV-001")).isTrue();

        undoRedoService.redo(userId);
        assertThat(watchlistService.isInWatchlist(userId, "MOV-001")).isFalse();
    }

    @Test
    @DisplayName("Should execute, undo and redo ClearWatchlistCommand with full snapshot")
    void shouldExecuteUndoRedoClearCommand() {
        String userId = "USR-001";
        watchlistService.addToWatchlist(userId, "MOV-001");
        watchlistService.addToWatchlist(userId, "MOV-002");
        assertThat(watchlistService.getWatchlist(userId)).hasSize(2);

        ClearWatchlistCommand cmd = new ClearWatchlistCommand(watchlistService, userId);
        undoRedoService.execute(userId, cmd);
        assertThat(watchlistService.getWatchlist(userId)).isEmpty();

        undoRedoService.undo(userId);
        assertThat(watchlistService.getWatchlist(userId)).hasSize(2);
        assertThat(watchlistService.isInWatchlist(userId, "MOV-001")).isTrue();
        assertThat(watchlistService.isInWatchlist(userId, "MOV-002")).isTrue();

        undoRedoService.redo(userId);
        assertThat(watchlistService.getWatchlist(userId)).isEmpty();
    }

    @Test
    @DisplayName("Should clear redo stack when a new command is executed")
    void shouldClearRedoOnNewExecution() {
        String userId = "USR-001";
        undoRedoService.execute(userId,
                new AddToWatchlistCommand(watchlistService, userId, "MOV-001", "Inception"));
        undoRedoService.undo(userId);
        assertThat(undoRedoService.canRedo(userId)).isTrue();

        // New action executed
        undoRedoService.execute(userId,
                new AddToWatchlistCommand(watchlistService, userId, "MOV-002", "Interstellar"));
        assertThat(undoRedoService.canRedo(userId)).isFalse();
    }

    @Test
    @DisplayName("Should evict oldest command when exceeding max stack size")
    void shouldEvictOldestWhenExceedingCapacity() {
        String userId = "USR-001";
        for (int i = 1; i <= 6; i++) {
            // execute 6 distinct movie commands with capacity 5
            WatchlistCommand cmd =
                    new AddToWatchlistCommand(watchlistService, userId, "MOV-00" + i, "Movie " + i);
            undoRedoService.execute(userId, cmd);
        }

        List<String> undoHistory = undoRedoService.getUndoHistory(userId);
        assertThat(undoHistory).hasSize(5);
        assertThat(undoHistory.get(0)).contains("Movie 6");
        assertThat(undoHistory.get(4)).contains("Movie 2");
    }
}
