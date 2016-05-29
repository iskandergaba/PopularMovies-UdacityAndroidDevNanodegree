package com.example.alexgaba.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {

    public static final String TMDB_MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
    public static final String TMDB_POPULAR_PARAM = "popular";
    public static final String TMDB_TOP_RATED_PARAM = "top_rated";
    public static final String TMDB_API_KEY = "?api_key=fb3915b06d0641541692779f202b518c";
    public static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    public static final String TMDB_POSTER_SIZE_PARAM = "w500/";
    public static final String TMDB_BACKDROP_SIZE_PARAM = "w600/";
    public static final String TMDB_JSON_RESULTS_KEY = "results";
    public static final String TMDB_JSON_POSTER_KEY = "poster_path";
    public static final String TMDB_JSON_BACKDROP_KEY = "backdrop_path";
    public static final String TMDB_JSON_TITLE_KEY = "original_title";
    public static final String TMDB_JSON_PLOT_KEY = "overview";
    public static final String TMDB_JSON_RATING_KEY = "vote_average";
    public static final String TMDB_JSON_RELEASE_DATE_KEY = "release_date";
    public static JSONArray TMDB_POPULAR = null;
    public static JSONArray TMDB_TOP_RATED = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openOptionsMenu();
        setContentView(R.layout.activity_main);
        try {
            updateDB();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String sortParam = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default_value));

        try {
            String[] posterThumbs = getPosters(sortParam);
            GridView gridView = (GridView) findViewById(R.id.movies_gridview);
            if (gridView != null) {
                gridView.setAdapter(new ImageAdapter(this,  posterThumbs));
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v,
                                            int position, long id) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        String sortParam = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default_value));
                        Log.v("fuck", sortParam);
                        JSONObject movie;
                        String backDropURL = TMDB_IMAGE_BASE_URL + TMDB_BACKDROP_SIZE_PARAM;
                        String posterURL = TMDB_IMAGE_BASE_URL + TMDB_POSTER_SIZE_PARAM;
                        String movieTitle = null;
                        String plotSynopsis = null;
                        String rating = null;
                        String releaseDate = null;
                        try {
                            if (sortParam.equals(TMDB_TOP_RATED_PARAM))
                                movie = TMDB_TOP_RATED.getJSONObject(position);
                            else
                                movie = TMDB_POPULAR.getJSONObject(position);

                            backDropURL += movie.getString(TMDB_JSON_BACKDROP_KEY).substring(1);
                            posterURL += movie.getString(TMDB_JSON_POSTER_KEY).substring(1);
                            movieTitle = movie.getString(TMDB_JSON_TITLE_KEY);
                            plotSynopsis = movie.getString(TMDB_JSON_PLOT_KEY);
                            rating = movie.getString(TMDB_JSON_RATING_KEY);
                            releaseDate = movie.getString(TMDB_JSON_RELEASE_DATE_KEY);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Intent detailIntent = new Intent(v.getContext(), DetailActivity.class);
                        detailIntent.putExtra("backDropURL", backDropURL);
                        detailIntent.putExtra("posterURL", posterURL);
                        detailIntent.putExtra("movieTitle", movieTitle);
                        detailIntent.putExtra("plotSynopsis", plotSynopsis);
                        detailIntent.putExtra("rating", rating);
                        detailIntent.putExtra("releaseDate", releaseDate);
                        startActivity(detailIntent);
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String[] getPosters(String sortParam) throws JSONException {

        JSONArray moviesDB;
        if (sortParam.equals(TMDB_TOP_RATED_PARAM))
            moviesDB = TMDB_TOP_RATED;

        else
            moviesDB = TMDB_POPULAR;

        ArrayList<String> urls = new ArrayList<>();
        if (moviesDB != null) {
            for (int i = 0; i < moviesDB.length(); i++) {
                JSONObject movie = moviesDB.getJSONObject(i);
                String url = TMDB_IMAGE_BASE_URL + TMDB_POSTER_SIZE_PARAM + movie.getString(TMDB_JSON_POSTER_KEY).substring(1);
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
            if (mThumbIds != null)
                return mThumbIds.length;
            return 0;
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
                imageView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, (int)getResources().getDimension(R.dimen.main_poster_height)));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
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
                StringBuilder buffer = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
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
                    }
                }
                try {
                    moviesJsonDB = new JSONObject(moviesDB).getJSONArray(TMDB_JSON_RESULTS_KEY);

                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
            return moviesJsonDB;
        }
    }
}