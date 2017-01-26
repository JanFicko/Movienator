package xyz.janficko.movienator.enums;

public enum SortMovie {

    POPULAR("popular"),
    TOP_RATED("top_rated"),
    NOW_PLAYING("now_playing");

    private final String sort;

    SortMovie(String sort) {
        this.sort = sort;
    }

    public String getSort() {
        return sort;
    }
}
