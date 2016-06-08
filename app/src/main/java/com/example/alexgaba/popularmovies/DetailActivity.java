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

import java.util.HashSet;
import java.util.Set;

public class DetailActivity extends AppCompatActivity {

    private static final String APP_SHARE_HASHTAG = "\n\nShared by #PopularMoviesApp";
    private String mMovieDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String backDropURL = getIntent().getExtras().getString("backDropURL");
        String posterURL = getIntent().getStringExtra("posterURL");
        String movieTitle = getIntent().getExtras().getString("movieTitle");
        String plotSynopsis = getIntent().getExtras().getString("plotSynopsis");
        String rating = getIntent().getExtras().getString("rating");
        String releaseDate = getIntent().getExtras().getString("releaseDate");
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
        updateFavoriteMenuItem(favorite);
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
                if (isFavorite()) {
                    removeFavorite();
                } else {
                    addFavorite();
                }
                updateFavoriteMenuItem(item);
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

    private boolean isFavorite() {
        String Id = getIntent().getExtras().getString("Id");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Set<String> favorite = new HashSet<>(prefs.getStringSet(MainActivity.FAVORITE_KEY, new HashSet<String>()));
        return favorite.contains(Id);
    }

    private void addFavorite() {
        String Id = getIntent().getExtras().getString("Id");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Set<String> favorite = new HashSet<>(prefs.getStringSet(MainActivity.FAVORITE_KEY, new HashSet<String>()));
        SharedPreferences.Editor editor = prefs.edit();
        favorite.add(Id);
        editor.putStringSet(MainActivity.FAVORITE_KEY, favorite);
        editor.apply();

        CharSequence text = "\"" + getIntent().getStringExtra("movieTitle") + "\"" +
                " has been added to favorite";
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void removeFavorite() {
        String Id = getIntent().getExtras().getString("Id");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Set<String> favorite = new HashSet<>(prefs.getStringSet(MainActivity.FAVORITE_KEY, new HashSet<String>()));
        SharedPreferences.Editor editor = prefs.edit();
        favorite.remove(Id);
        editor.putStringSet(MainActivity.FAVORITE_KEY, favorite);
        editor.apply();

        CharSequence text = "\"" + getIntent().getStringExtra("movieTitle") + "\"" +
                " has been removed from favorite";
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void updateFavoriteMenuItem(MenuItem item) {
        if (isFavorite()) {
            item.setIcon(R.drawable.action_favorite_on);
            item.setTitle(R.string.action_remove_favorite);
        } else {
            item.setIcon(R.drawable.action_favorite_off);
            item.setTitle(R.string.action_add_favorite);
        }
    }
}