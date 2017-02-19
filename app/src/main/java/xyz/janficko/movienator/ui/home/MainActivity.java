package xyz.janficko.movienator.ui.home;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.janficko.movienator.R;
import xyz.janficko.movienator.data.FavouriteContract;
import xyz.janficko.movienator.enums.SortMovie;
import xyz.janficko.movienator.objects.Movie;
import xyz.janficko.movienator.objects.MovieResult;
import xyz.janficko.movienator.ui.detail.DetailActivity;
import xyz.janficko.movienator.ui.misc.MoviesInterface;
import xyz.janficko.movienator.utilities.NetworkStatusService;
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
    private static final String SORT_STATE = "SORT";

    private TheMovieDB mTmd = new TheMovieDB();
    private MoviesInterface mMoviesInterface = mTmd.movieInterface();
    private NetworkStatusService mNetworkStatusService = new NetworkStatusService(this);

    private MovieAdapter mMovieAdapter;
    private FavouriteMovieAdapter mFavouriteAdapter;
    private RecyclerView mRecyclerViewMovies;
    private GridLayoutManager mGridLayoutManager;
    private EndlessRecyclerViewScrollListener mScrollListener;
    private ProgressBar mLoadingBar;
    private TextView mNoMoviesError;
    private LinearLayout mNoInternetError;
    private List<Movie> mMovieList;
    private int mPageCounter = 1;
    private int mTotalPages = 0;
    private SortMovie mSortMovie = POPULAR;
    private float mDpWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerViewMovies = (RecyclerView) findViewById(R.id.rv_movies);

        mLoadingBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mNoMoviesError = (TextView) findViewById(R.id.tv_no_movies_error);
        mNoInternetError = (LinearLayout) findViewById(R.id.ll_no_internet_error);

        getScreenWidth();

        setLayoutManager();

        if (savedInstanceState != null) {
            mSortMovie = SortMovie.valueOf(savedInstanceState.getString(SORT_STATE));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!mNetworkStatusService.isConnected()){
            onNoInternet();
        } else {
            setRecyclerView();

            populateMovieList(mPageCounter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(SORT_STATE, mSortMovie.toString());
    }

    private void setRecyclerView(){
        mRecyclerViewMovies.setHasFixedSize(true);
        mRecyclerViewMovies.setLayoutManager(mGridLayoutManager);
        mScrollListener = new EndlessRecyclerViewScrollListener(mGridLayoutManager) {
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
        mNoMoviesError.setVisibility(View.INVISIBLE);
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
        if(movieList != null && mSortMovie != FAVOURITES && mNetworkStatusService.isConnected()){
            hideNoInternetError();

            movieList.enqueue(new Callback<MovieResult>() {
                @Override
                public void onResponse(Call<MovieResult> call, Response<MovieResult> response) {
                    if (mMovieList == null) {
                        mMovieList = response.body().getMovie();
                        mTotalPages = response.body().getTotalPages();
                        mMovieAdapter = new MovieAdapter(MainActivity.this, mMovieList, MainActivity.this);
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
        } else if(!mNetworkStatusService.isConnected() && mSortMovie != FAVOURITES){
            mLoadingBar.setVisibility(View.INVISIBLE);
            onNoInternet();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch(mSortMovie){
            case POPULAR:
                MenuItem popular = menu.findItem(R.id.action_popular);
                if (popular.isChecked()) {
                    popular.setChecked(false);
                } else {
                    popular.setChecked(true);
                }
                break;
            case TOP_RATED:
                MenuItem topRated = menu.findItem(R.id.action_top_rated);
                if (topRated.isChecked()) {
                    topRated.setChecked(false);
                } else {
                    topRated.setChecked(true);
                }
                break;
            case NOW_PLAYING:
                MenuItem nowPlaying = menu.findItem(R.id.action_now_playing);
                if (nowPlaying.isChecked()) {
                    nowPlaying.setChecked(false);
                } else {
                    nowPlaying.setChecked(true);
                }
                break;
            case FAVOURITES:
                MenuItem favourites = menu.findItem(R.id.action_favourites);
                if (favourites.isChecked()) {
                    favourites.setChecked(false);
                } else {
                    favourites.setChecked(true);
                }
                break;
        }
        return true;
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
                if(mMovieList != null){
                    mMovieList.clear();
                    mRecyclerViewMovies.setAdapter(mMovieAdapter);
                    mMovieAdapter.notifyDataSetChanged();
                    mScrollListener.resetState();
                    mPageCounter = 1;
                }
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
                if(mMovieList != null){
                    mMovieList.clear();
                    mRecyclerViewMovies.setAdapter(mMovieAdapter);
                    mMovieAdapter.notifyDataSetChanged();
                    mScrollListener.resetState();
                    mPageCounter = 1;
                }
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
                if(mMovieList != null){
                    mMovieList.clear();
                    mRecyclerViewMovies.setAdapter(mMovieAdapter);
                    mMovieAdapter.notifyDataSetChanged();
                    mScrollListener.resetState();
                    mPageCounter = 1;
                }
                mSortMovie = NOW_PLAYING;
                populateMovieList(mPageCounter);
                return true;
            case R.id.action_favourites:
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                }
                if(mMovieList != null) {
                    mMovieList.clear();
                    mMovieAdapter.notifyDataSetChanged();
                    mScrollListener.resetState();
                    mPageCounter = 1;
                }

                setLayoutManager();

                mRecyclerViewMovies.setLayoutManager(mGridLayoutManager);
                mSortMovie = FAVOURITES;
                populateMovieList(mPageCounter);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(int movieId) {
        if(mNetworkStatusService.isConnected()){
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, String.valueOf(movieId));
            startActivity(intent);
        } else {
            Toast.makeText(
                    this,
                    getResources().getString(R.string.error_message_no_internet), Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {

            @Override
            protected void onStartLoading() {
                forceLoad();
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
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mLoadingBar.setVisibility(View.INVISIBLE);
        hideNoInternetError();
        if(data.getCount() != 0 && mSortMovie == FAVOURITES){
            mFavouriteAdapter = new FavouriteMovieAdapter(this, data, this);
            mRecyclerViewMovies.setAdapter(mFavouriteAdapter);
        } else if (data.getCount() == 0 && mSortMovie == FAVOURITES) {
            mNoMoviesError.setVisibility(View.VISIBLE);
        } else if(!mNetworkStatusService.isConnected()){
            onNoInternet();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void setLayoutManager(){
        mGridLayoutManager = new GridLayoutManager(this, 2);
        if(mDpWidth >= getResources().getInteger(R.integer.min_width)){
            mGridLayoutManager = new GridLayoutManager(this, 3);
            if(getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE){
                mGridLayoutManager = new GridLayoutManager(this, 4);
            }
        }
    }

    public void retryConnection(View view){
        if(mNetworkStatusService.isConnected()){
            setRecyclerView();
            populateMovieList(mPageCounter);

            hideNoInternetError();
        }
    }

    private void hideNoInternetError(){
        mNoInternetError.setVisibility(LinearLayout.INVISIBLE);
    }

    private void onNoInternet(){
        mNoInternetError.setVisibility(LinearLayout.VISIBLE);
        if(mMovieAdapter != null){
            mMovieAdapter.clear();
        }
    }

    private void getScreenWidth(){
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        float density  = getResources().getDisplayMetrics().density;
        mDpWidth  = outMetrics.widthPixels / density;
    }

    private int getScreenOrientation() {
        Display getOrient = getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        if(getOrient.getWidth()==getOrient.getHeight()){
            orientation = Configuration.ORIENTATION_SQUARE;
        } else{
            if(getOrient.getWidth() < getOrient.getHeight()){
                orientation = Configuration.ORIENTATION_PORTRAIT;
            }else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

}
