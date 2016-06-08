package com.example.alexgaba.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;

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

import static android.widget.GridView.AUTO_FIT;


public class MainActivity extends AppCompatActivity {

    public static final String TMDB_MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
    public static final String TMDB_POPULAR_PARAM = "popular";
    public static final String TMDB_TOP_RATED_PARAM = "top_rated";
    public static final String TMDB_FAVORITE_PARAM = "favorite";
    public static final String TMDB_API_KEY = "?api_key=fb3915b06d0641541692779f202b518c";
    public static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    public static final String TMDB_POSTER_SIZE_PARAM = "w500/";
    public static final String TMDB_BACKDROP_SIZE_PARAM = "w780/";
    public static final String TMDB_JSON_RESULTS_KEY = "results";
    public static final String TMDB_JSON_POSTER_KEY = "poster_path";
    public static final String TMDB_JSON_BACKDROP_KEY = "backdrop_path";
    public static final String TMDB_JSON_TITLE_KEY = "original_title";
    public static final String TMDB_JSON_PLOT_KEY = "overview";
    public static final String TMDB_JSON_RATING_KEY = "vote_average";
    public static final String TMDB_JSON_RELEASE_DATE_KEY = "release_date";
    public static final String TMDB_JSON_ID_KEY = "id";
    public static final int SELECT_POPULAR = 0;
    public static final int SELECT_TOP_RATED = 1;
    public static final int SELECT_FAVORITE = 2;

