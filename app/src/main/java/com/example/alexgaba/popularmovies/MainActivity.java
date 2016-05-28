package com.example.alexgaba.popularmovies;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonWriter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    public static final String TMDB_MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
    public static final String TMDB_POPULAR_PARAM = "popular";
    public static final String TMDB_TOP_RATED_PARAM = "top_rated";
    public static final String TMDB_API_KEY = "?api_key=fb3915b06d0641541692779f202b518c";
    public static final String TMDB_JSON_RESULTS_KEY = "results";
    public static final String  TMDB_POSTER_BASE_URL = "https://image.tmdb.org/t/p/";
    public static final String TMDB_POSTER_SIZE_PARAM = "w300/";
    public static final String TMDB_JSON_POSTER_KEY = "poster_path";

    public JSONArray TMDB_POPULAR = null;
    public JSONArray TMDB_TOP_RATED = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            updateDB();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        try {
            String[] posterThumbs = getPosters(TMDB_POPULAR);
            GridView gridView = (GridView) findViewById(R.id.movies_gridview);
            gridView.setAdapter(new ImageAdapter(this,  posterThumbs));
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v,
                                        int position, long id) {
                    Toast.makeText(MainActivity.this, "" + position,
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    private String[] getPosters(JSONArray moviesDB) throws JSONException {

        ArrayList<String> urls = new ArrayList<>();
        if (moviesDB != null) {
            for (int i = 0; i < moviesDB.length(); i++) {
                JSONObject movie = moviesDB.getJSONObject(i);
                String url = TMDB_POSTER_BASE_URL + TMDB_POSTER_SIZE_PARAM + movie.getString(TMDB_JSON_POSTER_KEY).substring(1);
                urls.add(url);
            }
        }
        return urls.toArray(new String[urls.size()]);
    }

    private void updateDB() throws ExecutionException, InterruptedException {
        FetchMoviesTask popularMoviesTask = new FetchMoviesTask();
        FetchMoviesTask topMoviesTask = new FetchMoviesTask();
        TMDB_POPULAR = popularMoviesTask.execute(TMDB_POPULAR_PARAM).get();
        TMDB_TOP_RATED = topMoviesTask.execute(TMDB_TOP_RATED_PARAM).get();
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        String[] mThumbIds;

        public ImageAdapter(Context c, String[] mThumbIds) {
            mContext = c;
            this.mThumbIds = mThumbIds;
        }

        @Override
        public int getCount() {
            return mThumbIds.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, 1024));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView = (ImageView) convertView;
            }

            Picasso
                    .with(mContext)
                    .load(mThumbIds[position])
                    .into(imageView);

            return imageView;
        }
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, JSONArray> {

        private ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Fetching data from the server...");
            this.dialog.show();
        }

        @Override
        protected JSONArray doInBackground(String... params) {

            String moviesDB = null;
            JSONArray moviesJsonDB = null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String genreParam = params[0];
            try {
                URL url = new URL(TMDB_MOVIE_BASE_URL + genreParam + TMDB_API_KEY);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                moviesDB = buffer.toString();
            } catch (ProtocolException e) {
                e.printStackTrace();
                return null;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }

            try {
                moviesJsonDB = new JSONObject(moviesDB).getJSONArray(TMDB_JSON_RESULTS_KEY);

            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            return moviesJsonDB;
        }

        @Override
        protected void onPostExecute(JSONArray moviesJsonDB) {
            super.onPostExecute(moviesJsonDB);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
}