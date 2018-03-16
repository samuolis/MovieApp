package com.example.android.movieapp.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.example.android.movieapp.data.MovieContract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lukas on 2018-03-06.
 */

public class MoviesJsonUtils {
    public static final String LOG_TAG = MoviesJsonUtils.class.getSimpleName();

    public static String separator1 = "__,__";
    public static String separator2 = "__.__";

    public static String convertArrayToString(String[] array, String separator) {
        String str = "";
        for (int i = 0; i < array.length; i++) {
            str = str + array[i];
            // Do not append comma at the end of last element
            if (i < array.length - 1) {
                str = str + separator;
            }
        }
        return str;
    }

    public static String[] convertStringToArray(String str, String separator) {
        String[] arr = str.split(separator);
        return arr;
    }

    public static ContentValues[] getSimpleMovieContentValuesFromJson(Context context, String moviesJsonStr) throws JSONException, IOException {

        final String OWN_MESSAGE_CODE = "status_code";
        final String OWM_RESULT = "results";
        final String OWN_IMAGE_PATH = "poster_path";
        final String OWN_MOVIE_TITLE = "title";
        final String OWN_MOVIE_RELEASE_DATE = "release_date";
        final String OWN_MOVIE_VOTE = "vote_average";
        final String OWN_OVERVIEW = "overview";
        final String OWN_ID = "id";
        final String OWN_MOVIE_BACKDROP="backdrop_path";


        JSONObject moviesJson = new JSONObject(moviesJsonStr);

        if (moviesJson.has(OWN_MESSAGE_CODE)) {

            int errorCode = moviesJson.getInt(OWN_MESSAGE_CODE);
            Log.i(LOG_TAG, "error code :   " + errorCode);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        JSONArray moviesArray = moviesJson.getJSONArray(OWM_RESULT);


        ContentValues[] movieContentValues = new ContentValues[moviesArray.length()];

        for (int i = 0; i < moviesArray.length(); i++) {
            JSONObject movieById = moviesArray.getJSONObject(i);
            String imagePath = movieById.getString(OWN_IMAGE_PATH);
            String movieTitle = movieById.getString(OWN_MOVIE_TITLE);
            String movieReleaseDate = movieById.getString(OWN_MOVIE_RELEASE_DATE);
            String movieVote = movieById.getString(OWN_MOVIE_VOTE);
            String movieOverview = movieById.getString(OWN_OVERVIEW);
            String movieId = movieById.getString(OWN_ID);
            String movieBackdrop=movieById.getString(OWN_MOVIE_BACKDROP);


//            String reviewJSON=NetworkUtilities.getResponseFromHttpUrl(NetworkUtilities.
//                    buildUrlForVideosAndReviews("reviews", Integer.getInteger(movieId)));
//            String trailerJSON=NetworkUtilities.getResponseFromHttpUrl(NetworkUtilities.
//                    buildUrlForVideosAndReviews("trailers", Integer.getInteger(movieId)));
//
//            JSONObject movierReviewJson = new JSONObject(reviewJSON);
//            JSONArray moviesReviewArray = moviesJson.getJSONArray(OWM_RESULT);
//
//            JSONObject moviesTrailerJson = new JSONObject(trailerJSON);
//


            //parsedMoviesData[i] = imagePath+"#"+movieTitle+"#"+movieReleaseDate+"#"+movieVote+"#"+movieOverview;
            //Log.i(LOG_TAG, "visi duomenys    " + parsedMoviesData);

            ContentValues movieValue = new ContentValues();
            movieValue.put(MovieEntry.COLUMN_MOVIE_ID, movieId);
            movieValue.put(MovieEntry.COLUMN_MOVIE_TITLE, movieTitle);
            movieValue.put(MovieEntry.COLUMN_MOVIE_RELEASE_DATE, movieReleaseDate);
            movieValue.put(MovieEntry.COLUMN_MOVIE_POSTER_URL, imagePath);
            movieValue.put(MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE, movieVote);
            movieValue.put(MovieEntry.COLUMN_MOVIE_OVERVIEW, movieOverview);
            movieValue.put(MovieEntry.COLUMN_MOVIE_BACKDROP_PATH, movieBackdrop);


            movieContentValues[i] = movieValue;
        }
        return movieContentValues;

    }


    public static ContentValues getSimpleMovieContentValuesFromJsonForTrailers(Context context, String id) throws JSONException, IOException {


        URL reviewsUrl = NetworkUtilities.buildUrlForVideosAndReviews("trailers", id);
        Log.i(LOG_TAG, "review url : " + reviewsUrl);
        String reviewJson = NetworkUtilities.getResponseFromHttpUrl(reviewsUrl);
        Log.i(LOG_TAG, "review JSON : " + reviewJson);

        String youtubeTrailerSource = MoviesJsonUtils.getYoutubeTrailer(context, reviewJson);
        Log.i(LOG_TAG, "YOUTUBE SOURCE : " + youtubeTrailerSource);

        String jsonMovieReviewResponse = NetworkUtilities
                .getResponseFromHttpUrl(NetworkUtilities.buildUrlForVideosAndReviews("reviews", id));

        String simpleJsonMoviesReviewData = MoviesJsonUtils
                .getMovieReviews(context, jsonMovieReviewResponse);


//            String reviewJSON=NetworkUtilities.getResponseFromHttpUrl(NetworkUtilities.
//                    buildUrlForVideosAndReviews("reviews", Integer.getInteger(movieId)));
//            String trailerJSON=NetworkUtilities.getResponseFromHttpUrl(NetworkUtilities.
//                    buildUrlForVideosAndReviews("trailers", Integer.getInteger(movieId)));
//
//            JSONObject movierReviewJson = new JSONObject(reviewJSON);
//            JSONArray moviesReviewArray = moviesJson.getJSONArray(OWM_RESULT);
//
//            JSONObject moviesTrailerJson = new JSONObject(trailerJSON);
//


        //parsedMoviesData[i] = imagePath+"#"+movieTitle+"#"+movieReleaseDate+"#"+movieVote+"#"+movieOverview;
        //Log.i(LOG_TAG, "visi duomenys    " + parsedMoviesData);

        ContentValues movieValue = new ContentValues();
        movieValue.put(MovieEntry.COLUMN_MOVIE_TRAILER_URL, youtubeTrailerSource);
        movieValue.put(MovieEntry.COLUMN_MOVIE_REVIEW_STRING, simpleJsonMoviesReviewData);


        return movieValue;

    }

    public static String getYoutubeTrailer(Context context, String moviesTrailersJsonStr) throws JSONException {
        final String OWN_TRAILER_TYPE = "type";
        final String OWN_YOUTUBE_SOURCE = "source";
        final String OWN_MESSAGE_CODE = "status_code";
        final String OWM_YOUTUBE_LABEL = "youtube";

        JSONObject moviesTrailerJson = new JSONObject(moviesTrailersJsonStr);

        if (moviesTrailerJson.has(OWN_MESSAGE_CODE)) {

            int errorCode = moviesTrailerJson.getInt(OWN_MESSAGE_CODE);
            Log.i(LOG_TAG, "error code :   " + errorCode);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }
        JSONArray youtubeVideosArray = moviesTrailerJson.getJSONArray(OWM_YOUTUBE_LABEL);
        List<String> trailersSources = new ArrayList<>();

        int j = 0;
        Log.i(LOG_TAG, "YOUTUBE ARRAY SIZE :" + youtubeVideosArray.length());
        for (int i = 0; i < youtubeVideosArray.length(); i++) {
            JSONObject movieTrailerById = youtubeVideosArray.getJSONObject(i);

            String videoType = movieTrailerById.getString(OWN_TRAILER_TYPE);
            Log.i(LOG_TAG, "TYPE :" + videoType);
            if (videoType.equals("Trailer")) {
                Log.i(LOG_TAG, "SOURCE CODE :" + movieTrailerById.getString(OWN_YOUTUBE_SOURCE));
                trailersSources.add(movieTrailerById.getString(OWN_YOUTUBE_SOURCE));
                Log.i(LOG_TAG, "Veikia ADD ");

            }

        }
        Log.i(LOG_TAG, "TRAILER LIST SIZE :" + trailersSources.size());
        if (trailersSources.size() > 0) {
            return trailersSources.get(0);
        } else return null;


    }

    public static String getMovieReviews(Context context, String moviesReviewsJsonStr) throws JSONException {

        final String OWN_REVIEW_AUTHOR = "author";
        final String OWN_REVIEW_CONTENT = "content";
        final String OWN_MESSAGE_CODE = "status_code";
        final String OWM_RESULT = "results";

        String[] parsedMoviesReviewsData = null;


        JSONObject moviesReviewJson = new JSONObject(moviesReviewsJsonStr);

        if (moviesReviewJson.has(OWN_MESSAGE_CODE)) {

            int errorCode = moviesReviewJson.getInt(OWN_MESSAGE_CODE);
            Log.i(LOG_TAG, "error code :   " + errorCode);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }
        JSONArray moviesReviewArray = moviesReviewJson.getJSONArray(OWM_RESULT);

        parsedMoviesReviewsData = new String[moviesReviewArray.length()];


        for (int i = 0; i < moviesReviewArray.length(); i++) {
            JSONObject movieReviewById = moviesReviewArray.getJSONObject(i);

            String reviewAuthor = movieReviewById.getString(OWN_REVIEW_AUTHOR);
            Log.i(LOG_TAG, "Author :" + reviewAuthor);
            String reviewMessage = movieReviewById.getString(OWN_REVIEW_CONTENT);
            parsedMoviesReviewsData[i] = reviewAuthor + "#" + reviewMessage;

        }
        Log.i(LOG_TAG, "Baige VISKA");
        String parsedMoviesReviewsToSingleString = convertArrayToString(parsedMoviesReviewsData, separator1);
        return parsedMoviesReviewsToSingleString;


    }


//    public static String[] getSimpleMovieStringFromJsonForTrailersAndReviews
//            (Context context, String moviesTrailersJsonStr, String moviesReviewsJsonStr) throws JSONException, IOException {
//
//        final String OWN_REVIEW_AUTHOR="author";
//        final String OWN_REVIEW_CONTENT="content";
//        final String OWN_TRAILER_TYPE="youtube";
//        final String OWN_TRAILER_SOURCE="source";
//        final String OWN_MESSAGE_CODE = "status_code";
//        final String OWM_RESULT = "results";
//
//        JSONObject movierReviewJson = new JSONObject(reviewJSON);
//
//
//        JSONObject moviesTrailerJson = new JSONObject(trailerJSON);
//
//
//        if (moviesJson.has(OWN_MESSAGE_CODE)) {
//
//            int errorCode = moviesJson.getInt(OWN_MESSAGE_CODE);
//            Log.i(LOG_TAG, "error code :   " + errorCode);
//
//            switch (errorCode) {
//                case HttpURLConnection.HTTP_OK:
//                    break;
//                case HttpURLConnection.HTTP_NOT_FOUND:
//                    /* Location invalid */
//                    return null;
//                default:
//                    /* Server probably down */
//                    return null;
//            }
//        }
//
//        JSONArray moviesReviewArray = moviesJson.getJSONArray(OWM_RESULT);
//
//
//        String[] movieString= new String[moviesArray.length()];
//
////        String reviewJSON=NetworkUtilities.getResponseFromHttpUrl(NetworkUtilities.
////                    buildUrlForVideosAndReviews("reviews", Integer.getInteger(movieId)));
////        String trailerJSON=NetworkUtilities.getResponseFromHttpUrl(NetworkUtilities.
////                    buildUrlForVideosAndReviews("trailers", Integer.getInteger(movieId)));
//
//
//
//
//
//            //parsedMoviesData[i] = imagePath+"#"+movieTitle+"#"+movieReleaseDate+"#"+movieVote+"#"+movieOverview;
//            //Log.i(LOG_TAG, "visi duomenys    " + parsedMoviesData);
//
//            ContentValues movieValue=new ContentValues();
//            movieValue.put(MovieEntry.COLUMN_MOVIE_TITLE, movieTitle);
//            movieValue.put(MovieEntry.COLUMN_MOVIE_RELEASE_DATE, movieReleaseDate);
//            movieValue.put(MovieEntry.COLUMN_MOVIE_POSTER_URL, imagePath);
//            movieValue.put(MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE, movieVote);
//            movieValue.put(MovieEntry.COLUMN_MOVIE_OVERVIEW, movieOverview);
//
//            movieContentValues[i]=movieValue;
//        }
//        return movieContentValues;
//
//    }


}
