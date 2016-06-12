package com.example.alexgaba.popularmovies;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

public class DetailFragment extends Fragment {

    private static final String APP_SHARE_HASHTAG = "\n\nShared by #PopularMoviesApp";
    private static final String TMDB_RESULTS_PARAM = "results";
    public static final String TMDB_JSON_BACKDROP_KEY = "backdrop_path";
    public static final String TMDB_JSON_TITLE_KEY = "original_title";
    public static final String TMDB_JSON_PLOT_KEY = "overview";
    public static final String TMDB_JSON_RATING_KEY = "vote_average";
    public static final String TMDB_JSON_RELEASE_DATE_KEY = "release_date";
    public static final String TMDB_JSON_ID_KEY = "id";
    private static final String TMDB_TRAILER_URL_PARAM = "key";
    private static final String TMDB_REVIEW_URL_PARAM = "url";
    private static final String TMDB_TITLE_PARAM = "name";
    private static final String TMDB_AUTHOR_PARAM = "author";
    private static final String YOUTUBE_BASE_URL = "http://www.youtube.com/watch?v=";
    public static final String TMDB_BACKDROP_SIZE_PARAM = "w780/";
    private JSONObject mMovie = null;
    private String mMovieDetails;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail,
                container, false);
        try {
            mMovie = new JSONObject(getArguments().getString("movie"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        buildLayout(view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detail_menu, menu);
        MenuItem favorite = menu.findItem(R.id.action_favorite);
        try {
            updateFavoriteMenuItem(favorite);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String sortParam = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default_value));
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
                    if (MainActivity.mTwoPane && sortParam.equals(MoviesFragment.TMDB_FAVORITE_PARAM)) {
                        Bundle args = new Bundle();
                        args.putString("movie", mMovie.toString());
                        DetailFragment fragment = new DetailFragment();
                        fragment.setArguments(args);
                        getActivity().getFragmentManager().beginTransaction()
                                .replace(R.id.detail_container, fragment)
                                .commit();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void buildLayout(View rootView) {

        String backDropURL = MoviesFragment.TMDB_IMAGE_BASE_URL + TMDB_BACKDROP_SIZE_PARAM;
        String posterURL = MoviesFragment.TMDB_IMAGE_BASE_URL + MoviesFragment.TMDB_POSTER_SIZE_PARAM;
        String movieTitle = null;
        String plotSynopsis = null;
        String rating = null;
        String releaseDate = null;
        String Id = null;
        try {
            backDropURL += mMovie.getString(TMDB_JSON_BACKDROP_KEY).substring(1);
            posterURL += mMovie.getString(MoviesFragment.TMDB_JSON_POSTER_KEY).substring(1);
            movieTitle = mMovie.getString(TMDB_JSON_TITLE_KEY);
            plotSynopsis = mMovie.getString(TMDB_JSON_PLOT_KEY);
            rating = mMovie.getString(TMDB_JSON_RATING_KEY) + "/10";
            releaseDate = mMovie.getString(TMDB_JSON_RELEASE_DATE_KEY);
            Id = mMovie.getString(TMDB_JSON_ID_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ImageView poster = (ImageView)rootView.findViewById(R.id.detail_poster_imageview);
        ImageView backDrop = (ImageView)rootView.findViewById(R.id.detail_backdrop_imageview);
        TextView movieTitleTextView = (TextView)rootView.findViewById(R.id.detail_title_textview);
        if (movieTitleTextView != null)
            movieTitleTextView.setText(movieTitle);

        TextView plotSynopsisTextView = (TextView)rootView.findViewById(R.id.detail_plot_textview);
        if (plotSynopsisTextView != null)
            plotSynopsisTextView.setText(plotSynopsis);

        TextView ratingTextView = (TextView)rootView.findViewById(R.id.detail_rating_textview);
        if (ratingTextView != null)
            ratingTextView.setText(rating);

        TextView releaseDateTextView = (TextView)rootView.findViewById(R.id.detail_release_date_textview);
        if (releaseDateTextView != null)
            releaseDateTextView.setText(releaseDate);

        final ArrayList<String> mTrailerURLs = new ArrayList<>();
        ArrayList<String> mTitles = new ArrayList<>();
        JSONArray trailersJSON = null;
        try {
            for (int i = 0; i < MoviesFragment.mTrailersJSONData.length(); i++) {
                String curId = MoviesFragment.mTrailersJSONData.getJSONObject(i).getString(TMDB_JSON_ID_KEY);
                if (curId.equals(Id)) {
                    trailersJSON = MoviesFragment.mTrailersJSONData.getJSONObject(i).getJSONArray(TMDB_RESULTS_PARAM);
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

        TextView TrailersTextView = (TextView)rootView.findViewById(R.id.trailers_title_text_view);
        if (TrailersTextView != null)
            TrailersTextView.setText(MessageFormat.format("Trailers ({0})", mTitles.size()));

        ListView mTrailers = (ListView)rootView.findViewById(R.id.trailers_list_view);
        if (mTrailers != null) {
            mTrailers.setAdapter(new MoviesDataAdapter(getActivity(), mTitles, R.drawable.ic_play));
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
            for (int i = 0; i < MoviesFragment.mReviewsJSONData.length(); i++) {
                String curId = MoviesFragment.mReviewsJSONData.getJSONObject(i).getString(TMDB_JSON_ID_KEY);
                if (curId.equals(Id)) {
                    reviewsJSON = MoviesFragment.mReviewsJSONData.getJSONObject(i).getJSONArray(TMDB_RESULTS_PARAM);
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

        TextView ReviewsTextView = (TextView)rootView.findViewById(R.id.reviews_title_text_view);
        if (ReviewsTextView != null)
            ReviewsTextView.setText(MessageFormat.format("Reviews ({0})", mAuthors.size()));

        ListView mReviews = (ListView)rootView.findViewById(R.id.reviews_list_view);
        if (mReviews != null) {
            mReviews.setAdapter(new MoviesDataAdapter(getActivity(), mAuthors, R.drawable.ic_review));
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
                .with(getActivity())
                .load(backDropURL)
                .error(R.drawable.error_loading_backdrop)
                .into(backDrop);

        Picasso
                .with(getActivity())
                .load(posterURL)
                .error(R.drawable.error_loading_poster)
                .into(poster);
    }

    private boolean isFavorite() throws JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        JSONArray favorite = new JSONArray(prefs.getString(MoviesFragment.TMDB_FAVORITE_PARAM, new JSONArray().toString()));
        for (int i = 0; i < favorite.length(); i++) {
            JSONObject fMovie = favorite.getJSONObject(i);
            if (fMovie.toString().equals(mMovie.toString())) {
                return true;
            }
        }
        return false;
    }

    private void addFavorite() throws JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        JSONArray favorite = new JSONArray(prefs.getString(MoviesFragment.TMDB_FAVORITE_PARAM, new JSONArray().toString()));
        ArrayList<JSONObject> fList = new ArrayList<>();

        for (int i = 0; i < favorite.length(); i++){
            fList.add(favorite.getJSONObject(i));
        }
        fList.add(mMovie);
        favorite = new JSONArray(fList);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(MoviesFragment.TMDB_FAVORITE_PARAM, favorite.toString());
        editor.apply();

        CharSequence text = "\"" + mMovie.getString(TMDB_JSON_TITLE_KEY) + "\"" +
                " has been added to favorite";
        Toast toast = Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void removeFavorite() throws JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        JSONArray favorite = new JSONArray(prefs.getString(MoviesFragment.TMDB_FAVORITE_PARAM, new JSONArray().toString()));
        ArrayList<JSONObject> fList = new ArrayList<>();

        for (int i = 0; i < favorite.length(); i++){
            JSONObject fMovie = favorite.getJSONObject(i);
            if (!fMovie.toString().equals(mMovie.toString())) {
                fList.add(fMovie);
            }
        }

        favorite = new JSONArray(fList);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(MoviesFragment.TMDB_FAVORITE_PARAM, favorite.toString());
        editor.apply();

        CharSequence text = "\"" + mMovie.getString(TMDB_JSON_TITLE_KEY) + "\"" +
                " has been removed from favorite";
        Toast toast = Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_SHORT);
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