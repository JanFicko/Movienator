package xyz.janficko.movienator.ui.home;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.JsonElement;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.janficko.movienator.BuildConfig;
import xyz.janficko.movienator.R;
import xyz.janficko.movienator.enums.SortMovie;
import xyz.janficko.movienator.objects.Result;
import xyz.janficko.movienator.ui.detail.DetailActivity;
import xyz.janficko.movienator.utilities.TheMovieDB;
import xyz.janficko.movienator.utilities.EndlessRecyclerViewScrollListener;

import static xyz.janficko.movienator.enums.SortMovie.NOW_PLAYING;
import static xyz.janficko.movienator.enums.SortMovie.POPULAR;
import static xyz.janficko.movienator.enums.SortMovie.TOP_RATED;

public class MainActivity extends AppCompatActivity implements MovieAdapter.ListItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String API_KEY = BuildConfig.API_KEY;

    private MovieAdapter mMovieAdapter;
    private TheMovieDB mTmd = new TheMovieDB();
    private MoviesInterface mMoviesInterface = mTmd.discoverInterface();

    private RecyclerView mRecyclerView;
    private EndlessRecyclerViewScrollListener mScrollListener;
    private ProgressBar mLoadingBar;
    private List<JsonElement> mMovieList;
    private int mPageCounter = 1;
    private int mTotalPages = 0;
    private SortMovie mSortMovie = POPULAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadingBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mScrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (mPageCounter <= mTotalPages) {
                    populateMovieList(mPageCounter);
                }
            }
        };
        mRecyclerView.addOnScrollListener(mScrollListener);
        populateMovieList(mPageCounter);
    }

    private void populateMovieList(int page) {
        mLoadingBar.setVisibility(View.VISIBLE);
        Call<Result> movieList = null;
        switch (mSortMovie) {
            case POPULAR:
                movieList = mMoviesInterface.getPopular(API_KEY, page);
                break;
            case TOP_RATED:
                movieList = mMoviesInterface.getTopRated(API_KEY, page);
                break;
            case NOW_PLAYING:
                movieList = mMoviesInterface.getNowPlaying(API_KEY, page);
                break;
        }
        movieList.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if (mMovieList == null) {
                    mMovieList = response.body().getResults();
                    mTotalPages = response.body().getTotalPages();
                    mMovieAdapter = new MovieAdapter(mMovieList, MainActivity.this);
                    mRecyclerView.setAdapter(mMovieAdapter);
                } else {
                    mMovieList.addAll(response.body().getResults());
                }
                mMovieAdapter.notifyDataSetChanged();
                mPageCounter++;

                mLoadingBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Log.e(TAG, "Couldn't fetch movies.");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_popular:
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }
                mMovieList.clear();
                mMovieAdapter.notifyDataSetChanged();
                mScrollListener.resetState();
                mPageCounter = 1;
                mSortMovie = POPULAR;
                populateMovieList(mPageCounter);
                return true;
            case R.id.action_top_rated:
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }
                mMovieList.clear();
                mMovieAdapter.notifyDataSetChanged();
                mScrollListener.resetState();
                mPageCounter = 1;
                mSortMovie = TOP_RATED;
                populateMovieList(mPageCounter);
                return true;
            case R.id.action_now_playing:
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }
                mMovieList.clear();
                mMovieAdapter.notifyDataSetChanged();
                mScrollListener.resetState();
                mPageCounter = 1;
                mSortMovie = NOW_PLAYING;
                populateMovieList(mPageCounter);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(int movieId) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, String.valueOf(movieId));
        startActivity(intent);
    }
}
