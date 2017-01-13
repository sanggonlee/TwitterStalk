package com.example.sanggon.twitterstalk.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class CompositeAnalysisResult extends AnalysisResult {

    private Integer numImages;

    public CompositeAnalysisResult(ImageAnalysisResult imageResult, TextAnalysisResult textResult, Integer ratio) {

        this.type = AnalysisType.IMAGE_TEXT;
        this.numTweets = textResult.numTweets;
        this.numImages = imageResult.numImages;
        this.segments = new HashMap<>();

        if (!imageResult.twitterId.equals(textResult.twitterId)) {
            System.out.println("CompositeAnalysisResult with two different Twitter IDs. Investigate!");
        }
        this.twitterId = imageResult.getTwitterId();

        float imageRatio = (100 - ratio) / 100F;
        float textRatio = ratio / 100F;

        for (Map.Entry<String, Float> entry : imageResult.getSegments().entrySet()) {
            segments.put(entry.getKey(), entry.getValue() * imageRatio);
        }

        for (Map.Entry<String, Float> entry : textResult.getSegments().entrySet()) {
            String key = entry.getKey();
            Float val = entry.getValue();

            if (segments.containsKey(key)) {
                segments.put(key, (val + segments.get(key)) * textRatio);
            }
            else {
                segments.put(key, val * textRatio);
            }
        }
    }

    public String toTitle() {
        return "Image + Text Based Analysis";
    }

    public Integer getNumImages() {
        return numImages;
    }

    public CompositeAnalysisResult(Parcel parcel) {
        super(parcel);
        numImages = parcel.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeInt(numImages);
    }

    public static final Parcelable.Creator<CompositeAnalysisResult> CREATOR = new Parcelable.Creator<CompositeAnalysisResult>() {

        public CompositeAnalysisResult createFromParcel(Parcel parcel) {
            return new CompositeAnalysisResult(parcel);
        }

        public CompositeAnalysisResult[] newArray(int size) {
            return new CompositeAnalysisResult[size];
        }
    };
}
