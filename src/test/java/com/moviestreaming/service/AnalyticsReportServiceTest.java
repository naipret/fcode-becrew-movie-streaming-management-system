package com.moviestreaming.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.moviestreaming.repository.CategoryRepository;
import com.moviestreaming.repository.MovieRepository;
import com.moviestreaming.repository.UserRepository;
import com.moviestreaming.repository.WatchHistoryRepository;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Analytics & Report Service Test Suite")
class AnalyticsReportServiceTest {

    private Path tempDir;
    private AnalyticsReportService analyticsReportService;
    private WatchHistoryRepository historyRepository;
    private MovieRepository movieRepository;
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("analytics_test");

        String catPath = tempDir.resolve("categories.csv").toString();
        String movPath = tempDir.resolve("movies.csv").toString();
        String usrPath = tempDir.resolve("users.csv").toString();
        String hisPath = tempDir.resolve("history.csv").toString();

        List<String> catLines = Arrays.asList("id|name|description",
                "CAT-01|Sci-Fi|Science fiction", "CAT-02|Action|High octane action");
        Files.write(tempDir.resolve("categories.csv"), catLines);

        List<String> movLines = Arrays.asList(
                "id|title|categoryId|director|actors|releaseYear|durationMinutes|rating|viewCount|favoriteCount|synopsis",
                "MOV-001|Inception|CAT-01|Christopher Nolan|Leonardo DiCaprio|2010|148|8.8|1000|500|Dream heist.",
                "MOV-002|The Dark Knight|CAT-02|Christopher Nolan|Christian Bale|2008|152|9.0|2000|800|Batman.");
        Files.write(tempDir.resolve("movies.csv"), movLines);

        List<String> usrLines =
                Arrays.asList("id|username|passwordHash|fullName|email|role|createdAt",
                        "USR-001|alice|hash1|Alice Smith|alice@test.com|USER|2026-01-01T00:00:00");
        Files.write(tempDir.resolve("users.csv"), usrLines);

        // Recent history: 2 days ago (CAT-01), 4 days ago (CAT-01), 15 days ago (CAT-02)
        LocalDateTime now = LocalDateTime.now();
        List<String> hisLines = Arrays.asList(
                "id|userId|movieId|watchedDurationMinutes|totalDurationMinutes|lastWatchedTimestamp|isCompleted",
                "HIS-001|USR-001|MOV-001|148|148|" + now.minusDays(2).toString() + "|true",
                "HIS-002|USR-001|MOV-001|60|148|" + now.minusDays(4).toString() + "|false",
                "HIS-003|USR-001|MOV-002|152|152|" + now.minusDays(15).toString() + "|true");
        Files.write(tempDir.resolve("history.csv"), hisLines);

        categoryRepository = new CategoryRepository(catPath);
        movieRepository = new MovieRepository(movPath);
        userRepository = new UserRepository(usrPath);
        historyRepository = new WatchHistoryRepository(hisPath);

        analyticsReportService = new AnalyticsReportService(historyRepository, movieRepository,
                categoryRepository, userRepository);
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
    @DisplayName("Should correctly filter trending categories within sliding 7-day window")
    void shouldGetTrendingCategoriesIn7Days() {
        List<AnalyticsReportService.TrendingCategory> trending =
                analyticsReportService.getTrendingCategories(7, 5);
        assertThat(trending).hasSize(1);
        assertThat(trending.get(0).getCategory().getId()).isEqualTo("CAT-01");
        assertThat(trending.get(0).getWatchCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should include older entries when sliding window is expanded to 30 days")
    void shouldGetTrendingCategoriesIn30Days() {
        List<AnalyticsReportService.TrendingCategory> trending =
                analyticsReportService.getTrendingCategories(30, 5);
        assertThat(trending).hasSize(2);
        assertThat(trending.get(0).getCategory().getId()).isEqualTo("CAT-01");
        assertThat(trending.get(0).getWatchCount()).isEqualTo(2L);
        assertThat(trending.get(1).getCategory().getId()).isEqualTo("CAT-02");
        assertThat(trending.get(1).getWatchCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should compute user viewing statistics")
    void shouldComputeUserViewingStats() {
        AnalyticsReportService.UserViewingStats stats =
                analyticsReportService.getUserViewingStats("USR-001");
        assertThat(stats.getUserId()).isEqualTo("USR-001");
        assertThat(stats.getUsername()).isEqualTo("alice");
        assertThat(stats.getTotalWatched()).isEqualTo(3);
        assertThat(stats.getTotalCompleted()).isEqualTo(2);
        assertThat(stats.getTotalMinutesWatched()).isEqualTo(148 + 60 + 152);
        assertThat(stats.getCompletionRate()).isGreaterThan(60.0);
        assertThat(stats.getTopCategoryName()).isEqualTo("Sci-Fi");
    }

    @Test
    @DisplayName("Should compute system overview metrics")
    void shouldComputeSystemOverviewStats() {
        AnalyticsReportService.SystemStats stats = analyticsReportService.getSystemOverviewStats();
        assertThat(stats.getTotalMovies()).isEqualTo(2);
        assertThat(stats.getTotalCategories()).isEqualTo(2);
        assertThat(stats.getTotalUsers()).isEqualTo(1);
        assertThat(stats.getTotalSessions()).isEqualTo(3);
        assertThat(stats.getTotalWatchMinutes()).isEqualTo(148 + 60 + 152);
        assertThat(stats.getAverageRating()).isEqualTo(8.9);
    }

    @Test
    @DisplayName("Should export user viewing report to text and CSV files")
    void shouldExportUserReports() throws IOException {
        String reportsDir = tempDir.resolve("reports").toString();

        String txtPath = analyticsReportService.exportUserViewingReport("USR-001", reportsDir);
        File txtFile = new File(txtPath);
        assertThat(txtFile).exists();
        String txtContent = new String(Files.readAllBytes(txtFile.toPath()));
        assertThat(txtContent).contains("NETFLIX CLI - USER VIEWING ANALYTICS REPORT");
        assertThat(txtContent).contains("USR-001");
        assertThat(txtContent).contains("Inception");

        String csvPath = analyticsReportService.exportUserViewingReportCsv("USR-001", reportsDir);
        File csvFile = new File(csvPath);
        assertThat(csvFile).exists();
        List<String> csvLines = Files.readAllLines(csvFile.toPath());
        assertThat(csvLines.get(0)).contains("historyId|movieId|title|category");
        assertThat(csvLines.size()).isGreaterThanOrEqualTo(4);
    }
}
