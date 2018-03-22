package com.example.android.movieapp;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
        LoaderManager.LoaderCallbacks<Cursor> {

    private TextView mMovieTitle;
    private TextView mMovieReleaseDate;
    private ImageView mMoviePoster;
    private TextView mMovieVotes;
    private TextView mMovieSynopsis;
    private Button mReviewButton;
    private TextView mMovieCommentsLabel;
    private RecyclerView mMovieCommentsContent;
    private ImageView mMoviefavoriteStar;
    public static final String LOG_TAG = DetailActivity.class.getSimpleName();
    public static Boolean asyncDone;
    private String youtubeTrailerSOURCE;
    private RecyclerView mDetailRecyclerView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private DetailAdapter mDetailAdapter;
    private String moviePosterUrl;
    private SharedPreferences sharedPreferences;
    private String orderValue;
    Toolbar toolbar;
    private ImageView backdrop;
    private String backdropPath;
    private static final String BUNDLE_RECYCLER_LAYOUT = "detailActivity.recycler.layout";
    private Parcelable mListState;

    private boolean favoritePressed = false;

    public static final String[] MOVIE_DETAIL_PROJECTION = {
            MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_MOVIE_TITLE,
            MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
            MovieEntry.COLUMN_MOVIE_POSTER_URL,
            MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE,
            MovieEntry.COLUMN_MOVIE_OVERVIEW,
            MovieEntry.COLUMN_MOVIE_TRAILER_URL,
            MovieEntry.COLUMN_MOVIE_REVIEW_STRING,
            MovieEntry.COLUMN_MOVIE_BACKDROP_PATH
    };

    public static final int INDEX_MOVIE_ID = 0;
    public static final int INDEX_MOVIE_TITLE = 1;
    public static final int INDEX_MOVIE_RELEASE_DATE = 2;
    public static final int INDEX_MOVIE_POSTER_URI = 3;
    public static final int INDEX_MOVIE_VOTE_AVERAGE = 4;
    public static final int INDEX_MOVIE_OVERVIEW = 5;
    public static final int INDEX_MOVIE_TRAILER_URL = 6;
    public static final int INDEX_MOVIE_REVIEW_STRING = 7;
    public static final int INDEX_MOVIE_BACKDROP_PATH = 8;
    private int mMovieID;
    private LinearLayoutManager layoutManager;

    private Uri mMovieUrlID;
    private static final int ID_DETAIL_LOADER = 353;
    private static final int ID_LOADER_LOADER_FAVORITES = 373;
    private static final int ID_LOADER_LOADER_FAVORITES_REMOVE = 383;
    private static final int ID_LOADER_FAVORITES_LOAD = 400;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mMovieTitle = (TextView) findViewById(R.id.movie_title);
        mMovieReleaseDate = (TextView) findViewById(R.id.movie_release_date);
        mMoviePoster = (ImageView) findViewById(R.id.movie_poster);
        mMovieVotes = (TextView) findViewById(R.id.movie_vote_average);
        mMovieSynopsis = (TextView) findViewById(R.id.movie_plot_synopsis);
        mReviewButton = (Button) findViewById(R.id.review_button);
        mMovieCommentsLabel = (TextView) findViewById(R.id.comments_label);
        mMovieCommentsContent = (RecyclerView) findViewById(R.id.reviews_recycler_view);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        backdrop=(ImageView) findViewById(R.id.backdrop);
        mDetailRecyclerView=(RecyclerView) findViewById(R.id.reviews_recycler_view);
        mDetailRecyclerView.setFocusable(false);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        Intent intentThatStartedThisActivity = getIntent();

        mMovieUrlID = intentThatStartedThisActivity.getData();
        mMovieID = intentThatStartedThisActivity.getIntExtra("id", 0);
        sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(DetailActivity.this);
        orderValue = sharedPreferences.getString(getString(R.string.pref_order_key),
                getString(R.string.pref_popular_value));

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
        layoutManager=new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mDetailRecyclerView.setLayoutManager(layoutManager);

        mDetailAdapter = new DetailAdapter(this);
        mDetailRecyclerView.setAdapter(mDetailAdapter);


        if (mMovieUrlID == null)
            throw new NullPointerException("URI FOR DETAILACTIVITY cannot be null");

        if (orderValue.equals("favorite")) {
            getSupportLoaderManager().initLoader(ID_LOADER_FAVORITES_LOAD, null, this);
        } else {
            getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_screen, menu);
        MenuItem removeItem = menu.findItem(R.id.remove_favorites);
        MenuItem addItem = menu.findItem(R.id.add_favorites);
        if ((orderValue.equals("favorite"))) {
            removeItem.setVisible(true);
            addItem.setVisible(false);
        } else {
            removeItem.setVisible(false);
            addItem.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_favorites) {

            getSupportLoaderManager().initLoader(ID_LOADER_LOADER_FAVORITES, null, DetailActivity.this);
            item.setVisible(false);
            return true;
        }

        if (id == R.id.remove_favorites) {
            getSupportLoaderManager().initLoader(ID_LOADER_LOADER_FAVORITES_REMOVE, null, DetailActivity.this);
            this.finish();

            return true;

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {


        switch (id) {

            case ID_DETAIL_LOADER: {

                Log.i(LOG_TAG, "mMovie URL ID value :" + mMovieUrlID);

                AsyncTask<Void, Void, Void> mFetchMovieTask;

                mFetchMovieTask = new AsyncTask<Void, Void, Void>() {


                    ContentValues mMoviesTrailersAndReviewsValues;

                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {

                            mMoviesTrailersAndReviewsValues = MoviesJsonUtils.
                                    getSimpleMovieContentValuesFromJsonForTrailers(getApplicationContext(), Integer.toString(mMovieID));


                            if (mMoviesTrailersAndReviewsValues != null) {
                                ContentResolver movieContentResolver = getApplicationContext().getContentResolver();
                                String[] selectionArgsForUpdate = new String[]{Integer.toString(mMovieID)};


                                int rows = movieContentResolver.update(
                                        mMovieUrlID,
                                        mMoviesTrailersAndReviewsValues,
                                        MovieEntry.COLUMN_MOVIE_ID + "=?",
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
                        asyncDone = true;
                    }
                };

                mFetchMovieTask.execute();

                return new CursorLoader(this,
                        mMovieUrlID,
                        MOVIE_DETAIL_PROJECTION,
                        null,
                        null,
                        null);


            }
            case ID_LOADER_LOADER_FAVORITES: {

                AsyncTask<Void, Void, Void> mFetchMovieTask;


                mFetchMovieTask = new AsyncTask<Void, Void, Void>() {
                    ContentValues mMoviesValuesForFavorites = new ContentValues();

                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {


                            mMoviesValuesForFavorites.put(MovieEntry.COLUMN_MOVIE_ID, Integer.toString(mMovieID));
                            Log.i(LOG_TAG, "mMovieID :" + mMovieID);
                            mMoviesValuesForFavorites.put(MovieEntry.COLUMN_MOVIE_TITLE, mMovieTitle.getText().toString());
                            Log.i(LOG_TAG, "Movie Title :" + mMovieTitle.getText().toString());
                            mMoviesValuesForFavorites.put(MovieEntry.COLUMN_MOVIE_RELEASE_DATE, mMovieReleaseDate.getText().toString());
                            Log.i(LOG_TAG, "Release Date :" + mMovieReleaseDate.toString());
                            mMoviesValuesForFavorites.put(MovieEntry.COLUMN_MOVIE_POSTER_URL, moviePosterUrl);
                            Log.i(LOG_TAG, "Poster url :" + moviePosterUrl);
                            mMoviesValuesForFavorites.put(MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE, mMovieVotes.getText().toString());
                            Log.i(LOG_TAG, "Movies Vote :" + mMovieVotes.getText().toString());
                            mMoviesValuesForFavorites.put(MovieEntry.COLUMN_MOVIE_OVERVIEW, mMovieSynopsis.getText().toString());
                            Log.i(LOG_TAG, "Movies Overview :" + mMovieSynopsis.getText().toString());
                            mMoviesValuesForFavorites.put(MovieEntry.COLUMN_MOVIE_TRAILER_URL, youtubeTrailerSOURCE);
                            Log.i(LOG_TAG, "Youtube source :" + youtubeTrailerSOURCE);
                            mMoviesValuesForFavorites.put(MovieEntry.COLUMN_MOVIE_BACKDROP_PATH, backdropPath);


                            if (mMoviesValuesForFavorites != null) {
                                ContentResolver movieContentResolver = getApplicationContext().getContentResolver();


                                movieContentResolver.insert(
                                        MovieEntry.CONTENT_URI,
                                        mMoviesValuesForFavorites);

                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                };

                mFetchMovieTask.execute();
                return null;
            }

            //delete implementuoti ir MovieProvider Delete vienam sutvarkyti
            case ID_LOADER_LOADER_FAVORITES_REMOVE: {
                AsyncTask<Void, Void, Void> mFetchMovieTask;

                mFetchMovieTask = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        String[] selectionArgsForUpdate = new String[]{Integer.toString(mMovieID)};
                        ContentResolver movieContentResolver = getApplicationContext().getContentResolver();
                        movieContentResolver.delete(mMovieUrlID,
                                MovieEntry.COLUMN_MOVIE_ID + "=?",
                                selectionArgsForUpdate);
                        return null;
                    }
                };
                mFetchMovieTask.execute();

                return null;
            }

            case ID_LOADER_FAVORITES_LOAD: {
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
        mMovieTitle.setVisibility(View.GONE);
        toolbar.setTitle(data.getString(INDEX_MOVIE_TITLE));
        mMovieReleaseDate.setText(data.getString(INDEX_MOVIE_RELEASE_DATE));

        moviePosterUrl = data.getString(INDEX_MOVIE_POSTER_URI);
        Picasso.with(getApplicationContext()).load(NetworkUtilities.
                buildUrlForImage(moviePosterUrl, "w500").toString()).into(mMoviePoster);
        mMovieVotes.setText(data.getString(INDEX_MOVIE_VOTE_AVERAGE));
        mMovieSynopsis.setText(data.getString(INDEX_MOVIE_OVERVIEW));
        youtubeTrailerSOURCE = data.getString(INDEX_MOVIE_TRAILER_URL);
        backdropPath=data.getString(INDEX_MOVIE_BACKDROP_PATH);
        Picasso.with(getApplicationContext()).load(NetworkUtilities.
                buildUrlForImage(backdropPath, "w500").toString()).into(backdrop);


        if (data.getString(INDEX_MOVIE_REVIEW_STRING) != null && !data.getString(INDEX_MOVIE_REVIEW_STRING).equals("")) {
            mMovieCommentsLabel.setVisibility(View.VISIBLE);
            mMovieCommentsContent.setVisibility(View.VISIBLE);
            String[] movieReviewStringArray = MoviesJsonUtils.convertStringToArray(data.
                    getString(INDEX_MOVIE_REVIEW_STRING), MoviesJsonUtils.separator1);

            mDetailAdapter.setReviewData(movieReviewStringArray);
        } else {
            mMovieCommentsLabel.setVisibility(View.GONE);
            mMovieCommentsContent.setVisibility(View.GONE);

        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        Log.i(LOG_TAG, "ON SAVE!!!!!!!!!!!!!!");

        mListState = mDetailRecyclerView.getLayoutManager().onSaveInstanceState();
        state.putParcelable(BUNDLE_RECYCLER_LAYOUT, mListState);
        Log.i(LOG_TAG, "STATE :" +mListState);


    }

    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        Log.i(LOG_TAG, "ON RESTORE!!!!!!!!!!!!!!");
        if (state != null) {
            mListState = state.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            mDetailRecyclerView.getLayoutManager().onRestoreInstanceState(mListState);
        }
//
    }
}
