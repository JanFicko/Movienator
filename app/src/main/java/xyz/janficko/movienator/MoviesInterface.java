package xyz.janficko.movienator;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import xyz.janficko.movienator.enums.SortBy;
import xyz.janficko.movienator.objects.Result;

public interface MoviesInterface {

    @GET("movie/popular")
    Call<Result> getPopular(
            @Query("api_key") String API_KEY,
            @Query("sort_by") SortBy sortBy,
            @Query("page") Integer page
    );

    @GET("movie/top_rated")
    Call<Result> getTopRated(
            @Query("api_key") String API_KEY,
            @Query("sort_by") SortBy sortBy,
            @Query("page") Integer page
    );

    @GET("movie/{movie_id}")
    Call<Result> getDetails(
            @Query("api_key") String API_KEY,
            @Path("movie_id") int movieId
    );

}
