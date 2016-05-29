package com.example.alexgaba.popularmovies;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String backDropURL = getIntent().getExtras().getString("backDropURL");
        String posterURL = getIntent().getStringExtra("posterURL");
        String movieTitle = getIntent().getExtras().getString("movieTitle");
        String plotSynopsis = getIntent().getExtras().getString("plotSynopsis");
        String rating = getIntent().getExtras().getString("rating");
        String releaseDate = getIntent().getExtras().getString("releaseDate");
        ImageView poster = (ImageView)findViewById(R.id.detail_poster_imageview);
        TextView movieTitleTextView = (TextView)findViewById(R.id.detail_title_textview);
        movieTitleTextView.setText(movieTitle);
        TextView plotSynopsisTextView = (TextView)findViewById(R.id.detail_plot_textview);
        plotSynopsisTextView.setText(plotSynopsis);
        TextView ratingTextView = (TextView)findViewById(R.id.detail_rating_textview);
        ratingTextView.setText(rating);
        TextView releaseDateTextView = (TextView)findViewById(R.id.detail_release_date_textview);
        releaseDateTextView.setText(releaseDate);

        final RelativeLayout backDrop = (RelativeLayout)findViewById(R.id.backdrop_layout);

        Picasso
                .with(this)
                .load(backDropURL)
                .resize(600, 256)
                .centerCrop()
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            backDrop.setBackgroundDrawable(new BitmapDrawable(getApplicationContext().getResources(), bitmap));
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        Toast.makeText(getApplicationContext(), "Failed Loading", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });

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
