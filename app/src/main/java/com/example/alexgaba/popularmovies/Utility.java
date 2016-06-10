package com.example.alexgaba.popularmovies;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Utility {
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem instanceof ViewGroup) {
                listItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public static JSONArray JsonAddObjectToArray(JSONArray array, JSONObject object) throws JSONException {
        ArrayList<JSONObject> list = new ArrayList<>();

        for (int i = 0; i < array.length(); i++){
            list.add(array.getJSONObject(i));
        }

        list.add(object);
        return new JSONArray(list);
    }

    public static JSONArray JsonRemoveObjectFromArray(JSONArray array, JSONObject object) throws JSONException {
        ArrayList<JSONObject> list = new ArrayList<>();

        for (int i = 0; i < array.length(); i++){
            JSONObject obj = array.getJSONObject(i);
            if (!obj.toString().equals(object.toString())) {
                list.add(obj);
            }
        }

        return new JSONArray(list);
    }

    public static boolean isMovieExisting(String movieId, JSONArray movieDB) throws JSONException {
        for (int i = 0; i < movieDB.length(); i++) {
            String id = movieDB.getJSONObject(i).getString(MoviesFragment.TMDB_JSON_ID_KEY);
            if (movieId.equals(id)) {
                return true;
            }
        }
        return false;
    }
}