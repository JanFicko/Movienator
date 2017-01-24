package xyz.janficko.movienator;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TheMovieDB {

    private String API_URL = "https://api.themoviedb.org/3/";

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

    public MoviesInterface discoverInterface() {
        return getRetrofit().create(MoviesInterface.class);
    }

}
