package com.example.alexgaba.popularmovies;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alexgaba.popularmovies.ViewAdapters.MoviesDataAdapter;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    private static final String APP_SHARE_HASHTAG = "\n\nShared by #PopularMoviesApp";
    private static final String TMDB_RESULTS_PARAM = "results";
    private static final String TMDB_TRAILER_URL_PARAM = "key";
    private static final String TMDB_REVIEW_URL_PARAM = "url";
    private static final String TMDB_TITLE_PARAM = "name";
    private static final String TMDB_AUTHOR_PARAM = "author";
    private static final String YOUTUBE_BASE_URL = "http://www.youtube.com/watch?v=";
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

        buildLayout();
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

    private void buildLayout() {

        String backDropURL = MainActivity.TMDB_IMAGE_BASE_URL + MainActivity.TMDB_BACKDROP_SIZE_PARAM;
        String posterURL = MainActivity.TMDB_IMAGE_BASE_URL + MainActivity.TMDB_POSTER_SIZE_PARAM;
        String movieTitle = null;
        String plotSynopsis = null;
        String rating = null;
        String releaseDate = null;
        String Id = null;
        try {
            mMovie = new JSONObject(getIntent().getExtras().getString("movie"));
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

        final ArrayList<String> mTrailerURLs = new ArrayList<>();
        ArrayList<String> mTitles = new ArrayList<>();
        JSONArray trailersJSON = null;
        try {
            for (int i = 0; i < MainActivity.mTrailersJSONData.length(); i++) {
                String curId = MainActivity.mTrailersJSONData.getJSONObject(i).getString(MainActivity.TMDB_JSON_ID_KEY);
                if (curId.equals(Id)) {
                    trailersJSON = MainActivity.mTrailersJSONData.getJSONObject(i).getJSONArray(TMDB_RESULTS_PARAM);
                    break;
                }
            }

            if (trailersJSON != null) {
                for (int i = 0; i < trailersJSON.length(); i++) {
                    JSONObject trailer = trailersJSON.getJSONObject(i);
                    mTrailerURLs.add(YOUTUBE_BASE_URL + trailer.getString(TMDB_TRAILER_URL_PARAM));
                    mTitles.add(trailer.getString(TMDB_TITLE_PARAM));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView TrailersTextView = (TextView)findViewById(R.id.trailers_title_text_view);
        if (TrailersTextView != null)
            TrailersTextView.setText(MessageFormat.format("Trailers ({0})", mTitles.size()));

        ListView mTrailers = (ListView)findViewById(R.id.trailers_list_view);
        if (mTrailers != null) {
            mTrailers.setAdapter(new MoviesDataAdapter(this, mTitles, R.drawable.ic_play));
            Utility.setListViewHeightBasedOnChildren(mTrailers);
            mTrailers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mTrailerURLs.get(position))));
                }
            });
        }

        final ArrayList<String> mAuthors = new ArrayList<>();
        final ArrayList<String> mReviewURLs = new ArrayList<>();
        JSONArray reviewsJSON = null;
        try {
            for (int i = 0; i < MainActivity.mReviewsJSONData.length(); i++) {
                String curId = MainActivity.mReviewsJSONData.getJSONObject(i).getString(MainActivity.TMDB_JSON_ID_KEY);
                if (curId.equals(Id)) {
                    reviewsJSON = MainActivity.mReviewsJSONData.getJSONObject(i).getJSONArray(TMDB_RESULTS_PARAM);
                    break;
                }
            }

            if (reviewsJSON != null) {
                for (int i = 0; i < reviewsJSON.length(); i++) {
                    JSONObject review = reviewsJSON.getJSONObject(i);
                    mAuthors.add("Review by: " + review.getString(TMDB_AUTHOR_PARAM));
                    mReviewURLs.add(review.getString(TMDB_REVIEW_URL_PARAM));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView ReviewsTextView = (TextView)findViewById(R.id.reviews_title_text_view);
        if (ReviewsTextView != null)
            ReviewsTextView.setText(MessageFormat.format("Reviews ({0})", mAuthors.size()));

        ListView mReviews = (ListView)findViewById(R.id.reviews_list_view);
        if (mReviews != null) {
            mReviews.setAdapter(new MoviesDataAdapter(this, mAuthors, R.drawable.ic_review));
            Utility.setListViewHeightBasedOnChildren(mReviews);
            mReviews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mReviewURLs.get(position))));
                }
            });
        }

        mMovieDetails = "Movie Title: " + movieTitle + "\n\nPlot Synopsis: " + plotSynopsis +
                "\n\nRating: " + rating + "\n\nRelease Date: " + releaseDate;

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
            JSONObject fMovie = favorite.getJSONObject(i);
            if (!fMovie.toString().equals(mMovie.toString())) {
                fList.add(fMovie);
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