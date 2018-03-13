package com.example.android.movieapp.utilities;

import android.net.Uri;
import android.util.Log;

import com.example.android.movieapp.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Lukas on 2018-03-06.
 */

public class NetworkUtilities {
    private static final String TAG = NetworkUtilities.class.getSimpleName();

    private static final String MOVIE_URL =
            "https://api.themoviedb.org/3";

    private static final String MOVIE_IMAGE_BASE_URL = "http://image.tmdb.org/t/p";
    private static final String MOVIE_BASE_URL = MOVIE_URL;
    private static final String MOVIE_YOUTUBE_BASE_URL = "https://www.youtube.com/watch?v=";


    final static String QUERY_MOVIES = "movie";
    final static String QUERY_POPULAR = "popular";
    final static String QUERY_API_KEY = "api_key";


    public static URL buildUrl(String order) {
        Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                .appendEncodedPath(QUERY_MOVIES)
                .appendEncodedPath(order)
                .appendQueryParameter(QUERY_API_KEY, MainActivity.API_KEY)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

    public static URL buildUrlForImage(String path, String size) {
        Uri builtUri = Uri.parse(MOVIE_IMAGE_BASE_URL).buildUpon()
                .appendPath(size)
                .appendEncodedPath(path)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildUrlForVideosAndReviews(String whatNeed, String id) {
        Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                .appendEncodedPath(QUERY_MOVIES)
                .appendEncodedPath(id)
                .appendEncodedPath(whatNeed)
                .appendQueryParameter(QUERY_API_KEY, MainActivity.API_KEY)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

    public static URL buildUrlForTrailerVideo(String source) {
        Uri builtUri = Uri.parse(MOVIE_YOUTUBE_BASE_URL + source).buildUpon()
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }


    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

}
