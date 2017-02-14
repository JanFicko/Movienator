package xyz.janficko.movienator.ui.home;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;

import xyz.janficko.movienator.R;
import xyz.janficko.movienator.data.FavouriteContract;

public class FavouriteMovieAdapter extends RecyclerView.Adapter<FavouriteMovieAdapter.FavouriteMovieAdapterViewHolder> {

    private Cursor mCursor;
    private final ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int movieId);
    }

    public FavouriteMovieAdapter(Cursor cursor, ListItemClickListener listener){
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

        private SimpleDraweeView mPoster;

        FavouriteMovieAdapterViewHolder(View itemView) {
            super(itemView);
            mPoster = (SimpleDraweeView) itemView.findViewById(R.id.poster);
            itemView.setOnClickListener(this);
        }

        void bind(int position){
            int postPath = mCursor.getColumnIndex(FavouriteContract.FavouriteEntry.COLUMN_POSTER_PATH);
            mCursor.moveToPosition(position);
            mPoster.setImageURI(mCursor.getString(postPath));
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();

            int mediaId = mCursor.getColumnIndex(FavouriteContract.FavouriteEntry.COLUMN_MEDIA_ID);
            mCursor.moveToPosition(clickedPosition);

            mOnClickListener.onListItemClick(Integer.valueOf(mCursor.getString(mediaId)));
        }
    }
}
