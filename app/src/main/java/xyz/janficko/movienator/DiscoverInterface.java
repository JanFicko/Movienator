package xyz.janficko.movienator;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import xyz.janficko.movienator.enums.SortBy;
import xyz.janficko.movienator.objects.Result;

public interface DiscoverInterface {

    @GET("discover/movie")
    Call<Result> discoverMovie(
            @Query("api_key") String API_KEY,
            @Query("sort_by") SortBy sortBy,
            @Query("vote_count.gte") Integer voteCount,
            @Query("page") Integer page
    );

    @GET("discover/tv")
    Call<Result> discoverTV(
            @Query("api_key") String API_KEY,
            @Query("sort_by") SortBy sortBy,
            @Query("vote_count.gte") Integer voteCount,
            @Query("page") Integer page
    );

}
