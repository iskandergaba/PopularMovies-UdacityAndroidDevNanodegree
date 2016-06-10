package com.example.alexgaba.popularmovies.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.alexgaba.popularmovies.MoviesFragment;
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
            for (int i = 0; i < MoviesFragment.mPopularJSONData.length(); i++) {
                String id = MoviesFragment.mPopularJSONData.getJSONObject(i).getString(MoviesFragment.TMDB_JSON_ID_KEY);
                if (!Utility.isMovieExisting(id, MoviesFragment.mTrailersJSONData)) {
                    JSONObject trailersJSON = getMovieTrailers(id);
                    if (trailersJSON != null) {
                        MoviesFragment.mTrailersJSONData = Utility.JsonAddObjectToArray(MoviesFragment.mTrailersJSONData, trailersJSON);
                    }
                }

                if (!Utility.isMovieExisting(id, MoviesFragment.mReviewsJSONData)) {
                    JSONObject reviewsJSON = getMovieReviews(id);
                    if (reviewsJSON != null) {
                        MoviesFragment.mReviewsJSONData = Utility.JsonAddObjectToArray(MoviesFragment.mReviewsJSONData, reviewsJSON);
                    }
                }
            }

            for (int i = 0; i < MoviesFragment.mTopRatedJSONData.length(); i++) {
                String id = MoviesFragment.mTopRatedJSONData.getJSONObject(i).getString(MoviesFragment.TMDB_JSON_ID_KEY);
                if (!Utility.isMovieExisting(id, MoviesFragment.mTrailersJSONData)) {
                    JSONObject trailersJSON = getMovieTrailers(id);
                    if (trailersJSON != null) {
                        MoviesFragment.mTrailersJSONData = Utility.JsonAddObjectToArray(MoviesFragment.mTrailersJSONData, trailersJSON);
                    }
                }

                if (!Utility.isMovieExisting(id, MoviesFragment.mReviewsJSONData)) {
                    JSONObject reviewsJSON = getMovieReviews(id);
                    if (reviewsJSON != null) {
                        MoviesFragment.mReviewsJSONData = Utility.JsonAddObjectToArray(MoviesFragment.mReviewsJSONData, reviewsJSON);
                    }
                }
            }

            for (int i = 0; i < MoviesFragment.mFavoriteJSONData.length(); i++) {
                String id = MoviesFragment.mFavoriteJSONData.getJSONObject(i).getString(MoviesFragment.TMDB_JSON_ID_KEY);
                if (!Utility.isMovieExisting(id, MoviesFragment.mTrailersJSONData)) {
                    JSONObject trailersJSON = getMovieTrailers(id);
                    if (trailersJSON != null) {
                        MoviesFragment.mTrailersJSONData = Utility.JsonAddObjectToArray(MoviesFragment.mTrailersJSONData, trailersJSON);
                    }
                }

                if (!Utility.isMovieExisting(id, MoviesFragment.mReviewsJSONData)) {
                    JSONObject reviewsJSON = getMovieReviews(id);
                    if (reviewsJSON != null) {
                        MoviesFragment.mReviewsJSONData = Utility.JsonAddObjectToArray(MoviesFragment.mReviewsJSONData, reviewsJSON);
                    }
                }
            }

            for (int i = 0; i < MoviesFragment.mReviewsJSONData.length(); i++) {
                JSONObject movie = MoviesFragment.mReviewsJSONData.getJSONObject(i);
                String id = movie.getString(MoviesFragment.TMDB_JSON_ID_KEY);
                if (!Utility.isMovieExisting(id, MoviesFragment.mPopularJSONData) &&
                        !Utility.isMovieExisting(id, MoviesFragment.mTopRatedJSONData) &&
                        !Utility.isMovieExisting(id, MoviesFragment.mFavoriteJSONData)) {
                    MoviesFragment.mReviewsJSONData = Utility.JsonRemoveObjectFromArray(MoviesFragment.mReviewsJSONData, movie);
                }
            }

            for (int i = 0; i < MoviesFragment.mTrailersJSONData.length(); i++) {
                JSONObject movie = MoviesFragment.mTrailersJSONData.getJSONObject(i);
                String id = movie.getString(MoviesFragment.TMDB_JSON_ID_KEY);
                if (!Utility.isMovieExisting(id, MoviesFragment.mPopularJSONData) &&
                        !Utility.isMovieExisting(id, MoviesFragment.mTopRatedJSONData) &&
                        !Utility.isMovieExisting(id, MoviesFragment.mFavoriteJSONData)) {
                    MoviesFragment.mTrailersJSONData = Utility.JsonRemoveObjectFromArray(MoviesFragment.mTrailersJSONData, movie);
                }
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(MoviesFragment.TMDB_TRAILERS_PARAM, MoviesFragment.mTrailersJSONData.toString());
            editor.putString(MoviesFragment.TMDB_REVIEWS_PARAM, MoviesFragment.mReviewsJSONData.toString());
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
        String dataType  = MoviesFragment.TMDB_TRAILERS_PARAM;
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
        String dataType  = MoviesFragment.TMDB_REVIEWS_PARAM;
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