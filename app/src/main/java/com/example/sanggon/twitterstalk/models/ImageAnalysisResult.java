package com.example.sanggon.twitterstalk.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.services.vision.v1.model.EntityAnnotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageAnalysisResult extends AnalysisResult
        implements Parcelable {

    Integer numImages = 0;

    public <T extends Map> ImageAnalysisResult(List<T> annotations, Integer numImages) {
        super(annotations);
        type = AnalysisType.IMAGE;
        this.numImages = numImages;
    }

    public void setNumImages(Integer n) {
        numImages = n;
    }

    public Integer getNumImages() {
        return numImages;
    }

    public String toTitle() {
        return "Image Based Analysis";
    }

    public ImageAnalysisResult(Parcel parcel) {
        super(parcel);
        numImages = parcel.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeInt(numImages);
    }

    public static final Parcelable.Creator<ImageAnalysisResult> CREATOR = new Parcelable.Creator<ImageAnalysisResult>() {

        public ImageAnalysisResult createFromParcel(Parcel parcel) {
            return new ImageAnalysisResult(parcel);
        }

        public ImageAnalysisResult[] newArray(int size) {
            return new ImageAnalysisResult[size];
        }
    };
}
