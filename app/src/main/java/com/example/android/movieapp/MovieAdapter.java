package com.example.android.movieapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.movieapp.utilities.NetworkUtilities;
import com.squareup.picasso.Picasso;

/**
 * Created by Lukas on 2018-03-06.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {
    private final Context mContext;
    public static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    private final MovieAdapterOnClickHandler mClickHandler;

    public interface MovieAdapterOnClickHandler {
        void onClick(int currentMovie);
    }

    private Cursor mCursor;


    public MovieAdapter(Context context, MovieAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }


    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_list_layout;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String moviePosterUrl = mCursor.getString(MainActivity.INDEX_MOVIE_POSTER);
        Picasso.with(mContext).load(NetworkUtilities.buildUrlForImage(moviePosterUrl, "w185").toString()).into(holder.mMovieImage);


    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        public final ImageView mMovieImage;

        public MovieAdapterViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mMovieImage = (ImageView) itemView.findViewById(R.id.movieImage);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int movieId = mCursor.getInt(MainActivity.INDEX_MOVIE_ID);
            mClickHandler.onClick(movieId);
        }
    }

}
