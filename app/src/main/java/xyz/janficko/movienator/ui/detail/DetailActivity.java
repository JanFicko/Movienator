package xyz.janficko.movienator.ui.detail;

import android.content.ContentValues;
import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.janficko.movienator.R;
import xyz.janficko.movienator.data.FavouriteContract;
import xyz.janficko.movienator.objects.Movie;
import xyz.janficko.movienator.objects.Review;
import xyz.janficko.movienator.objects.ReviewResult;
import xyz.janficko.movienator.objects.Video;
import xyz.janficko.movienator.objects.VideoResult;
import xyz.janficko.movienator.ui.home.MainActivity;
import xyz.janficko.movienator.ui.home.MovieAdapter;
import xyz.janficko.movienator.utilities.EndlessRecyclerViewScrollListener;
import xyz.janficko.movienator.utilities.TheMovieDB;
import xyz.janficko.movienator.ui.misc.MoviesInterface;

public class DetailActivity extends AppCompatActivity implements VideoAdapter.VideoListItemClickListener, ReviewAdapter.ReviewListItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = DetailActivity.class.getSimpleName();
    private static final int TASK_LOADER_ID = 0;

    private TheMovieDB mTmd = new TheMovieDB();
    private MoviesInterface mMoviesInterface = mTmd.movieInterface();

    private String mediaId;
    private TextView mTitle;
    private SimpleDraweeView mPoster;
    private TextView mReleaseDateTitle;
    private TextView mReleaseDate;
    private TextView mVoteAverageTitle;
    private TextView mVoteAverage;
    private TextView mVideosTitle;
    private RecyclerView mRecyclerViewVideos;
    private TextView mSynopsisTitle;
    private TextView mSynopsis;
    private TextView mReviewsTitle;
    private TextView mFavouriteTitle;
    private ImageButton mFavourite;
    private RecyclerView mRecyclerViewReviews;
    private ProgressBar mLoadingBar;
    private VideoAdapter mVideoAdapter;
    private ReviewAdapter mReviewAdapter;
    private PopupWindow mPopupWindow;
    private LinearLayoutManager mReviewsLayoutManager;
    private EndlessRecyclerViewScrollListener mScrollListener;
    private List<Review> mReviewList;
    private int mPageCounter = 1;
    private int mTotalPages = 0;
    private boolean isFavourited = false;

    public static final String[] FAVOURITE_DETAIL_PROJECTION = {
            FavouriteContract.FavouriteEntry.COLUMN_MEDIA_ID
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mTitle = (TextView) findViewById(R.id.tv_title);
        mReleaseDateTitle = (TextView) findViewById(R.id.tv_release_date_title);
        mPoster = (SimpleDraweeView) findViewById(R.id.iv_poster);
        mReleaseDate = (TextView) findViewById(R.id.tv_release_date);
        mVoteAverageTitle = (TextView) findViewById(R.id.tv_vote_average_title);
        mVoteAverage = (TextView) findViewById(R.id.tv_vote_average);
        mFavouriteTitle = (TextView) findViewById(R.id.tv_favourite_title);
        mFavourite = (ImageButton) findViewById(R.id.ib_favourite);
        mSynopsisTitle = (TextView) findViewById(R.id.tv_synopsis_title);
        mSynopsis = (TextView) findViewById(R.id.tv_synopsis);
        mLoadingBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        Intent intent = getIntent();
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            mediaId = intent.getStringExtra(Intent.EXTRA_TEXT);
            populateMovie();
        } else {
            finish();
        }
        getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, this);
    }

    private void populateMovie() {
        mLoadingBar.setVisibility(View.VISIBLE);

        Call<Movie> movie = mMoviesInterface.getDetails(mediaId);
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

                    populateVideos();

                    mReviewsLayoutManager = new LinearLayoutManager(DetailActivity.this);
                    mRecyclerViewReviews = (RecyclerView) findViewById(R.id.rv_reviews);
                    mScrollListener = new EndlessRecyclerViewScrollListener(mReviewsLayoutManager) {
                        @Override
                        public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                            if (mPageCounter <= mTotalPages) {
                                populateReviews(mPageCounter);
                            }
                        }
                    };
                    mRecyclerViewReviews.addOnScrollListener(mScrollListener);

                    populateReviews(mPageCounter);
                }

                mLoadingBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                Log.e(TAG, "Couldn't fetch movie details.");
            }
        });

    }

    private void populateVideos(){

        Call<VideoResult> videos = mMoviesInterface.getVideos(mediaId);
        videos.enqueue(new Callback<VideoResult>() {
            @Override
            public void onResponse(Call<VideoResult> call, Response<VideoResult> response) {
                List<Video> results = response.body().getResults();

                if(results != null && results.size() != 0){
                    mVideosTitle = (TextView) findViewById(R.id.tv_video_title);

                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                            DetailActivity.this,
                            LinearLayoutManager.HORIZONTAL,
                            false
                    );

                    mRecyclerViewVideos = (RecyclerView) findViewById(R.id.rv_videos);
                    mRecyclerViewVideos.setHasFixedSize(true);
                    mRecyclerViewVideos.setLayoutManager(linearLayoutManager);
                    mVideosTitle.setVisibility(View.VISIBLE);
                    mVideoAdapter = new VideoAdapter(results, DetailActivity.this);
                    mRecyclerViewVideos.setAdapter(mVideoAdapter);
                    mVideoAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<VideoResult> call, Throwable t) {
                Log.e(TAG, "Couldn't fetch videos.");
            }
        });
    }

    private void populateReviews(int page){
        Call<ReviewResult> reviews = mMoviesInterface.getReviews(mediaId, page);
        reviews.enqueue(new Callback<ReviewResult>() {
            @Override
            public void onResponse(Call<ReviewResult> call, Response<ReviewResult> response) {

                    if (mReviewList == null) {
                        mReviewList = response.body().getReviews();
                        if(mReviewList != null && mReviewList.size() != 0) {
                            mReviewsTitle = (TextView) findViewById(R.id.tv_reviews_title);
                            mRecyclerViewReviews = (RecyclerView) findViewById(R.id.rv_reviews);
                            mTotalPages = response.body().getTotalPages();
                            mRecyclerViewReviews.setHasFixedSize(true);
                            mRecyclerViewReviews.setLayoutManager(mReviewsLayoutManager);
                            mReviewsTitle.setVisibility(View.VISIBLE);
                            mReviewAdapter = new ReviewAdapter(mReviewList, DetailActivity.this);
                            mRecyclerViewReviews.setAdapter(mReviewAdapter);
                        }
                    } else {
                        mReviewList.addAll(response.body().getReviews());
                        mReviewAdapter.notifyDataSetChanged();
                    }

                    mPageCounter++;
            }

            @Override
            public void onFailure(Call<ReviewResult> call, Throwable t) {
                Log.e(TAG, "Couldn't fetch reviews.");
            }
        });
    }

    @Override
    public void onVideoListItemClick(String urlKey, String site) {
        if(site.equalsIgnoreCase("YouTube")){
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v="+urlKey));
            startActivity(intent);
        } else {
            Toast.makeText(this, getResources().getString(R.string.error_message_site_not_supported), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onReviewListItemClick(String author, String content) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x - (int) Math.round(size.x * 0.2);
        int height = size.y - (int) Math.round(size.y  * 0.2);

        LayoutInflater inflater = (LayoutInflater) DetailActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup_review,
                (ViewGroup) findViewById(R.id.popup_element));

        TextView tvAuthor = (TextView) layout.findViewById(R.id.tv_author);
        TextView tvContent = (TextView) layout.findViewById(R.id.tv_content);
        tvAuthor.setText(author);
        tvContent.setText(content);

        mPopupWindow = new PopupWindow(layout, width, height, true);
        mPopupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);
    }

    public void onFavouriteClicked(View view){
        if(mediaId == null){
            return;
        }

        Uri uri = null;

        if(!isFavourited){
            ContentValues contentValues = new ContentValues();
            contentValues.put(FavouriteContract.FavouriteEntry.COLUMN_MEDIA_ID, mediaId);

            try {
                uri = getContentResolver().insert(
                        FavouriteContract.FavouriteEntry.CONTENT_URI,
                        contentValues
                );
            } catch (Exception e){
                Log.e(TAG, "This media is already present in the database.");
            }

            if(uri != null){
                setFavourite();
                Log.v(TAG, uri.toString());
            }
        } else {
            uri = FavouriteContract.FavouriteEntry.CONTENT_URI;
            uri = uri.buildUpon().appendPath(mediaId).build();

            int rowsDeleted = getContentResolver().delete(uri, null, null);

            if(rowsDeleted != 0){
                setUnfavourite();
                Log.v(TAG, String.valueOf(rowsDeleted));
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {

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
                    Uri uri = FavouriteContract.FavouriteEntry.CONTENT_URI;
                    uri = uri.buildUpon().appendPath(mediaId).build();
                    /*return getContentResolver().query(
                            FavouriteContract.FavouriteEntry.CONTENT_URI,
                            FAVOURITE_DETAIL_PROJECTION,
                            FavouriteContract.FavouriteEntry.COLUMN_MEDIA_ID + " = ?",
                            new String[] { mediaId },
                            null
                    );*/
                    return getContentResolver().query(
                            uri,
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
        //int idIndex = data.getColumnIndex(FavouriteContract.FavouriteEntry._ID);
        //data.moveToFirst();
        //Log.v(TAG, String.valueOf(data.getInt(idIndex)));
        Log.v(TAG, String.valueOf(data.getCount()));
        if(data.getCount() == 1){
            setFavourite();
        } else{
            setUnfavourite();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void setFavourite(){
        isFavourited = true;
        mFavourite.setBackground(ContextCompat.getDrawable(DetailActivity.this, R.drawable.ic_favourite));
    }
    private void setUnfavourite(){
        isFavourited = false;
        mFavourite.setBackground(ContextCompat.getDrawable(DetailActivity.this, R.drawable.ic_unfavourite));
    }
}
