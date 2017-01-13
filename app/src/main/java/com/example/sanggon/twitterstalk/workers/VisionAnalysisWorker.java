package com.example.sanggon.twitterstalk.workers;

import android.util.Log;

import com.example.sanggon.twitterstalk.Constants;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.common.collect.ImmutableList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static com.google.api.client.googleapis.auth.oauth2.GoogleCredential.getApplicationDefault;

public class VisionAnalysisWorker {

    private final static String TAG = "VisionAnalysisWorker";

    private Vision vision;

    /*
     *  Constructor
     */
    public VisionAnalysisWorker() {
        try {
            this.vision = getVisionService();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "GeneralSecurityException while initializing vision service");
        }
    }

    /*
     *  Initialize the Vision Service
     */
    private static Vision getVisionService()
            throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null)
                .setVisionRequestInitializer(new VisionRequestInitializer(Constants.GOOGLE_API_KEY))
                .setApplicationName("TwitterStalk");
        return builder.build();
    }

    /*
     *  Send request via Google Vision API
     */
    public ArrayList<EntityAnnotation> performVisionAnalysis(ArrayList<byte[]> byteArrays) {

        if (vision == null) {
            Log.w(TAG, "Vision has not been initialized");
            return null;
        }

        ArrayList<EntityAnnotation> results = new ArrayList<>();

        ImmutableList.Builder<AnnotateImageRequest> requestsBuilder = ImmutableList.builder();

        try {
            ArrayList<Feature> features = new ArrayList<>();
            features.add(new Feature()
                    .setMaxResults(20)
                    .setType("LABEL_DETECTION"));

            for (byte[] byteArray : byteArrays) {
                requestsBuilder.add(
                        new AnnotateImageRequest()
                                .setImage(new Image().encodeContent(byteArray))
                                .setFeatures(features));
            }

            Vision.Images.Annotate annotate = vision.images().annotate(
                    new BatchAnnotateImagesRequest().setRequests(requestsBuilder.build()));
            annotate.setDisableGZipContent(true);

            BatchAnnotateImagesResponse batchResponse = annotate.execute();

            List<AnnotateImageResponse> responses = batchResponse.getResponses();

            for (AnnotateImageResponse response : responses) {
                Log.i(TAG, response.toPrettyString());
                List<EntityAnnotation> labels = response.getLabelAnnotations();

                if (labels == null) {
                    Log.w(TAG, "Empty results!");
                    continue;
                }

                for (EntityAnnotation label : labels) {
                    Log.i(TAG, "Score: " + label.getScore() + " , Description: " + label.getDescription());
                    results.add(label);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }
}
