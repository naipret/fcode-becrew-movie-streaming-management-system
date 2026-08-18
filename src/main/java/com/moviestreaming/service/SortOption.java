package com.moviestreaming.service;

/**
 * Enumeration of available sorting options for movie listings.
 */
public enum SortOption {
    TITLE_ASC("Title (A-Z)"),
    TITLE_DESC("Title (Z-A)"),
    RATING_DESC("Highest Rated First"),
    RATING_ASC("Lowest Rated First"),
    RELEASE_YEAR_DESC("Newest Release Year First"),
    RELEASE_YEAR_ASC("Oldest Release Year First"),
    POPULARITY_DESC("Most Popular (Views & Favorites)"),
    POPULARITY_ASC("Least Popular"),
    VIEW_COUNT_DESC("Most Viewed"),
    VIEW_COUNT_ASC("Least Viewed"),
    FAVORITE_COUNT_DESC("Most Favorited"),
    FAVORITE_COUNT_ASC("Least Favorited");

    private final String displayName;

    SortOption(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
