package com.example.alexgaba.popularmovies;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}