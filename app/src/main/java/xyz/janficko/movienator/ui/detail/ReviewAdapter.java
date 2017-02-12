package xyz.janficko.movienator.ui.detail;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import xyz.janficko.movienator.R;
import xyz.janficko.movienator.objects.Review;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {

    private static final String TAG = ReviewAdapter.class.getSimpleName();

    private List<Review> mReviewList;
    private final ReviewListItemClickListener mOnClickListener;

    public interface ReviewListItemClickListener {
        void onReviewListItemClick(String author, String content);
    }

    public ReviewAdapter(List<Review> reviewList, ReviewListItemClickListener listener){
        mReviewList = reviewList;
        mOnClickListener = listener;
    }

    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = R.layout.item_review;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(layoutId, parent, false);

        return new ReviewAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewAdapterViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if(mReviewList == null || mReviewList.size() == 0) {
            return 0;
        } else {
            return mReviewList.size();
        }
    }

    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mAuthorName;

        public ReviewAdapterViewHolder(View itemView) {
            super(itemView);
            mAuthorName = (TextView) itemView.findViewById(R.id.tv_author_name);
            itemView.setOnClickListener(this);
        }

        public void bind(int position){
            Review review = mReviewList.get(position);
            mAuthorName.setText(review.getAuthor());
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            String author = mReviewList.get(clickedPosition).getAuthor();
            String content = mReviewList.get(clickedPosition).getContent();
            mOnClickListener.onReviewListItemClick(author, content);
        }
    }
}
