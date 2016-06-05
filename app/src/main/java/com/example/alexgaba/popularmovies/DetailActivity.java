package com.example.alexgaba.popularmovies;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

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

        Picasso
                .with(this)
                .load(backDropURL)
                .into(backDrop);

        Picasso
                .with(this)
                .load(posterURL)
                .into(poster);
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
        super.onBackPressed();
    }

}