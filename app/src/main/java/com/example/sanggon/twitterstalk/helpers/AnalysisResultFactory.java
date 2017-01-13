package com.example.sanggon.twitterstalk.helpers;

import android.util.Log;

import com.example.sanggon.twitterstalk.models.AnalysisResult;
import com.example.sanggon.twitterstalk.models.ImageAnalysisResult;
import com.example.sanggon.twitterstalk.models.TextAnalysisResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnalysisResultFactory {

    private final static String TAG = "AnalysisResultFactory";

    public static <T extends Map> AnalysisResult createAnalysisResult(
            AnalysisResult.AnalysisType type, List<T> annotations, String twitterId, Integer numTweets, Integer numEntities) {

        AnalysisResult result;

        switch (type) {
            case IMAGE:
                result = new ImageAnalysisResult(annotations, numEntities);
                break;
            case TEXT:
                result = new TextAnalysisResult(annotations);
                break;
            default:
                Log.w(TAG, "Unidentified Analysis Type");
                result = new AnalysisResult();
                break;
        }

        result.setTwitterId(twitterId);
        result.setNumTweets(numTweets);
        return result;
    }

    public static <T extends Map> AnalysisResult createAnalysisResult(
            AnalysisResult.AnalysisType type, List<T> annotations, String twitterId, Integer numTweets) {

         return createAnalysisResult(type, annotations, twitterId, numTweets, 0);

    }
}
