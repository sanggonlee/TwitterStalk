package com.example.sanggon.twitterstalk.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

public class ImageProgressAdapter extends BaseAdapter{

    private Context mContext;
    private ArrayList<Bitmap> mBitmaps;

    public ImageProgressAdapter(Context c) {
        mContext = c;
    }

    public void setBitmaps(ArrayList<Bitmap> bitmaps) {
        mBitmaps = bitmaps;
    }

    public int getCount() {
        return mBitmaps.size();
    }

    public Object getItem(int position) {
        return mBitmaps.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void clear() {
        if (mBitmaps != null) {
            mBitmaps.clear();
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(mContext);
        }
        else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap(mBitmaps.get(position));
        return imageView;
    }
}
