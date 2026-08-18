package com.moviestreaming.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.moviestreaming.config.AppConstants;
import com.moviestreaming.model.Movie;
import com.moviestreaming.repository.CategoryRepository;
import com.moviestreaming.repository.MovieRepository;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SearchService Full Integration Test Suite")
class SearchServiceTest {

    private SearchService searchService;
    private MovieService movieService;
    private MovieRepository movieRepository;
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        String catPath = Paths.get(AppConstants.DATA_DIR, AppConstants.CATEGORIES_FILE).toString();
        String moviePath = Paths.get(AppConstants.DATA_DIR, AppConstants.MOVIES_FILE).toString();

        categoryRepository = new CategoryRepository(catPath);
        movieRepository = new MovieRepository(moviePath);

        IndexingService indexingService = new IndexingService();
        movieService = new MovieService(movieRepository, categoryRepository, indexingService);
        SortingService sortingService = new SortingService();

        searchService = new SearchService(indexingService, movieRepository, sortingService);
    }

    @Test
    @DisplayName("Should search movies by Title across sample dataset")
    void shouldSearchByTitle() {
        List<Movie> results = searchService.searchByTitle("Inception");
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).isEqualTo("Inception");
    }

    @Test
    @DisplayName("Should search movies by Actor across sample dataset")
    void shouldSearchByActor() {
        List<Movie> results = searchService.searchByActor("DiCaprio");
        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(m -> m.getActors().stream().anyMatch(a -> a.toLowerCase().contains("dicaprio")));
    }

    @Test
    @DisplayName("Should search movies by Director across sample dataset")
    void shouldSearchByDirector() {
        List<Movie> results = searchService.searchByDirector("Nolan");
        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(m -> m.getDirector().toLowerCase().contains("nolan"));
    }

    @Test
    @DisplayName("Should search movies by Genre name and Category ID")
    void shouldSearchByGenre() {
        List<Movie> sciFiMovies = searchService.searchByGenre("Sci-Fi");
        assertThat(sciFiMovies).isNotEmpty();

        List<Movie> cat01Movies = searchService.searchByGenre("CAT-01");
        assertThat(cat01Movies).isNotEmpty();
    }

    @Test
    @DisplayName("Should perform universal keyword search and sort results")
    void shouldSearchUniversalAndSort() {
        List<Movie> sortedNolan = searchService.searchUniversalAndSort("Nolan", SortOption.RATING_DESC);
        assertThat(sortedNolan).isNotEmpty();

        // Check descending rating order
        for (int i = 0; i < sortedNolan.size() - 1; i++) {
            assertThat(sortedNolan.get(i).getRating()).isGreaterThanOrEqualTo(sortedNolan.get(i + 1).getRating());
        }
    }
}
