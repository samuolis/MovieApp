package com.example.android.movieapp;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.example.android.movieapp.MovieAdapter.MovieAdapterOnClickHandler;
import com.example.android.movieapp.data.MovieContract;
import com.example.android.movieapp.data.MovieContract.MovieEntry;
import com.example.android.movieapp.utilities.MoviesJsonUtils;
import com.example.android.movieapp.utilities.NetworkUtilities;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements MovieAdapterOnClickHandler,
        OnSharedPreferenceChangeListener,
        LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private ProgressBar mLoadingIndicator;
    public static String API_KEY;
    private static boolean PREFERENCES_HAVE_BEEN_UPDATED = false;
    private static final int MOVIE_LOADER_ID = 0;

    public static final String FAVORITE_PATH = "favorites";
    public static final String FAVORITE_PREF = "favorite";
    public static final String POPULAR_PREF = "popular";
    public static final String TOP_RATED_PREF = "top_rated";


    public static final String[] MAIN_MOVIE_PROJECTION = {
            MovieEntry.COLUMN_MOVIE_POSTER_URL,
            MovieEntry.COLUMN_MOVIE_ID,
    };

    public static final int INDEX_MOVIE_POSTER = 0;
    public static final int INDEX_MOVIE_ID = 1;
    private int mPosition = RecyclerView.NO_POSITION;
    SharedPreferences sharedPreferencesInAssync;
    String orderValueInAssync;
    GridLayoutManager gridLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        API_KEY = getString(R.string.api_key_movies);
        sharedPreferencesInAssync = PreferenceManager.
                getDefaultSharedPreferences(MainActivity.this);
        orderValueInAssync = sharedPreferencesInAssync.getString(getString(R.string.pref_order_key),
                getString(R.string.pref_popular_value));


        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_indicator_main);

        gridLayoutManager = new GridLayoutManager(this, 2
                , GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mMovieAdapter = new MovieAdapter(this, this);
        mRecyclerView.setAdapter(mMovieAdapter);


        showLoading();
        getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);


    }

    @Override
    public void onClick(int currentMovie) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);

        Uri uriForMovieId;
        if (orderValueInAssync.equals("favorite")) {
            uriForMovieId = MovieContract.MovieEntry.buildMovieUriWithIdForFavorites(currentMovie);
        } else {
            uriForMovieId = MovieContract.MovieEntry.buildMovieUriWithId(currentMovie);
        }
        Log.i(LOG_TAG, "URL : " + uriForMovieId);
        intentToStartDetailActivity.putExtra("id", currentMovie);
        intentToStartDetailActivity.setData(uriForMovieId);
        startActivity(intentToStartDetailActivity);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PREFERENCES_HAVE_BEEN_UPDATED = true;

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle loaderArgs) {

        sharedPreferencesInAssync = PreferenceManager.
                getDefaultSharedPreferences(MainActivity.this);
        orderValueInAssync = sharedPreferencesInAssync.getString(getString(R.string.pref_order_key),
                getString(R.string.pref_popular_value));

        Log.i(LOG_TAG, "orderValue!!!!!!!!!!!!!!!!!! " + orderValueInAssync);

        if (orderValueInAssync.equals("popular") || orderValueInAssync.equals("top_rated")) {

            AsyncTask<Void, Void, Void> mFetchMovieTask;

            mFetchMovieTask = new AsyncTask<Void, Void, Void>() {
                ContentValues[] mMoviesValues;

                @Override
                protected Void doInBackground(Void... voids) {
                    try {


                        URL moviesRequestUrl = NetworkUtilities.buildUrl(orderValueInAssync);

                        String jsonMoviesResponse = NetworkUtilities.getResponseFromHttpUrl(moviesRequestUrl);


                        mMoviesValues = MoviesJsonUtils
                                .getSimpleMovieContentValuesFromJson(getApplicationContext(), jsonMoviesResponse);

                        if (mMoviesValues != null && mMoviesValues.length != 0) {
                            ContentResolver movieContentResolver = getApplicationContext().getContentResolver();


                            movieContentResolver.delete(
                                    MovieEntry.CONTENT_URI,
                                    null,
                                    null);

                            movieContentResolver.bulkInsert(
                                    MovieEntry.CONTENT_URI,
                                    mMoviesValues);

                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

            };

            mFetchMovieTask.execute();

//        return new AsyncTaskLoader<String[]>(this) {
//            String[] mMovieData = null;
//
//            @Override
//            protected void onStartLoading() {
//                if (mMovieData != null) {
//                    deliverResult(mMovieData);
//                } else {
//                    forceLoad();
//                }
//            }
//
//            @Override
//            public String[] loadInBackground() {
//
//                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
//                String orderValue = sharedPreferences.getString(getString(R.string.pref_order_key), getString(R.string.pref_popular_value));
//                URL moviesRequestUrl = NetworkUtilities.buildUrl(orderValue);
//                Log.i(LOG_TAG, "URL : " + moviesRequestUrl);
//
//
//                try {
//                    String jsonMoviesResponse = NetworkUtilities.getResponseFromHttpUrl(moviesRequestUrl);
//
//
//                    String[] simpleJsonMoviesResponse = MoviesJsonUtils.getSimpleMovieStringFromJson(MainActivity.this, jsonMoviesResponse);
//                    return simpleJsonMoviesResponse;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return null;
//
//                }
//            }
//
//            public void deliverResult(String[] data) {
//                mMovieData = data;
//                super.deliverResult(data);
//            }
//        };
//    }

//        return new AsyncTaskLoader<Cursor>(this) {
//            ContentValues[] mMoviesValues;
//
//            @Override
//            public Cursor loadInBackground() {
//                try {
//                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
//                    String orderValue = sharedPreferences.getString(getString(R.string.pref_order_key), getString(R.string.pref_popular_value));
//                    URL moviesRequestUrl = NetworkUtilities.buildUrl(orderValue);
//
//                    String jsonMoviesResponse = NetworkUtilities.getResponseFromHttpUrl(moviesRequestUrl);
//
//
//                    mMoviesValues = MoviesJsonUtils
//                            .getSimpleMovieContentValuesFromJson(getApplicationContext(), jsonMoviesResponse);
//
//                    if (mMoviesValues != null && mMoviesValues.length != 0) {
//                        ContentResolver movieContentResolver = getApplicationContext().getContentResolver();
//
//
//                        movieContentResolver.delete(
//                                MovieEntry.CONTENT_URI,
//                                null,
//                                null);
//
//                        movieContentResolver.bulkInsert(
//                                MovieEntry.CONTENT_URI,
//                                mMoviesValues);
//
//                    }
//
//                    Uri movieQueryUri = MovieEntry.CONTENT_URI;
//                    return (Cursor) new CursorLoader(getApplicationContext(),
//                            movieQueryUri,
//                            MAIN_MOVIE_PROJECTION,
//                            null,
//                            null,
//                            null);
//
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return null;
//                }
//            }
//        };
//    }

            switch (id) {
                case MOVIE_LOADER_ID:
                    Uri movieQueryUri = MovieEntry.CONTENT_URI;
                    return new CursorLoader(this,
                            movieQueryUri,
                            MAIN_MOVIE_PROJECTION,
                            null,
                            null,
                            null);
                default:
                    throw new RuntimeException("Loader Not Implemented: " + id);
            }
        } else {

            switch (id) {
                case MOVIE_LOADER_ID:
                    Uri favoriteMovieQueryUri = MovieEntry.CONTENT_URI;
                    Uri favoriteQuerry = favoriteMovieQueryUri.buildUpon().appendPath(FAVORITE_PATH).build();


                    return new CursorLoader(this,
                            favoriteQuerry,
                            MAIN_MOVIE_PROJECTION,
                            null,
                            null,
                            null);
                default:
                    throw new RuntimeException("Loader Not Implemented: " + id);
            }

        }
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null) {

            mMovieAdapter.swapCursor(data);
            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            mRecyclerView.smoothScrollToPosition(mPosition);
            if (data.getCount() != 0) showMovieDataView();
        }


        switch (orderValueInAssync) {
            case FAVORITE_PREF: {
                setTitle("Favorites");
                break;
            }
            case TOP_RATED_PREF: {
                setTitle("Top Rated");
                break;
            }
            case POPULAR_PREF: {
                setTitle("Popular");
                break;
            }
            default:
                throw new RuntimeException("Sharred Pref not selected : " + orderValueInAssync);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.settings_menu) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (PREFERENCES_HAVE_BEEN_UPDATED) {
            getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
            PREFERENCES_HAVE_BEEN_UPDATED = false;
        }
    }

    private void showLoading() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    private void showMovieDataView() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

}
