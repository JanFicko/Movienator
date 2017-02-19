package xyz.janficko.movienator.ui.home;

import android.app.Activity;
import android.content.res.Configuration;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import xyz.janficko.movienator.R;
import xyz.janficko.movienator.data.FavouriteContract;

public class FavouriteMovieAdapter extends RecyclerView.Adapter<FavouriteMovieAdapter.FavouriteMovieAdapterViewHolder> {

    private Activity mActivity;
    private Cursor mCursor;
    private final ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int movieId);
    }

    public FavouriteMovieAdapter(Activity activity, Cursor cursor, ListItemClickListener listener){
        this.mActivity = activity;
        this.mCursor = cursor;
        this.mOnClickListener = listener;
    }

    @Override
    public FavouriteMovieAdapter.FavouriteMovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = R.layout.item_grid_movies;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(layoutId, parent, false);

        return new FavouriteMovieAdapter.FavouriteMovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FavouriteMovieAdapter.FavouriteMovieAdapterViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if(mCursor == null){
            return 0;
        } else {
            return mCursor.getCount();
        }
    }

    class FavouriteMovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mPoster;

        FavouriteMovieAdapterViewHolder(View itemView) {
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

        void bind(int position){
            int postPath = mCursor.getColumnIndex(FavouriteContract.FavouriteEntry.COLUMN_POSTER_PATH);
            mCursor.moveToPosition(position);
            Picasso
                    .with(mActivity)
                    .load(mCursor.getString(postPath))
                    .resize(50, 50)
                    .into(mPoster);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();

            int mediaId = mCursor.getColumnIndex(FavouriteContract.FavouriteEntry.COLUMN_MEDIA_ID);
            mCursor.moveToPosition(clickedPosition);

            mOnClickListener.onListItemClick(Integer.valueOf(mCursor.getString(mediaId)));
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
            if(getOrient.getWidth()== getOrient.getHeight()){
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
