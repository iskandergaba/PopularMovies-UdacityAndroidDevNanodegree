package com.example.alexgaba.popularmovies.ViewAdapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alexgaba.popularmovies.R;

import java.util.ArrayList;

public class MoviesDataAdapter extends ArrayAdapter<String> {

    private final int iconResId;

    public MoviesDataAdapter(Activity mContext, ArrayList<String> mLabels, int iconResId) {
        super(mContext, 0, mLabels);
        this.iconResId = iconResId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String mLabel = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.movie_data_list_view_item, parent, false);
        }

        ImageView icon = (ImageView) convertView.findViewById(R.id.icon_image_view);
        icon.setImageResource(iconResId);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.label_text_view);
        txtTitle.setText(mLabel);
        return convertView;
    }
}