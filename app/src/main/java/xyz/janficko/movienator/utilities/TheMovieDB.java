package xyz.janficko.movienator.utilities;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.janficko.movienator.ui.misc.MoviesInterface;

public class TheMovieDB {

    private static final String API_URL = "https://api.themoviedb.org/3/";

    private Retrofit retrofit = null;

    public Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public MoviesInterface movieInterface() {
        return getRetrofit().create(MoviesInterface.class);
    }

}
