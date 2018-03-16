package com.example.android.movieapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Lukas on 2018-03-08.
 */

public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.movieapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";
    public static final String FAVORITE_PATH = "favorites";

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIE)
                .build();
        public static final String PATH_FAVORITES_FINAL = PATH_MOVIE + "/" + FAVORITE_PATH;

        public static final String TABLE_NAME_FAVORITES = "FavoriteMovie";

        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_MOVIE_ID = "id";

        public static final String COLUMN_MOVIE_TITLE = "title";

        public static final String COLUMN_MOVIE_RELEASE_DATE = "date";

        public static final String COLUMN_MOVIE_POSTER_URL = "poster";

        public static final String COLUMN_MOVIE_VOTE_AVERAGE = "vote";

        public static final String COLUMN_MOVIE_OVERVIEW = "overview";

        public static final String COLUMN_MOVIE_TRAILER_URL = "trailer";

        public static final String COLUMN_MOVIE_REVIEW_STRING = "review";

        public static final String COLUMN_MOVIE_BACKDROP_PATH="backdrop";


        public static Uri buildMovieUriWithId(int id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(id))
                    .build();
        }

        public static Uri buildMovieUriWithIdForFavorites(int id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(FAVORITE_PATH)
                    .appendPath(Integer.toString(id))
                    .build();
        }

    }


}
