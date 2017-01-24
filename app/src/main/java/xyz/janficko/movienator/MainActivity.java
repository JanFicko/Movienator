package xyz.janficko.movienator;

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
import android.widget.Toast;

import com.google.gson.JsonElement;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.janficko.movienator.enums.SortBy;
import xyz.janficko.movienator.objects.Result;
import xyz.janficko.movienator.utilities.EndlessRecyclerViewScrollListener;

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
    private Toast mToast;
    private int mPageCounter = 1;
    private int mTotalPages = 0;

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

        Call<Result> discoverMedia = mMoviesInterface.getPopular(API_KEY, SortBy.VOTE_AVERAGE_DESC, page);
        discoverMedia.enqueue(new Callback<Result>() {
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
            case R.id.action_sort:
                populateMovieList(mPageCounter);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        if (mToast != null) {
            mToast.cancel();
        }

        String toastMessage = "Item #" + clickedItemIndex + " clicked.";
        Toast toast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);

        toast.show();
    }
}
