package com.example.sanggon.twitterstalk.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Tweet
        implements Parcelable {
    private String id;
    private String createdAt;
    private String text;
    private ArrayList<String> mediaUrls;

    public Tweet(JSONObject json) {
        try {
            this.createdAt = json.getString("created_at");
            this.id = json.getString("id");
            this.text = json.getString("text");

            this.mediaUrls = new ArrayList<>();

            JSONObject entitiesJson = json.getJSONObject("entities");
            if (entitiesJson != null && entitiesJson.has("media")) {
                JSONArray mediaJsonArray = entitiesJson.getJSONArray("media");
                for (int i = 0; i < mediaJsonArray.length(); ++i) {
                    Log.i("tweet, media=", "" + mediaJsonArray.getJSONObject(i));
                    mediaUrls.add(mediaJsonArray.getJSONObject(i).getString("media_url"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Tweet(Parcel parcel) {
        id = parcel.readString();
        createdAt = parcel.readString();
        text = parcel.readString();
        mediaUrls = (ArrayList<String>) parcel.readSerializable();
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public ArrayList<String> getMediaUrls() {
        return mediaUrls;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(createdAt);
        parcel.writeString(text);
        parcel.writeSerializable(mediaUrls);
    }

    @Override
    public int describeContents() {
        return this.hashCode();
    }

    public static final Parcelable.Creator<Tweet> CREATOR = new Parcelable.Creator<Tweet>() {

        public Tweet createFromParcel(Parcel parcel) {
            return new Tweet(parcel);
        }

        public Tweet[] newArray(int size) {
            return new Tweet[size];
        }
    };
}
