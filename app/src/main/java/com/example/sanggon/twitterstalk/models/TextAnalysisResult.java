package com.example.sanggon.twitterstalk.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.services.language.v1beta1.model.Entity;
import com.google.api.services.vision.v1.model.EntityAnnotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextAnalysisResult extends AnalysisResult
        implements Parcelable {

    public <T extends Map> TextAnalysisResult(List<T> annotations) {
        super(annotations);
        type = AnalysisType.TEXT;
    }

    public String toTitle() {
        return "Text Based Analysis";
    }

    public TextAnalysisResult(Parcel parcel) {
        super(parcel);
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
    }

    public static final Parcelable.Creator<TextAnalysisResult> CREATOR = new Parcelable.Creator<TextAnalysisResult>() {

        public TextAnalysisResult createFromParcel(Parcel parcel) {
            return new TextAnalysisResult(parcel);
        }

        public TextAnalysisResult[] newArray(int size) {
            return new TextAnalysisResult[size];
        }
    };
}