package xyz.janficko.movienator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.janficko.movienator.objects.Movie;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getSimpleName();
    private static final String API_KEY = BuildConfig.API_KEY;

    private TheMovieDB mTmd = new TheMovieDB();
    private MoviesInterface mMoviesInterface = mTmd.discoverInterface();
    private TextView mTitle;
    private SimpleDraweeView mPoster;
    private TextView mReleaseDateTitle;
    private TextView mReleaseDate;
    private TextView mVoteAverageTitle;
    private TextView mVoteAverage;
    private TextView mSynopsisTitle;
    private TextView mSynopsis;
    private ProgressBar mLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mTitle = (TextView) findViewById(R.id.tv_title);
        mReleaseDateTitle = (TextView) findViewById(R.id.tv_release_date_title);
        mReleaseDate = (TextView) findViewById(R.id.tv_release_date);
        mVoteAverageTitle = (TextView) findViewById(R.id.tv_vote_average_title);
        mVoteAverage = (TextView) findViewById(R.id.tv_vote_average);
        mSynopsisTitle = (TextView) findViewById(R.id.tv_synopsis_title);
        mSynopsis = (TextView) findViewById(R.id.tv_synopsis);
        mPoster = (SimpleDraweeView) findViewById(R.id.iv_poster);
        mLoadingBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        Intent intent = getIntent();
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            populateMovie(intent.getStringExtra(Intent.EXTRA_TEXT));
        }
    }

    public void populateMovie(final String movieId) {
        mLoadingBar.setVisibility(View.VISIBLE);

        Call<Movie> movie = mMoviesInterface.getDetails(movieId, API_KEY);
        movie.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                Movie movieDetail = response.body();

                if (movieDetail != null) {
                    mTitle.setText(movieDetail.getTitle());
                    mPoster.setImageURI("http://image.tmdb.org/t/p/w154/" + movieDetail.getPosterPath());
                    mReleaseDate.setText(movieDetail.getReleaseDate());
                    mVoteAverage.setText(String.valueOf(movieDetail.getVoteAverage()));
                    mSynopsis.setText(movieDetail.getOverview());
                }

                mLoadingBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                Log.e(TAG, "Couldn't fetch movie details.");
            }
        });

    }


}
