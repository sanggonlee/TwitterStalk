package com.example.sanggon.twitterstalk.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.sanggon.twitterstalk.helpers.Utils;
import com.google.api.services.vision.v1.model.EntityAnnotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AnalysisResult
        implements Parcelable {

    public enum AnalysisType {
        IMAGE,
        TEXT,
        IMAGE_TEXT
    }

    protected AnalysisType type;
    protected HashMap<String, Float> segments;
    protected String twitterId;
    protected Integer numTweets;

    public AnalysisResult() {
    }

    public <T extends Map>AnalysisResult(List<T> annotations) {
        segments = new HashMap<>();

        Float totalScore = 0F;

        for (int i = 0; i < annotations.size(); ++i) {
            AnalysisSegment a = Utils.convertAnnotationsToSegments(annotations.get(i));

            String key = a.getDescription();
            Float score = a.getScore();

            if (segments.containsKey(key)) {
                segments.put(key, segments.get(key) + score);
            }
            else {
                segments.put(key, a.getScore());
            }

            totalScore += score;
        }

        if (totalScore < 0.00001F) {    // Shouldn't ever happen though..
            segments.clear();
        }
    }

    public void setTwitterId(String id) {
        this.twitterId = id;
    }

    public String getTwitterId() {
        return twitterId;
    }

    public void setNumTweets(Integer numTweets) {
        this.numTweets = numTweets;
    }

    public Integer getNumTweets() {
        return numTweets;
    }

    public HashMap<String, Float> getSegments() {
        return segments;
    }

    public AnalysisResult(Parcel parcel) {
        type = (AnalysisType)parcel.readSerializable();
        Bundle bundle = parcel.readBundle();
        System.out.println("reading parcel, bundle = " + bundle);
        segments = (HashMap<String, Float>) bundle.getSerializable("segments");
        System.out.println("reading parcel, segments = " + segments);
        twitterId = parcel.readString();
        numTweets = parcel.readInt();
    }

    public AnalysisType getType() {
        return type;
    }

    public List<AnalysisSegment> getSegmentsAsList() {
        List<AnalysisSegment> arr = new ArrayList<>();
        for (Map.Entry<String, Float> entry : segments.entrySet()) {
            arr.add(new AnalysisSegment(entry.getKey(), entry.getValue()));
        }

        Collections.sort(arr, new Comparator<AnalysisSegment>() {
            @Override
            public int compare(AnalysisSegment lhs, AnalysisSegment rhs) {
                Float diff = rhs.getScore() - lhs.getScore();
                if (diff == 0) return 0;
                else if (diff < 0) return -1;
                else return 1;
            }
        });

        if (arr.size() > 10) {
            arr = arr.subList(0, 10);
        }

        return arr;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeSerializable(type);
        Bundle bundle = new Bundle();
        bundle.putSerializable("segments", segments);
        parcel.writeBundle(bundle);
        parcel.writeString(twitterId);
        parcel.writeInt(numTweets);
    }

    @Override
    public int describeContents() {
        return this.hashCode();
    }

    public static final Parcelable.Creator<AnalysisResult> CREATOR = new Parcelable.Creator<AnalysisResult>() {

        public AnalysisResult createFromParcel(Parcel parcel) {
            return new AnalysisResult(parcel);
        }

        public AnalysisResult[] newArray(int size) {
            return new AnalysisResult[size];
        }
    };
}
