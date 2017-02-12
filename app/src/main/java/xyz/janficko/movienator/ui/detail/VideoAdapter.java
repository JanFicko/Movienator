package xyz.janficko.movienator.ui.detail;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;

import xyz.janficko.movienator.R;
import xyz.janficko.movienator.objects.Video;
import xyz.janficko.movienator.ui.home.MovieAdapter;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoAdapterViewHolder>{

    private static final String TAG = VideoAdapter.class.getSimpleName();

    private List<Video> mVideoList;
    private final VideoListItemClickListener mOnClickListener;

    public interface VideoListItemClickListener {
        void onVideoListItemClick(String urlKey, String site);
    }

    public VideoAdapter(List<Video> videoList, VideoListItemClickListener listener) {
        mVideoList = videoList;
        mOnClickListener = listener;
    }

    @Override
    public VideoAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = R.layout.item_video;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(layoutId, parent, false);

        return new VideoAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoAdapterViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (mVideoList == null || mVideoList.size() == 0) {
            return 0;
        } else {
            return mVideoList.size();
        }
    }

    public class VideoAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mVideoTitle;

        public VideoAdapterViewHolder(View itemView) {
            super(itemView);
            mVideoTitle = (TextView) itemView.findViewById(R.id.tv_trailer_title);
            itemView.setOnClickListener(this);
        }

        void bind(int position) {
            Video video = mVideoList.get(position);
            mVideoTitle.setText(video.getName());
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            String key = mVideoList.get(clickedPosition).getKey();
            String site = mVideoList.get(clickedPosition).getSite();
            mOnClickListener.onVideoListItemClick(key, site);
        }
    }
}
