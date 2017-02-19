package xyz.janficko.movienator.ui.home;

import android.app.Activity;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View.OnClickListener;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

import xyz.janficko.movienator.R;
import xyz.janficko.movienator.objects.Movie;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private static final String TAG = MovieAdapter.class.getSimpleName();

    private Activity mActivity;
    private List<Movie> mMovieList;
    private final ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int movieId);
    }



    public MovieAdapter(Activity activity, List<Movie> result, ListItemClickListener listener) {
        this.mActivity = activity;
        mMovieList = result;
        mOnClickListener = listener;
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = R.layout.item_grid_movies;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(layoutId, parent, false);

        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (mMovieList == null) {
            return 0;
        } else {
            return mMovieList.size();
        }
    }

    public void clear() {
        int size = this.mMovieList.size();
        this.mMovieList.clear();
        notifyItemRangeRemoved(0, size);
    }

    class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        private ImageView mPoster;

        MovieAdapterViewHolder(View itemView) {
            super(itemView);
            mPoster = (ImageView) itemView.findViewById(R.id.poster);
            int screenWidth = getScreenWidth();
            final float scale = mActivity.getResources().getDisplayMetrics().density;

            ViewGroup.LayoutParams params = mPoster.getLayoutParams();

            params.width = (int) (((screenWidth + 10) / getScreenDivider(screenWidth)) * scale + 0.5f);
            params.height = (int) (((((screenWidth + 10) / getScreenDivider(screenWidth)) * 15) / 10) * scale + 0.5f);

            mPoster.setLayoutParams(params);

            itemView.setOnClickListener(this);
        }

        void bind(int position) {
            Movie movie = mMovieList.get(position);

            Picasso.with(mActivity)
                    .load("http://image.tmdb.org/t/p/w342/" + movie.getPosterPath())
                    .into(mPoster);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            Movie movie = mMovieList.get(clickedPosition);
            mOnClickListener.onListItemClick(movie.getId());
        }

        private int getScreenWidth(){
            Display display = mActivity.getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics ();
            display.getMetrics(outMetrics);

            float density  = mActivity.getResources().getDisplayMetrics().density;
            return (int)(outMetrics.widthPixels / density);
        }

        private int getScreenOrientation() {
            Display getOrient = mActivity.getWindowManager().getDefaultDisplay();
            int orientation = Configuration.ORIENTATION_UNDEFINED;
            if(getOrient.getWidth() == getOrient.getHeight()){
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

        private int getScreenDivider(int screenWidth){
            int divider = 2;
            if(screenWidth >= mActivity.getResources().getInteger(R.integer.min_width)){
                divider = 3;
                if(getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE){
                    divider = 4;
                }
            }
            return divider;
        }
    }

}
