package com.example.alexgaba.popularmovies;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchMovieDataTask extends AsyncTask<String, Void, JSONObject> {


    @Override
    protected JSONObject doInBackground(String... params) {
        String movieData = null;
        JSONObject movieJSONData = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String id = params[0];
        String dataType  = params[1];
        try {
            URL url = new URL(FetchMoviesTask.TMDB_MOVIE_BASE_URL + id + "/" + dataType + FetchMoviesTask.TMDB_API_KEY);
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