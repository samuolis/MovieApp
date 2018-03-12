package com.example.android.movieapp;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.movieapp.data.MovieContract.MovieEntry;
import com.example.android.movieapp.utilities.MoviesJsonUtils;
import com.example.android.movieapp.utilities.NetworkUtilities;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private TextView mMovieTitle;
    private TextView mMovieReleaseDate;
    private ImageView mMoviePoster;
    private TextView mMovieVotes;
    private TextView mMovieSynopsis;
    private Button mReviewButton;
    private TextView mMovieCommentsLabel;
    private RecyclerView mMovieCommentsContent;
    public static final String LOG_TAG = DetailActivity.class.getSimpleName();
    public static Boolean asyncDone;

    public static final String[] MOVIE_DETAIL_PROJECTION = {
            MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_MOVIE_TITLE,
            MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
            MovieEntry.COLUMN_MOVIE_POSTER_URL,
            MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE,
            MovieEntry.COLUMN_MOVIE_OVERVIEW,
            MovieEntry.COLUMN_MOVIE_TRAILER_URL,
            MovieEntry.COLUMN_MOVIE_REVIEW_STRING
    };

    public static final int INDEX_MOVIE_ID = 0;
    public static final int INDEX_MOVIE_TITLE = 1;
    public static final int INDEX_MOVIE_RELEASE_DATE = 2;
    public static final int INDEX_MOVIE_POSTER_URI = 3;
    public static final int INDEX_MOVIE_VOTE_AVERAGE = 4;
    public static final int INDEX_MOVIE_OVERVIEW = 5;
    public static final int INDEX_MOVIE_TRAILER_URL = 6;
    public static final int INDEX_MOVIE_REVIEW_STRING = 7;
    private  int mMovieID;

    private Uri mMovieUrlID;
    private static final int ID_DETAIL_LOADER = 353;
    private static final int ID_LOADER_LOADER_REVIEWS=373;
    String youtubeTrailerSOURCE;
    private RecyclerView mDetailRecyclerView;
    private DetailAdapter mDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mMovieTitle = (TextView) findViewById(R.id.movie_title);
        mMovieReleaseDate = (TextView) findViewById(R.id.movie_release_date);
        mMoviePoster = (ImageView) findViewById(R.id.movie_poster);
        mMovieVotes = (TextView) findViewById(R.id.movie_vote_average);
        mMovieSynopsis = (TextView) findViewById(R.id.movie_plot_synopsis);
        mReviewButton=(Button) findViewById(R.id.review_button);
        mMovieCommentsLabel=(TextView) findViewById(R.id.comments_label);
        mMovieCommentsContent=(RecyclerView) findViewById(R.id.reviews_recycler_view);
        Intent intentThatStartedThisActivity = getIntent();

        mMovieUrlID = intentThatStartedThisActivity.getData();
        mMovieID=intentThatStartedThisActivity.getIntExtra("id",0);

        mReviewButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + youtubeTrailerSOURCE));
                Intent webIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.youtube.com/watch?v=" + youtubeTrailerSOURCE));
                try {
                    DetailActivity.this.startActivity(appIntent);
                } catch (ActivityNotFoundException ex) {
                    DetailActivity.this.startActivity(webIntent);
                }
            }
        });

        mDetailRecyclerView = (RecyclerView) findViewById(R.id.reviews_recycler_view);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mDetailRecyclerView.setLayoutManager(layoutManager);

        mDetailAdapter = new DetailAdapter(this);
        mDetailRecyclerView.setAdapter(mDetailAdapter);








        if (mMovieUrlID==null) throw new NullPointerException("URI FOR DETAILACTIVITY cannot be null");
        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);


    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        AsyncTask<Void, Void, Void> mFetchMovieTask;

        mFetchMovieTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                asyncDone=false;
            }

            ContentValues mMoviesTrailersAndReviewsValues;

            @Override
            protected Void doInBackground(Void... voids) {
                try {

                    mMoviesTrailersAndReviewsValues= MoviesJsonUtils.
                            getSimpleMovieContentValuesFromJsonForTrailers(getApplicationContext(),Integer.toString(mMovieID));


                    if (mMoviesTrailersAndReviewsValues != null) {
                        ContentResolver movieContentResolver = getApplicationContext().getContentResolver();
                        String [] selectionArgsForUpdate = new String[]{Integer.toString(mMovieID)};


                        int rows=movieContentResolver.update(
                                mMovieUrlID,
                                mMoviesTrailersAndReviewsValues,
                                MovieEntry.COLUMN_MOVIE_ID+"=?",
                                selectionArgsForUpdate);

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                asyncDone=true;
            }
        };

        mFetchMovieTask.execute();






        switch (id) {


            case ID_DETAIL_LOADER: {

                Log.i(LOG_TAG, "boolean !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! : " + asyncDone);

                return new CursorLoader(this,
                            mMovieUrlID,
                            MOVIE_DETAIL_PROJECTION,
                            null,
                            null,
                            null);

            }



            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }



    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {



        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            return;
        }
        mMovieTitle.setText(data.getString(INDEX_MOVIE_TITLE));
        mMovieReleaseDate.setText(data.getString(INDEX_MOVIE_RELEASE_DATE));
        Picasso.with(getApplicationContext()).load(NetworkUtilities.
                buildUrlForImage(data.getString(INDEX_MOVIE_POSTER_URI), "w500").toString()).into(mMoviePoster);
        mMovieVotes.setText(data.getString(INDEX_MOVIE_VOTE_AVERAGE));
        mMovieSynopsis.setText(data.getString(INDEX_MOVIE_OVERVIEW));
        youtubeTrailerSOURCE=data.getString(INDEX_MOVIE_TRAILER_URL);
        //Log.i(LOG_TAG, "Got String : " +data.getString(INDEX_MOVIE_REVIEW_STRING));


        //TODO when i uncoment this line above Log getting null value, but when i comment it everything is fine
        if (data.getString(INDEX_MOVIE_REVIEW_STRING)!=null&&!data.getString(INDEX_MOVIE_REVIEW_STRING).equals("")) {
            mMovieCommentsLabel.setVisibility(View.VISIBLE);
            mMovieCommentsContent.setVisibility(View.VISIBLE);
            String[] movieReviewStringArray = MoviesJsonUtils.convertStringToArray(data.getString(INDEX_MOVIE_REVIEW_STRING), MoviesJsonUtils.separator1);

            Log.i(LOG_TAG, "AS CIAAAAAAAAAAAAAAAAAAAA !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            mDetailAdapter.setReviewData(movieReviewStringArray);
        } else {
            mMovieCommentsLabel.setVisibility(View.GONE);
            mMovieCommentsContent.setVisibility(View.GONE);
        }







    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}