package com.example.alexgaba.popularmovies.ViewAdapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.example.alexgaba.popularmovies.R;
import com.squareup.picasso.Picasso;
import static android.widget.GridView.AUTO_FIT;

public class MoviesAdapter extends BaseAdapter {
    private Context mContext;
    String[] mThumbIds;

    public MoviesAdapter(Context c, String[] mThumbIds) {
        mContext = c;
        this.mThumbIds = mThumbIds;
    }

    @Override
    public int getCount() {
        if (mThumbIds != null)
            return mThumbIds.length;
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(AUTO_FIT, (int)mContext.getResources().getDimension(R.dimen.main_poster_height)));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            imageView = (ImageView) convertView;
        }

        Picasso
                .with(mContext)
                .load(mThumbIds[position])
                .error(R.drawable.error_loading_poster)
                .into(imageView);

        return imageView;
    }
}
