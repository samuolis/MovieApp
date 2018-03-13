package com.example.android.movieapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Lukas on 2018-03-10.
 */

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.DetailAdapterViewHolder> {

    private final Context mContext;
    public static final String LOG_TAG = DetailAdapter.class.getSimpleName();
    public String[] mReviewData;
    private Cursor mCursor;

    public DetailAdapter(Context context) {
        mContext = context;
    }


    @Override
    public DetailAdapter.DetailAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.detail_list_layout;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = LayoutInflater.from(mContext).inflate(layoutIdForListItem, parent, false);
        return new DetailAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DetailAdapter.DetailAdapterViewHolder holder, int position) {
        holder.bind(position);

    }

    @Override
    public int getItemCount() {

        if (null == mReviewData) return 0;
        return mReviewData.length;
    }

    public class DetailAdapterViewHolder extends RecyclerView.ViewHolder {
        public final TextView mNameTextView;
        public final TextView mReviewTextView;

        public DetailAdapterViewHolder(View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView.findViewById(R.id.reviewer_name);
            mReviewTextView = (TextView) itemView.findViewById(R.id.reviwer_message);
        }

        void bind(int listIndex) {
            String movieReviewData[] = mReviewData[listIndex].split("#");
            if (movieReviewData.length > 1) {
                mNameTextView.setText(movieReviewData[0]);
                mReviewTextView.setText(movieReviewData[1]);
            }
        }

    }

    //    void swapCursor(Cursor newCursor) {
//        mCursor = newCursor;
//        notifyDataSetChanged();
//    }
    public void setReviewData(String[] movieData) {
        mReviewData = movieData;

        notifyDataSetChanged();
    }
}
