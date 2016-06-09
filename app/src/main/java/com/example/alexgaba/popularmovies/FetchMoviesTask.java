package com.example.alexgaba.popularmovies;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchMoviesTask extends AsyncTask<String, Void, JSONArray> {

    public static final String TMDB_MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
    public static final String TMDB_API_KEY = BuildConfig.TMDB_MAP_API_KEY;
    public static final String TMDB_JSON_RESULTS_KEY = "results";

    @Override
    protected JSONArray doInBackground(String... params) {

        String moviesDB = null;
        JSONArray moviesJSONDB = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String sortParam = params[0];
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
            return null;
        }finally {
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
        return moviesJSONDB;
    }
}