    public static boolean isUpdated = false;
    public static int mScrollPosition;
    public static GridView mPostersGrid;
    public static JSONArray mPopularJSONData;
    public static JSONArray mTopRatedJSONData;
    public static JSONArray mFavoriteJSONData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateGrid();
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            mPopularJSONData = new JSONArray(prefs.getString(TMDB_POPULAR_PARAM, new JSONArray().toString()));
            mTopRatedJSONData = new JSONArray(prefs.getString(TMDB_TOP_RATED_PARAM, new JSONArray().toString()));
            mFavoriteJSONData = new JSONArray(prefs.getString(TMDB_FAVORITE_PARAM, new JSONArray().toString()));
            updateDB();
        } catch (ExecutionException | InterruptedException | JSONException e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    protected void onResume() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String sortParam = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default_value));
        try {
            if (isFavoriteChanged() && sortParam.equals(TMDB_FAVORITE_PARAM)) {
                updateDB();
            }
        } catch (JSONException  | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        mScrollPosition = mPostersGrid.getFirstVisiblePosition();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.action_sort);
        Spinner spinner = (Spinner) item.getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getSupportActionBar().getThemedContext(),
                R.array.action_sort_entries, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String pref = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default_value));

        switch (pref) {
            case TMDB_TOP_RATED_PARAM:
                spinner.setSelection(SELECT_TOP_RATED);
                break;
            case TMDB_FAVORITE_PARAM:
                spinner.setSelection(SELECT_FAVORITE);
                break;
            default:
                spinner.setSelection(SELECT_POPULAR);
                break;
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String pref = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default_value));
                SharedPreferences.Editor editor = prefs.edit();
                if (position == SELECT_POPULAR) {
                    if (!pref.equals(TMDB_POPULAR_PARAM)) {
                        mScrollPosition = 0;
                    }
                    editor.putString(getString(R.string.pref_sort_key), TMDB_POPULAR_PARAM);
                }
                else if (position == SELECT_TOP_RATED ) {
                    if (!pref.equals(TMDB_TOP_RATED_PARAM)) {
                        mScrollPosition = 0;
                    }
                    editor.putString(getString(R.string.pref_sort_key), TMDB_TOP_RATED_PARAM);
                }

                else if (position == SELECT_FAVORITE ) {
                    if (!pref.equals(TMDB_FAVORITE_PARAM)) {
                        mScrollPosition = 0;
                        try {
                            mFavoriteJSONData = new JSONArray(prefs.getString(TMDB_FAVORITE_PARAM, new JSONArray().toString()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    editor.putString(getString(R.string.pref_sort_key), TMDB_FAVORITE_PARAM);
                }
                editor.apply();
                updateGrid();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
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

    public void updateGrid() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        final String sortParam = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default_value));
        mPostersGrid = (GridView)findViewById(R.id.movies_gridview);
        try {
            String[] posterThumbs = getPosters(sortParam);
            if (mPostersGrid != null) {
                mPostersGrid.setAdapter(new ImageAdapter(this,  posterThumbs));
                mPostersGrid.setSelection(mScrollPosition);
                mPostersGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v,
                                            int position, long id) {
                        mScrollPosition = mPostersGrid.getFirstVisiblePosition();
                        String movie = null;
                        Log.v("sortparam", sortParam);
                        try {
                            switch (sortParam) {
                                case TMDB_TOP_RATED_PARAM:
                                    movie = mTopRatedJSONData.getJSONObject(position).toString();
                                    break;
                                case TMDB_FAVORITE_PARAM:
                                    movie = mFavoriteJSONData.getJSONObject(position).toString();
                                    break;
                                default:
                                    movie = mPopularJSONData.getJSONObject(position).toString();
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Intent detailIntent = new Intent(v.getContext(), DetailActivity.class);
                        detailIntent.putExtra("movie", movie);

                        startActivity(detailIntent);
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String[] getPosters(String sortParam) throws JSONException {

        JSONArray moviesDB;
        switch (sortParam) {
            case TMDB_TOP_RATED_PARAM:
                moviesDB = mTopRatedJSONData;
                break;
            case TMDB_FAVORITE_PARAM:
                moviesDB = mFavoriteJSONData;
                break;
            default:
                moviesDB = mPopularJSONData;
                break;
        }

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

    private void updateDB() throws ExecutionException, InterruptedException, JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String sortParam = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default_value));
        SharedPreferences.Editor editor = prefs.edit();
        if (!isUpdated) {
            FetchMoviesTask PopularMoviesTask = new FetchMoviesTask();
            JSONArray JSONData = PopularMoviesTask.execute(TMDB_POPULAR_PARAM).get();

             if (JSONData != null && !mPopularJSONData.toString().equals(JSONData.toString())) {
                mPopularJSONData = JSONData;
                editor.putString(TMDB_POPULAR_PARAM, mPopularJSONData.toString());
                if (sortParam.equals(TMDB_POPULAR_PARAM)) {
                    updateGrid();
                }
            }

            isUpdated = JSONData != null;

            FetchMoviesTask TopRatedMoviesTask = new FetchMoviesTask();
            JSONData = TopRatedMoviesTask.execute(TMDB_TOP_RATED_PARAM).get();

             if (JSONData != null && !mTopRatedJSONData.toString().equals(JSONData.toString())) {
                mTopRatedJSONData = JSONData;
                editor.putString(TMDB_TOP_RATED_PARAM, mTopRatedJSONData.toString());
                if (sortParam.equals(TMDB_TOP_RATED_PARAM)) {
                    updateGrid();
                }
            }
            editor.apply();
            isUpdated = isUpdated && JSONData != null;
        }

        if (isFavoriteChanged()) {
            mFavoriteJSONData =  new JSONArray(prefs.getString(TMDB_FAVORITE_PARAM, new JSONArray().toString()));
            if (sortParam.equals(TMDB_FAVORITE_PARAM)) {
                updateGrid();
            }
        }
    }

    private boolean isFavoriteChanged() throws JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        JSONArray JSONData = new JSONArray(prefs.getString(TMDB_FAVORITE_PARAM, new JSONArray().toString()));
        return !mFavoriteJSONData.toString().equals(JSONData.toString());
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
                imageView.setLayoutParams(new GridView.LayoutParams(AUTO_FIT, (int)getResources().getDimension(R.dimen.main_poster_height)));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                imageView = (ImageView) convertView;
            }

            Picasso
                    .with(mContext)
                    .load(mThumbIds[position])
                    .error(R.drawable.error_loading_poster)
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
                    moviesJsonDB = new JSONObject(moviesDB).getJSONArray(TMDB_JSON_RESULTS_KEY);

                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
            return moviesJsonDB;
        }
    }
}