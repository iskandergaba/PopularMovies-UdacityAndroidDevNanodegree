package com.example.alexgaba.popularmovies;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    private static final String APP_SHARE_HASHTAG = "\n\nShared by #PopularMoviesApp";
    private JSONObject mMovie = null;
    private String mMovieDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            mMovie = new JSONObject(getIntent().getExtras().getString("movie"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String backDropURL = MainActivity.TMDB_IMAGE_BASE_URL + MainActivity.TMDB_BACKDROP_SIZE_PARAM;
        String posterURL = MainActivity.TMDB_IMAGE_BASE_URL + MainActivity.TMDB_POSTER_SIZE_PARAM;
        String movieTitle = null;
        String plotSynopsis = null;
        String rating = null;
        String releaseDate = null;
        String Id = null;
        try {
            backDropURL += mMovie.getString(MainActivity.TMDB_JSON_BACKDROP_KEY).substring(1);
            posterURL += mMovie.getString(MainActivity.TMDB_JSON_POSTER_KEY).substring(1);
            movieTitle = mMovie.getString(MainActivity.TMDB_JSON_TITLE_KEY);
            plotSynopsis = mMovie.getString(MainActivity.TMDB_JSON_PLOT_KEY);
            rating = mMovie.getString(MainActivity.TMDB_JSON_RATING_KEY) + "/10";
            releaseDate = mMovie.getString(MainActivity.TMDB_JSON_RELEASE_DATE_KEY);
            Id = mMovie.getString(MainActivity.TMDB_JSON_ID_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ImageView poster = (ImageView)findViewById(R.id.detail_poster_imageview);
        ImageView backDrop = (ImageView)findViewById(R.id.detail_backdrop_imageview);
        TextView movieTitleTextView = (TextView)findViewById(R.id.detail_title_textview);
        if (movieTitleTextView != null)
            movieTitleTextView.setText(movieTitle);

        TextView plotSynopsisTextView = (TextView)findViewById(R.id.detail_plot_textview);
        if (plotSynopsisTextView != null)
            plotSynopsisTextView.setText(plotSynopsis);

        TextView ratingTextView = (TextView)findViewById(R.id.detail_rating_textview);
        if (ratingTextView != null)
            ratingTextView.setText(rating);

        TextView releaseDateTextView = (TextView)findViewById(R.id.detail_release_date_textview);
        if (releaseDateTextView != null)
            releaseDateTextView.setText(releaseDate);

        mMovieDetails = "Movie Title: " + movieTitle + "\n\nPlot Synopsis: " + plotSynopsis + "\n\nRating: " + rating + "\n\nRelease Date: " + releaseDate;

        Picasso
                .with(this)
                .load(backDropURL)
                .error(R.drawable.error_loading_backdrop)
                .into(backDrop);

        Picasso
                .with(this)
                .load(posterURL)
                .error(R.drawable.error_loading_poster)
                .into(poster);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu, menu);
        MenuItem favorite = menu.getItem(0);
        try {
            updateFavoriteMenuItem(favorite);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_share:
                Intent shareMovieIntent = new Intent(Intent.ACTION_SEND);
                shareMovieIntent.setType("text/plain");
                shareMovieIntent.putExtra(Intent.EXTRA_TEXT, mMovieDetails + APP_SHARE_HASHTAG);
                startActivity(shareMovieIntent);
                return true;

            case R.id.action_favorite:
                try {
                    if (isFavorite()) {
                        removeFavorite();
                    } else {
                        addFavorite();
                    }
                    updateFavoriteMenuItem(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    private boolean isFavorite() throws JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        JSONArray favorite = new JSONArray(prefs.getString(MainActivity.TMDB_FAVORITE_PARAM, new JSONArray().toString()));
        for (int i = 0; i < favorite.length(); i++) {
            JSONObject fMovie = favorite.getJSONObject(i);
            if (fMovie.toString().equals(mMovie.toString())) {
                return true;
            }
        }
        return false;
    }

    private void addFavorite() throws JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        JSONArray favorite = new JSONArray(prefs.getString(MainActivity.TMDB_FAVORITE_PARAM, new JSONArray().toString()));
        ArrayList<JSONObject> fList = new ArrayList<>();

        for (int i = 0; i < favorite.length(); i++){
            fList.add(favorite.getJSONObject(i));
        }
        fList.add(mMovie);
        favorite = new JSONArray(fList);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(MainActivity.TMDB_FAVORITE_PARAM, favorite.toString());
        editor.apply();

        CharSequence text = "\"" + mMovie.getString(MainActivity.TMDB_JSON_TITLE_KEY) + "\"" +
                " has been added to favorite";
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void removeFavorite() throws JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        JSONArray favorite = new JSONArray(prefs.getString(MainActivity.TMDB_FAVORITE_PARAM, new JSONArray().toString()));
        ArrayList<JSONObject> fList = new ArrayList<>();

        for (int i = 0; i < favorite.length(); i++){
            fList.add(favorite.getJSONObject(i));
        }

        for (int i = 0; i < fList.size(); i++) {
            JSONObject fMovie = favorite.getJSONObject(i);
            if (fMovie.toString().equals(mMovie.toString())) {
                fList.remove(i);
                break;
            }
        }

        favorite = new JSONArray(fList);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(MainActivity.TMDB_FAVORITE_PARAM, favorite.toString());
        editor.apply();

        CharSequence text = "\"" + mMovie.getString(MainActivity.TMDB_JSON_TITLE_KEY) + "\"" +
                " has been removed from favorite";
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void updateFavoriteMenuItem(MenuItem item) throws JSONException {
        if (isFavorite()) {
            item.setIcon(R.drawable.action_favorite_on);
            item.setTitle(R.string.action_remove_favorite);
        } else {
            item.setIcon(R.drawable.action_favorite_off);
            item.setTitle(R.string.action_add_favorite);
        }
    }
}