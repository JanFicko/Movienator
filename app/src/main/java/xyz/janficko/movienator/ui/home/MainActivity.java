package xyz.janficko.movienator.ui.home;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
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
import android.widget.TextView;


import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.janficko.movienator.BuildConfig;
import xyz.janficko.movienator.R;
import xyz.janficko.movienator.data.FavouriteContract;
import xyz.janficko.movienator.enums.SortMovie;
import xyz.janficko.movienator.objects.Movie;
import xyz.janficko.movienator.objects.MovieResult;
import xyz.janficko.movienator.ui.detail.DetailActivity;
import xyz.janficko.movienator.ui.misc.MoviesInterface;
import xyz.janficko.movienator.utilities.TheMovieDB;
import xyz.janficko.movienator.utilities.EndlessRecyclerViewScrollListener;

import static xyz.janficko.movienator.enums.SortMovie.FAVOURITES;
import static xyz.janficko.movienator.enums.SortMovie.NOW_PLAYING;
import static xyz.janficko.movienator.enums.SortMovie.POPULAR;
import static xyz.janficko.movienator.enums.SortMovie.TOP_RATED;

public class MainActivity extends AppCompatActivity implements MovieAdapter.ListItemClickListener,
        FavouriteMovieAdapter.ListItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = MainActivity.class.getSimpleName();

    private TheMovieDB mTmd = new TheMovieDB();
    private MoviesInterface mMoviesInterface = mTmd.movieInterface();

    private MovieAdapter mMovieAdapter;
    private FavouriteMovieAdapter mFavouriteAdapter;
    private RecyclerView mRecyclerViewMovies;
    private EndlessRecyclerViewScrollListener mScrollListener;
    private ProgressBar mLoadingBar;
    private TextView mNoMoviesError;
    private List<Movie> mMovieList;
    private int mPageCounter = 1;
    private int mTotalPages = 0;
    private SortMovie mSortMovie = POPULAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadingBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mNoMoviesError = (TextView) findViewById(R.id.tv_no_movies_error);

        setRecyclerView();
        populateMovieList(mPageCounter);
    }

    private void setRecyclerView(){
        mNoMoviesError.setVisibility(View.GONE);
        mRecyclerViewMovies = (RecyclerView) findViewById(R.id.rv_movies);

        mRecyclerViewMovies.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerViewMovies.setLayoutManager(gridLayoutManager);
        mScrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (mPageCounter <= mTotalPages) {
                    populateMovieList(mPageCounter);
                }
            }
        };
        mRecyclerViewMovies.addOnScrollListener(mScrollListener);
    }

    private void populateMovieList(int page) {
        mLoadingBar.setVisibility(View.VISIBLE);
        Call<MovieResult> movieList = null;
        switch (mSortMovie) {
            case POPULAR:
                movieList = mMoviesInterface.getPopular(page);
                break;
            case TOP_RATED:
                movieList = mMoviesInterface.getTopRated(page);
                break;
            case NOW_PLAYING:
                movieList = mMoviesInterface.getNowPlaying(page);
                break;
            case FAVOURITES:
                getSupportLoaderManager().initLoader(0, null, MainActivity.this);
                break;
            default:
                throw new UnsupportedOperationException("Selected sorting option doesn't exist: " + mSortMovie);
        }
        if(movieList != null && mSortMovie != SortMovie.FAVOURITES){
            movieList.enqueue(new Callback<MovieResult>() {
                @Override
                public void onResponse(Call<MovieResult> call, Response<MovieResult> response) {
                    if (mMovieList == null) {
                        mMovieList = response.body().getMovie();
                        mTotalPages = response.body().getTotalPages();
                        mMovieAdapter = new MovieAdapter(mMovieList, MainActivity.this);
                        mRecyclerViewMovies.setAdapter(mMovieAdapter);
                    } else {
                        mMovieList.addAll(response.body().getMovie());
                    }
                    mMovieAdapter.notifyDataSetChanged();
                    mPageCounter++;

                    mLoadingBar.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onFailure(Call<MovieResult> call, Throwable t) {
                    Log.e(TAG, "Couldn't fetch movies: " + t.getMessage());
                }
            });
        }
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
                setRecyclerView();
                mRecyclerViewMovies.setAdapter(mMovieAdapter);
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
                setRecyclerView();
                mRecyclerViewMovies.addOnScrollListener(mScrollListener);
                mRecyclerViewMovies.setAdapter(mMovieAdapter);
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
                setRecyclerView();
                mRecyclerViewMovies.addOnScrollListener(mScrollListener);
                mRecyclerViewMovies.setAdapter(mMovieAdapter);
                mMovieList.clear();
                mMovieAdapter.notifyDataSetChanged();
                mScrollListener.resetState();
                mPageCounter = 1;
                mSortMovie = NOW_PLAYING;
                populateMovieList(mPageCounter);
                return true;
            case R.id.action_favourites:
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }
                mMovieList.clear();
                mMovieAdapter.notifyDataSetChanged();
                mScrollListener.resetState();
                mPageCounter = 1;

                mRecyclerViewMovies = null;
                mRecyclerViewMovies = (RecyclerView) findViewById(R.id.rv_movies);
                mRecyclerViewMovies.setHasFixedSize(true);
                mRecyclerViewMovies.setLayoutManager(new GridLayoutManager(this, 2));
                mSortMovie = FAVOURITES;
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {

            Cursor mFavouriteData = null;

            @Override
            protected void onStartLoading() {
                if(mFavouriteData != null){
                    deliverResult(mFavouriteData);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {
                try {
                    return getContentResolver().query(
                            FavouriteContract.FavouriteEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null
                    );
                }catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(Cursor data) {
                mFavouriteData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(data.getCount() != 0){
            mFavouriteAdapter = new FavouriteMovieAdapter(data, this);
            mRecyclerViewMovies.setAdapter(mFavouriteAdapter);
            mFavouriteAdapter.notifyDataSetChanged();
        } else {
            mNoMoviesError.setVisibility(View.VISIBLE);
        }


        mLoadingBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {


    }

}
