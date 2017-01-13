package com.example.sanggon.twitterstalk.helpers;

import android.graphics.Bitmap;
import android.text.Selection;
import android.widget.EditText;

import com.example.sanggon.twitterstalk.models.AnalysisSegment;
import com.example.sanggon.twitterstalk.models.CompositeAnalysisResult;
import com.example.sanggon.twitterstalk.models.ImageAnalysisResult;
import com.example.sanggon.twitterstalk.models.TextAnalysisResult;
import com.google.api.services.language.v1beta1.model.Entity;
import com.google.api.services.vision.v1.model.EntityAnnotation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 *  Utils class which contains multiple utility functions
 *  Simulates a static class
 */
public final class Utils {

    private Utils() {
        // Make the constructor private to prevent it from being instantiated
    }

    /**
     * Prepend str to the beginning of the text in the editText
     * @param editText
     * @param str
     */
    public static void prependToEditText(EditText editText, String str) {
        editText.setText(str);
        Selection.setSelection(editText.getText(), editText.getText().length());
    }

    /**
     * Converts a Bitmap to a byte array
     * @param bitmap
     * @return byte array
     */
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, blob);
        return blob.toByteArray();
    }

    /**
     * Format the label segment to a human friendly string
     * @param num
     * @param description
     * @param score
     * @return
     */
    public static String formatSegment(Integer num, String description, Float score) {
        return String.valueOf(num) + ". " + description + " (" + score * 100 + "%)";
    }

    /**
     * Convert a Google API Entitiy object or EntityAnnotation object into AnalysisSegment
     */
    public static <T extends Map>AnalysisSegment convertAnnotationsToSegments(T anno) {
        String descr = null;
        Float score = null;

        if (anno instanceof Entity) {
            descr = ((Entity)anno).getName();
            score = ((Entity)anno).getSalience();
        }
        else if (anno instanceof EntityAnnotation) {
            descr = ((EntityAnnotation)anno).getDescription();
            score = ((EntityAnnotation)anno).getScore();
        }

        return new AnalysisSegment(descr, score);
    }
}
