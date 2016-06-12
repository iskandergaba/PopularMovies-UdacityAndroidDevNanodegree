package com.example.alexgaba.popularmovies;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.alexgaba.popularmovies.Services.MoviesService;
import com.example.alexgaba.popularmovies.ViewAdapters.MoviesAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class MoviesFragment extends Fragment {

    public static final String TMDB_POPULAR_PARAM = "popular";
    public static final String TMDB_TOP_RATED_PARAM = "top_rated";
    public static final String TMDB_FAVORITE_PARAM = "favorite";
    public static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    public static final String TMDB_POSTER_SIZE_PARAM = "w500/";
    public static final String TMDB_TRAILERS_PARAM = "videos";
    public static final String TMDB_REVIEWS_PARAM = "reviews";
    public static final String TMDB_JSON_POSTER_KEY = "poster_path";
    public static final String TMDB_JSON_ID_KEY = "id";
    public static final int SELECT_POPULAR = 0;
    public static final int SELECT_TOP_RATED = 1;
    public static final int SELECT_FAVORITE = 2;

    public static boolean isContentUpdated = false;
    public static int mScrollPosition;
    public static GridView mPostersGrid;
    public static JSONArray mPopularJSONData;
    public static JSONArray mTopRatedJSONData;
    public static JSONArray mFavoriteJSONData;
    public static JSONArray mTrailersJSONData;
    public static JSONArray mReviewsJSONData;
    public static View rootView;

    public MoviesFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movies,
                container, false);
        rootView = view;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals(TMDB_POPULAR_PARAM) || key.equals(TMDB_TOP_RATED_PARAM)) {
                    updateGrid();
                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);

        try {
            mPopularJSONData = new JSONArray(prefs.getString(TMDB_POPULAR_PARAM, new JSONArray().toString()));
            mTopRatedJSONData = new JSONArray(prefs.getString(TMDB_TOP_RATED_PARAM, new JSONArray().toString()));
            mFavoriteJSONData = new JSONArray(prefs.getString(TMDB_FAVORITE_PARAM, new JSONArray().toString()));
            mTrailersJSONData = new JSONArray(prefs.getString(TMDB_TRAILERS_PARAM, new JSONArray().toString()));
            mReviewsJSONData = new JSONArray(prefs.getString(TMDB_REVIEWS_PARAM, new JSONArray().toString()));
            updateGrid();
            updateDB();
        } catch (ExecutionException | JSONException e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void onResume() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String sortParam = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default_value));
        try {
            if (isFavoriteChanged() && sortParam.equals(TMDB_FAVORITE_PARAM)) {
                updateDB();
            }
        } catch (JSONException | ExecutionException e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        mScrollPosition = mPostersGrid.getFirstVisiblePosition();
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.action_sort);
        Spinner spinner = (Spinner) item.getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(((AppCompatActivity)getActivity()).getSupportActionBar().getThemedContext(),
                R.array.action_sort_entries, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
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
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
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
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void updateDB() throws ExecutionException, JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String sortParam = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default_value));
        if (!isContentUpdated) {
            getActivity().startService(new Intent(getActivity(), MoviesService.class));
        }

        if (isFavoriteChanged()) {
            mFavoriteJSONData =  new JSONArray(prefs.getString(TMDB_FAVORITE_PARAM, new JSONArray().toString()));
            if (sortParam.equals(TMDB_FAVORITE_PARAM)) {
                updateGrid();
            }
        }
    }

    private void updateGrid() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        final String sortParam = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default_value));
        mPostersGrid = (GridView)rootView.findViewById(R.id.movies_gridview);
        try {
            mPopularJSONData = new JSONArray(prefs.getString(TMDB_POPULAR_PARAM, new JSONArray().toString()));
            mTopRatedJSONData = new JSONArray(prefs.getString(TMDB_TOP_RATED_PARAM, new JSONArray().toString()));
            mFavoriteJSONData = new JSONArray(prefs.getString(TMDB_FAVORITE_PARAM, new JSONArray().toString()));
            String[] posterThumbs = getPosters(sortParam);
            if (mPostersGrid != null) {
                mPostersGrid.setAdapter(new MoviesAdapter(getActivity(),  posterThumbs));
                mPostersGrid.setSelection(mScrollPosition);
                mPostersGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v,
                                            int position, long id) {
                        mScrollPosition = mPostersGrid.getFirstVisiblePosition();
                        String movie = null;
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

                        if (!MainActivity.mTwoPane) {
                            Intent detailIntent = new Intent(v.getContext(), DetailActivity.class);
                            detailIntent.putExtra(DetailFragment.MOVIE_PARAM, movie);
                            startActivity(detailIntent);
                        }

                        else {
                            TextView noMovie = (TextView)getActivity().findViewById(R.id.no_movie_text_view);
                            noMovie.setText("");
                            Bundle args = new Bundle();
                            args.putString(DetailFragment.MOVIE_PARAM, movie);
                            DetailFragment fragment = new DetailFragment();
                            fragment.setArguments(args);
                            getActivity().getFragmentManager().beginTransaction()
                                    .replace(R.id.detail_container, fragment)
                                    .commit();
                        }
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String[] getPosters(String sortParam) throws JSONException {

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

    private boolean isFavoriteChanged() throws JSONException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        JSONArray JSONData = new JSONArray(prefs.getString(TMDB_FAVORITE_PARAM, new JSONArray().toString()));
        return !mFavoriteJSONData.toString().equals(JSONData.toString());
    }
}