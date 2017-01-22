package xyz.janficko.movienator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.janficko.movienator.enums.SortBy;
import xyz.janficko.movienator.objects.Result;

public class MainActivity extends AppCompatActivity implements MovieAdapter.ListItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String API_KEY = BuildConfig.API_KEY;

    private MovieAdapter mMovieAdapter;
    private TheMovieDB tmd = new TheMovieDB();
    private DiscoverInterface discoverInterface = tmd.discoverInterface();

    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadingBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        populateMovie();
    }

    private void populateMovie() {
        mLoadingBar.setVisibility(View.VISIBLE);

        Call<Result> discoverMedia = discoverInterface.discoverMovie(API_KEY, SortBy.VOTE_AVERAGE_DESC, 1000, 1);

        discoverMedia.enqueue(new Callback<Result>() {

            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                final List<JsonElement> movieList = response.body().getResults();
                mMovieAdapter = new MovieAdapter(movieList, MainActivity.this);
                mRecyclerView.setAdapter(mMovieAdapter);

                mLoadingBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Log.e(TAG, "Couldn't fetch movies.");
            }
        });
    }

    @Override
    public void onListClick(int clickedItemIndex) {
        String toastMessage = "Item #" + clickedItemIndex + " clicked.";
        Toast toast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);

        toast.show();
    }
}
