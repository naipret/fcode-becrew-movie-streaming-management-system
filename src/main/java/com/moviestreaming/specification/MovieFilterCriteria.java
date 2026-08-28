package com.moviestreaming.specification;

/**
 * Filter criteria DTO with builder pattern for multi-condition movie queries.
 */
public class MovieFilterCriteria {

    private String categoryId;
    private Double minRating;
    private Double maxRating;
    private Integer fromYear;
    private Integer toYear;
    private String actorName;
    private String directorName;
    private Integer minDuration;
    private Integer maxDuration;
    private String titleKeyword;

    public MovieFilterCriteria() {
    }

    public String getCategoryId() {
        return categoryId;
    }

    public MovieFilterCriteria setCategoryId(String categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public Double getMinRating() {
        return minRating;
    }

    public MovieFilterCriteria setMinRating(Double minRating) {
        this.minRating = minRating;
        return this;
    }

    public Double getMaxRating() {
        return maxRating;
    }

    public MovieFilterCriteria setMaxRating(Double maxRating) {
        this.maxRating = maxRating;
        return this;
    }

    public Integer getFromYear() {
        return fromYear;
    }

    public MovieFilterCriteria setFromYear(Integer fromYear) {
        this.fromYear = fromYear;
        return this;
    }

    public Integer getToYear() {
        return toYear;
    }

    public MovieFilterCriteria setToYear(Integer toYear) {
        this.toYear = toYear;
        return this;
    }

    public String getActorName() {
        return actorName;
    }

    public MovieFilterCriteria setActorName(String actorName) {
        this.actorName = actorName;
        return this;
    }

    public String getDirectorName() {
        return directorName;
    }

    public MovieFilterCriteria setDirectorName(String directorName) {
        this.directorName = directorName;
        return this;
    }

    public Integer getMinDuration() {
        return minDuration;
    }

    public MovieFilterCriteria setMinDuration(Integer minDuration) {
        this.minDuration = minDuration;
        return this;
    }

    public Integer getMaxDuration() {
        return maxDuration;
    }

    public MovieFilterCriteria setMaxDuration(Integer maxDuration) {
        this.maxDuration = maxDuration;
        return this;
    }

    public String getTitleKeyword() {
        return titleKeyword;
    }

    public MovieFilterCriteria setTitleKeyword(String titleKeyword) {
        this.titleKeyword = titleKeyword;
        return this;
    }

    /**
     * Checks whether all filter criteria are empty/null.
     *
     * @return true if no filtering is requested
     */
    public boolean isEmpty() {
        return categoryId == null && minRating == null && maxRating == null && fromYear == null
                && toYear == null && actorName == null && directorName == null
                && minDuration == null && maxDuration == null && titleKeyword == null;
    }
}
