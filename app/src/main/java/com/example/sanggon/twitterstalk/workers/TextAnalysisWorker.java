package com.example.sanggon.twitterstalk.workers;

import android.util.Log;

import com.example.sanggon.twitterstalk.Constants;
import com.example.sanggon.twitterstalk.models.Tweet;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.language.v1beta1.CloudNaturalLanguage;
import com.google.api.services.language.v1beta1.CloudNaturalLanguageRequest;
import com.google.api.services.language.v1beta1.CloudNaturalLanguageRequestInitializer;
import com.google.api.services.language.v1beta1.model.AnalyzeEntitiesRequest;
import com.google.api.services.language.v1beta1.model.AnalyzeEntitiesResponse;
import com.google.api.services.language.v1beta1.model.AnnotateTextRequest;
import com.google.api.services.language.v1beta1.model.Document;
import com.google.api.services.language.v1beta1.model.Entity;
import com.google.api.services.language.v1beta1.model.EntityMention;
import com.google.api.services.vision.v1.model.Feature;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TextAnalysisWorker {

    private final static String TAG = "TextAnalysisWorker";

    private String bulkText;
    private CloudNaturalLanguage language;
    //private BlockingQueue<AnalyzeEntitiesRequest> requests = new ArrayBlockingQueue<>(3);

    /**
     * Constructor
     */
    public TextAnalysisWorker() {
        language = getLanguageService();
    }

    /**
     * Get the the Google Cloud Natural Language API
     */
    public CloudNaturalLanguage getLanguageService() {

        if (language != null) return language;

        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        return new CloudNaturalLanguage.Builder(httpTransport, jsonFactory, null)
                .setCloudNaturalLanguageRequestInitializer(new CloudNaturalLanguageRequestInitializer(Constants.GOOGLE_API_KEY))
                .setApplicationName("TwitterStalk")
                .build();
    }

    /**
     * Prepare to run the service by setting tweet texts
     * Appends all the texts into a single string
     */
    public void feedTweets(ArrayList<Tweet> tweets) {

        StringBuilder stringBuilder = new StringBuilder();
        for (Tweet tweet : tweets) {
            String text = tweet.getText();
            if (!text.equals("")) {
                stringBuilder
                        .append(text)
                        .append(System.getProperty("line.separator"));
            }
        }

        bulkText = stringBuilder.toString();
    }

    /**
     * Get the combined text
     */
    public String getBulkText() {
        return bulkText;
    }

    public List<Entity> performTextAnalysis() {
        if (language == null) {
            Log.w(TAG, "Google Cloud Natural Language service not initialized!");
            return null;
        }

        if (bulkText.trim() == "") {
            Log.w(TAG, "Text to process is empty.");
            return null;
        }

        List<Entity> entities = new ArrayList<>();

        try {
            CloudNaturalLanguageRequest<AnalyzeEntitiesResponse> request = language
                    .documents()
                    .analyzeEntities(new AnalyzeEntitiesRequest()
                        .setDocument(new Document()
                            .setContent(bulkText)
                            .setType("PLAIN_TEXT")));

            AnalyzeEntitiesResponse response = request.execute();

            entities = response.getEntities();
            for (Entity entity : entities) {
                Log.i(TAG, "name: " + entity.getName());
                Log.i(TAG, "mentions: ");
                for (EntityMention mention : entity.getMentions()) {
                    Log.i(TAG, "    " + mention.getText());
                }
                Log.i(TAG, "salience: " + entity.getSalience());
                Log.i(TAG, "type: " + entity.getType());
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException while executing text analysis");
            e.printStackTrace();
        }

        return entities;
    }
}
