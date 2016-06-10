package com.example.alexgaba.popularmovies.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.alexgaba.popularmovies.BuildConfig;
import com.example.alexgaba.popularmovies.MoviesFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MoviesService extends IntentService{

    public static final String TMDB_MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
    public static final String TMDB_API_KEY = BuildConfig.TMDB_MAP_API_KEY;
    public static final String TMDB_JSON_RESULTS_KEY = "results";


    public MoviesService() { super("PopularMovies"); }

    @Override
    protected void onHandleIntent(Intent intent) {
        ArrayList<String> sortParams = new ArrayList<>();
        sortParams.add(MoviesFragment.TMDB_POPULAR_PARAM);
        sortParams.add(MoviesFragment.TMDB_TOP_RATED_PARAM);
        for (int i = 0; i < sortParams.size(); i++) {
            String moviesDB = null;
            JSONArray moviesJSONDB = null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String sortParam = sortParams.get(i);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();
            try {
                URL url = new URL(TMDB_MOVIE_BASE_URL + sortParam + TMDB_API_KEY);
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
                moviesDB = buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
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
                    moviesJSONDB = new JSONObject(moviesDB).getJSONArray(TMDB_JSON_RESULTS_KEY);

                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
            if (moviesDB != null) {
                if (sortParam.equals(MoviesFragment.TMDB_TOP_RATED_PARAM)) {
                    MoviesFragment.mTopRatedJSONData = moviesJSONDB;
                    editor.putString(MoviesFragment.TMDB_TOP_RATED_PARAM, MoviesFragment.mTopRatedJSONData.toString());
                } else {
                    MoviesFragment.mPopularJSONData = moviesJSONDB;
                    editor.putString(MoviesFragment.TMDB_POPULAR_PARAM, MoviesFragment.mPopularJSONData.toString());
                }
                editor.apply();
            }
        }

        startService(new Intent(this, MoviesDataService.class));
        MoviesFragment.isContentUpdated = true;
    }
}