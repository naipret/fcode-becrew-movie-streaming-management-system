package com.moviestreaming.specification;

import com.moviestreaming.model.Movie;
import java.util.function.Predicate;

/**
 * Factory class generating composable Java 8 {@link Predicate} specifications for Movie entities.
 */
public final class MovieSpecifications {

    private MovieSpecifications() {
        // Utility class constructor
    }

    /**
     * Specification checking whether the movie belongs to the given category ID.
     *
     * @param categoryId category ID
     * @return predicate
     */
    public static Predicate<Movie> hasCategory(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return movie -> true;
        }
        String cleanId = categoryId.trim();
        return movie -> movie != null && movie.getCategoryId().equalsIgnoreCase(cleanId);
    }

    /**
     * Specification checking whether the movie rating is greater than or equal to minRating.
     *
     * @param minRating minimum rating
     * @return predicate
     */
    public static Predicate<Movie> ratingGreaterThanOrEqual(Double minRating) {
        if (minRating == null) {
            return movie -> true;
        }
        return movie -> movie != null && movie.getRating() >= minRating;
    }

    /**
     * Specification checking whether the movie rating is less than or equal to maxRating.
     *
     * @param maxRating maximum rating
     * @return predicate
     */
    public static Predicate<Movie> ratingLessThanOrEqual(Double maxRating) {
        if (maxRating == null) {
            return movie -> true;
        }
        return movie -> movie != null && movie.getRating() <= maxRating;
    }

    /**
     * Specification checking whether the movie release year is in the given range [fromYear,
     * toYear].
     *
     * @param fromYear starting release year (inclusive)
     * @param toYear ending release year (inclusive)
     * @return predicate
     */
    public static Predicate<Movie> releaseYearBetween(Integer fromYear, Integer toYear) {
        return movie -> {
            if (movie == null) {
                return false;
            }
            if (fromYear != null && movie.getReleaseYear() < fromYear) {
                return false;
            }
            return toYear == null || movie.getReleaseYear() <= toYear;
        };
    }

    /**
     * Specification checking whether the movie contains the specified actor name.
     *
     * @param actorName actor name substring
     * @return predicate
     */
    public static Predicate<Movie> containsActor(String actorName) {
        if (actorName == null || actorName.trim().isEmpty()) {
            return movie -> true;
        }
        String needle = actorName.trim().toLowerCase();
        return movie -> {
            if (movie == null || movie.getActors() == null) {
                return false;
            }
            for (String actor : movie.getActors()) {
                if (actor.toLowerCase().contains(needle)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Specification checking whether the movie director contains the specified name substring.
     *
     * @param directorName director name substring
     * @return predicate
     */
    public static Predicate<Movie> containsDirector(String directorName) {
        if (directorName == null || directorName.trim().isEmpty()) {
            return movie -> true;
        }
        String needle = directorName.trim().toLowerCase();
        return movie -> movie != null && movie.getDirector() != null
                && movie.getDirector().toLowerCase().contains(needle);
    }

    /**
     * Specification checking whether the movie duration is within [minDuration, maxDuration].
     *
     * @param minDuration minimum duration in minutes
     * @param maxDuration maximum duration in minutes
     * @return predicate
     */
    public static Predicate<Movie> durationBetween(Integer minDuration, Integer maxDuration) {
        return movie -> {
            if (movie == null) {
                return false;
            }
            if (minDuration != null && movie.getDurationMinutes() < minDuration) {
                return false;
            }
            return maxDuration == null || movie.getDurationMinutes() <= maxDuration;
        };
    }

    /**
     * Specification checking whether the movie title contains the given keyword.
     *
     * @param keyword keyword substring
     * @return predicate
     */
    public static Predicate<Movie> containsTitle(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return movie -> true;
        }
        String needle = keyword.trim().toLowerCase();
        return movie -> movie != null && movie.getTitle() != null
                && movie.getTitle().toLowerCase().contains(needle);
    }

    /**
     * Combines all non-null conditions from {@link MovieFilterCriteria} into a single composite
     * {@link Predicate}.
     *
     * @param criteria filter criteria container
     * @return chained predicate specification
     */
    public static Predicate<Movie> fromCriteria(MovieFilterCriteria criteria) {
        if (criteria == null || criteria.isEmpty()) {
            return movie -> true;
        }

        Predicate<Movie> predicate = movie -> movie != null;

        if (criteria.getCategoryId() != null && !criteria.getCategoryId().trim().isEmpty()) {
            predicate = predicate.and(hasCategory(criteria.getCategoryId()));
        }
        if (criteria.getMinRating() != null) {
            predicate = predicate.and(ratingGreaterThanOrEqual(criteria.getMinRating()));
        }
        if (criteria.getMaxRating() != null) {
            predicate = predicate.and(ratingLessThanOrEqual(criteria.getMaxRating()));
        }
        if (criteria.getFromYear() != null || criteria.getToYear() != null) {
            predicate =
                    predicate.and(releaseYearBetween(criteria.getFromYear(), criteria.getToYear()));
        }
        if (criteria.getActorName() != null && !criteria.getActorName().trim().isEmpty()) {
            predicate = predicate.and(containsActor(criteria.getActorName()));
        }
        if (criteria.getDirectorName() != null && !criteria.getDirectorName().trim().isEmpty()) {
            predicate = predicate.and(containsDirector(criteria.getDirectorName()));
        }
        if (criteria.getMinDuration() != null || criteria.getMaxDuration() != null) {
            predicate = predicate
                    .and(durationBetween(criteria.getMinDuration(), criteria.getMaxDuration()));
        }
        if (criteria.getTitleKeyword() != null && !criteria.getTitleKeyword().trim().isEmpty()) {
            predicate = predicate.and(containsTitle(criteria.getTitleKeyword()));
        }

        return predicate;
    }
}
