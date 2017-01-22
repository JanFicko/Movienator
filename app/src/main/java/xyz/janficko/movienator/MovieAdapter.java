package xyz.janficko.movienator;

import android.util.Log;
import android.view.View.OnClickListener;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.List;

import xyz.janficko.movienator.objects.Movie;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private static final String TAG = MovieAdapter.class.getSimpleName();

    private List<JsonElement> mMovieList;
    final private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListClick(int clickedItemIndex);
    }

    public MovieAdapter(List<JsonElement> result, ListItemClickListener listener) {
        mMovieList = result;
        mOnClickListener = listener;
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int idLayout = R.layout.grid_movie_item;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(idLayout, parent, false);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        private SimpleDraweeView mPoster;

        public MovieAdapterViewHolder(View itemView) {
            super(itemView);

            mPoster = (SimpleDraweeView) itemView.findViewById(R.id.poster);
        }

        void bind(int position) {
            String json = mMovieList.get(position).toString();
            Gson gson = new Gson();
            Movie movie = gson.fromJson(json, Movie.class);
            mPoster.setImageURI("http://image.tmdb.org/t/p/w342/" + movie.getPosterPath());
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListClick(clickedPosition);
            Log.v(TAG, "CLICKED");
        }
    }


}
