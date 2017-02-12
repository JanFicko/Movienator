package xyz.janficko.movienator.ui.misc;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import xyz.janficko.movienator.BuildConfig;
import xyz.janficko.movienator.objects.Movie;
import xyz.janficko.movienator.objects.MovieResult;
import xyz.janficko.movienator.objects.Review;
import xyz.janficko.movienator.objects.ReviewResult;
import xyz.janficko.movienator.objects.VideoResult;

public interface MoviesInterface {

    String API_KEY = BuildConfig.API_KEY;

    @GET("movie/popular?api_key=" + API_KEY)
    Call<MovieResult> getPopular(
            @Query("page") Integer page
    );

    @GET("movie/top_rated?api_key=" + API_KEY)
    Call<MovieResult> getTopRated(
            @Query("page") Integer page
    );

    @GET("movie/now_playing?api_key=" + API_KEY)
    Call<MovieResult> getNowPlaying(
            @Query("page") Integer page
    );

    @GET("movie/{movie_id}?api_key=" + API_KEY)
    Call<Movie> getDetails(
            @Path("movie_id") String movieId
    );

    @GET("movie/{movie_id}/videos?api_key=" + API_KEY)
    Call<VideoResult> getVideos(
            @Path("movie_id") String movieId
    );

    @GET("movie/{movie_id}/reviews?api_key=" + API_KEY)
    Call<ReviewResult> getReviews(
            @Path("movie_id") String movieId,
            @Query("page") Integer page
    );

}
