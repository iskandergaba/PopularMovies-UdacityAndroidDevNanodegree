package com.example.alexgaba.popularmovies.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.alexgaba.popularmovies.MainActivity;
import com.example.alexgaba.popularmovies.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MoviesDataService extends IntentService {

    public MoviesDataService() { super("PopularMovies"); }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            for (int i = 0; i < MainActivity.mPopularJSONData.length(); i++) {
                String id = MainActivity.mPopularJSONData.getJSONObject(i).getString(MainActivity.TMDB_JSON_ID_KEY);
                if (!Utility.isMovieExisting(id, MainActivity.mTrailersJSONData)) {
                    JSONObject trailersJSON = getMovieTrailers(id);
                    if (trailersJSON != null) {
                        MainActivity.mTrailersJSONData = Utility.JsonAddObjectToArray(MainActivity.mTrailersJSONData, trailersJSON);
                    }
                }

                if (!Utility.isMovieExisting(id, MainActivity.mReviewsJSONData)) {
                    JSONObject reviewsJSON = getMovieReviews(id);
                    if (reviewsJSON != null) {
                        MainActivity.mReviewsJSONData = Utility.JsonAddObjectToArray(MainActivity.mReviewsJSONData, reviewsJSON);
                    }
                }
            }

            for (int i = 0; i < MainActivity.mTopRatedJSONData.length(); i++) {
                String id = MainActivity.mTopRatedJSONData.getJSONObject(i).getString(MainActivity.TMDB_JSON_ID_KEY);
                if (!Utility.isMovieExisting(id, MainActivity.mTrailersJSONData)) {
                    JSONObject trailersJSON = getMovieTrailers(id);
                    if (trailersJSON != null) {
                        MainActivity.mTrailersJSONData = Utility.JsonAddObjectToArray(MainActivity.mTrailersJSONData, trailersJSON);
                    }
                }

                if (!Utility.isMovieExisting(id, MainActivity.mReviewsJSONData)) {
                    JSONObject reviewsJSON = getMovieReviews(id);
                    if (reviewsJSON != null) {
                        MainActivity.mReviewsJSONData = Utility.JsonAddObjectToArray(MainActivity.mReviewsJSONData, reviewsJSON);
                    }
                }
            }

            for (int i = 0; i < MainActivity.mFavoriteJSONData.length(); i++) {
                String id = MainActivity.mFavoriteJSONData.getJSONObject(i).getString(MainActivity.TMDB_JSON_ID_KEY);
                if (!Utility.isMovieExisting(id, MainActivity.mTrailersJSONData)) {
                    JSONObject trailersJSON = getMovieTrailers(id);
                    if (trailersJSON != null) {
                        MainActivity.mTrailersJSONData = Utility.JsonAddObjectToArray(MainActivity.mTrailersJSONData, trailersJSON);
                    }
                }

                if (!Utility.isMovieExisting(id, MainActivity.mReviewsJSONData)) {
                    JSONObject reviewsJSON = getMovieReviews(id);
                    if (reviewsJSON != null) {
                        MainActivity.mReviewsJSONData = Utility.JsonAddObjectToArray(MainActivity.mReviewsJSONData, reviewsJSON);
                    }
                }
            }

            for (int i = 0; i < MainActivity.mReviewsJSONData.length(); i++) {
                JSONObject movie = MainActivity.mReviewsJSONData.getJSONObject(i);
                String id = movie.getString(MainActivity.TMDB_JSON_ID_KEY);
                if (!Utility.isMovieExisting(id, MainActivity.mPopularJSONData) &&
                        !Utility.isMovieExisting(id, MainActivity.mTopRatedJSONData) &&
                        !Utility.isMovieExisting(id, MainActivity.mFavoriteJSONData)) {
                    MainActivity.mReviewsJSONData = Utility.JsonRemoveObjectFromArray(MainActivity.mReviewsJSONData, movie);
                }
            }

            for (int i = 0; i < MainActivity.mTrailersJSONData.length(); i++) {
                JSONObject movie = MainActivity.mTrailersJSONData.getJSONObject(i);
                String id = movie.getString(MainActivity.TMDB_JSON_ID_KEY);
                if (!Utility.isMovieExisting(id, MainActivity.mPopularJSONData) &&
                        !Utility.isMovieExisting(id, MainActivity.mTopRatedJSONData) &&
                        !Utility.isMovieExisting(id, MainActivity.mFavoriteJSONData)) {
                    MainActivity.mTrailersJSONData = Utility.JsonRemoveObjectFromArray(MainActivity.mTrailersJSONData, movie);
                }
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(MainActivity.TMDB_TRAILERS_PARAM, MainActivity.mTrailersJSONData.toString());
            editor.putString(MainActivity.TMDB_REVIEWS_PARAM, MainActivity.mReviewsJSONData.toString());
            editor.apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private JSONObject getMovieTrailers(String id) {
        String movieData = null;
        JSONObject movieJSONData = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String dataType  = MainActivity.TMDB_TRAILERS_PARAM;
        try {
            URL url = new URL(MoviesService.TMDB_MOVIE_BASE_URL + id + "/" + dataType + MoviesService.TMDB_API_KEY);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            movieData = buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();

            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                movieJSONData = new JSONObject(movieData);

            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return movieJSONData;
    }

    private JSONObject getMovieReviews(String id) {
        String movieData = null;
        JSONObject movieJSONData = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String dataType  = MainActivity.TMDB_REVIEWS_PARAM;
        try {
            URL url = new URL(MoviesService.TMDB_MOVIE_BASE_URL + id + "/" + dataType + MoviesService.TMDB_API_KEY);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            movieData = buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();

            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                movieJSONData = new JSONObject(movieData);

            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return movieJSONData;
    }
}