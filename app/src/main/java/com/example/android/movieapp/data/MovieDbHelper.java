package com.example.android.movieapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.movieapp.data.MovieContract.MovieEntry;

/**
 * Created by Lukas on 2018-03-08.
 */

public class MovieDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME="movie.db";
    public static final int DATABASE_VERSION=2;

    public MovieDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_MOVIE_TABLE=
                "CREATE TABLE "+ MovieEntry.TABLE_NAME+" ("+
                        MovieEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MovieEntry.COLUMN_MOVIE_ID+" INTEGER NOT NULL, "+
                        MovieEntry.COLUMN_MOVIE_TITLE+" TEXT NOT NULL, "+
                        MovieEntry.COLUMN_MOVIE_RELEASE_DATE +" INTEGER NOT NULL, "+
                        MovieEntry.COLUMN_MOVIE_POSTER_URL +" TEXT NOT NULL, "+
                        MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE+" REAL NOT NULL, "+
                        MovieEntry.COLUMN_MOVIE_OVERVIEW+" TEXT NOT NULL, "+
                        MovieEntry.COLUMN_MOVIE_TRAILER_URL+" TEXT, "+
                        MovieEntry.COLUMN_MOVIE_REVIEW_STRING+" TEXT, "+
                        " UNIQUE (" + MovieEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " +MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
