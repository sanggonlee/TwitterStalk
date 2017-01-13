package com.example.sanggon.twitterstalk.workers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ImageDownloader {

    private final static String TAG = "ImageDownloader";

    ArrayList<String> urls = new ArrayList<>();
    ArrayList<Bitmap> bitmaps = new ArrayList<>();

    public ImageDownloader(ArrayList<String> urls) {
        this.urls = urls;
    }

    public ArrayList<Bitmap> downloadImages() {
        for (String urlStr : urls) {
            Log.i(TAG, "starting download image for url: " + urlStr);
            InputStream istream = null;
            try {
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                istream = connection.getInputStream();

                Bitmap bitmap = BitmapFactory.decodeStream(istream);
                Log.i(TAG, "Received payload of " + bitmap.getByteCount() + " bytes");
                bitmap = bitmap.createScaledBitmap(bitmap, 150, 150, true);   // fix the size of the bitmaps

                bitmaps.add(bitmap);
            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException: " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "IOException: " + e.getMessage());
            } finally {
                if (istream != null) {
                    try {
                        istream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "IOException while closing InputStream: " + e.getMessage());
                    }
                }
            }
        }

        return bitmaps;
    }
}
