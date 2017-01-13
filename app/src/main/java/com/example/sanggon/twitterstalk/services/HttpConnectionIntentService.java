package com.example.sanggon.twitterstalk.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.sanggon.twitterstalk.helpers.AnalysisResultFactory;
import com.example.sanggon.twitterstalk.Constants;
import com.example.sanggon.twitterstalk.helpers.Utils;
import com.example.sanggon.twitterstalk.activities.MainActivity;
import com.example.sanggon.twitterstalk.models.AnalysisResult;
import com.example.sanggon.twitterstalk.models.ImageAnalysisResult;
import com.example.sanggon.twitterstalk.models.TextAnalysisResult;
import com.example.sanggon.twitterstalk.models.Tweet;
import com.example.sanggon.twitterstalk.workers.ImageDownloader;
import com.example.sanggon.twitterstalk.workers.TextAnalysisWorker;
import com.example.sanggon.twitterstalk.workers.TwitterDataFetcher;
import com.example.sanggon.twitterstalk.workers.VisionAnalysisWorker;
import com.google.api.services.language.v1beta1.model.Entity;
import com.google.api.services.vision.v1.model.EntityAnnotation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.action;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
public class HttpConnectionIntentService extends IntentService {
    private static final String TAG = "HCIntentService";

    public static final String ACTION_TWITTER_FETCH = "TWITTER_FETCH";
    public static final String ACTION_IMAGE_DOWNLOAD = "IMAGE_DOWNLOAD";
    public static final String ACTION_VISION_ANALYSIS = "ACTION_VISION_ANALYSIS";
    public static final String ACTION_TEXT_ANALYSIS = "ACTION_TEXT_ANALYSIS";
    public static final String ACTION_COMPOSITE_ANALYSIS = "ACTION_COMPOSITE_ANALYSIS";

    public static final String TWITTER_USER_ID = "TWITTER_USER_ID";

    String mTwitterId;
    ArrayList<Tweet> mTweets = new ArrayList<>();
    ArrayList<Bitmap> mBitmaps = new ArrayList<>();

    public HttpConnectionIntentService() {
        super("HttpConnectionIntentService");
    }

    /**
     *  Start the service to pipeline image based analysis
     */
    public void startImageAnalysis(Context context, String twitterUserId) {

        // First fetch tweets data
        startActionTwitterFetch(context, twitterUserId);

        // Then start downloading images from the urls
        startActionImageDownload(context, twitterUserId);

        // Perform vision analysis through Google Vision API
        startActionVisionAnalysis(context);

    }

    /**
     *  Start the service to pipeline text based anaylsis
     */
    public void startTextAnalysis(Context context, String twitterUserId) {

        // First fetch tweets data
        startActionTwitterFetch(context, twitterUserId);

        // Perform text analysis
        startActionTextAnalysis(context);

    }

    /**
     * Start the service to pipepline image + text based analysis
     */
    public void startCompositeAnalysis(Context context, String twitterUserId) {

        // First fetch tweets data
        startActionTwitterFetch(context, twitterUserId);

        // Start downloading images from urls
        startActionImageDownload(context, twitterUserId);

        // Perform composite analysis
        startActionCompositeAnalysis(context);
    }

    /**
     * Starts this service to perform action TWITTER_FETCH with the given parameters. If
     * the service is already performing a task this action will be queued.
     */
    public void startActionTwitterFetch(Context context, String twitterUserId) {
        if (twitterUserId.equals(mTwitterId) && mTweets.size() > 0) {
            Log.i(TAG, "Skipping twitter fetch because we have tweets available from past session");
            return;
        }

        Intent intent = new Intent(context, HttpConnectionIntentService.class);
        intent.setAction(ACTION_TWITTER_FETCH);
        intent.putExtra(TWITTER_USER_ID, twitterUserId);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action IMAGE_DOWNLOAD with the given parameters. If
     * the service is already performing a task this action will be queued.
     */
    public void startActionImageDownload(Context context, String twitterUserId) {
        Intent intent = new Intent(context, HttpConnectionIntentService.class);
        intent.setAction(ACTION_IMAGE_DOWNLOAD);
        intent.putExtra(TWITTER_USER_ID, twitterUserId);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action VISION_ANALYSIS. If
     * the service is already performing a task this action will be queued.
     */
    public void startActionVisionAnalysis(Context context) {
        Intent intent = new Intent(context, HttpConnectionIntentService.class);
        intent.setAction(ACTION_VISION_ANALYSIS);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action TEXT_ANALYSIS. If the service is
     * already performing a task this action will be queued.
     */
    public void startActionTextAnalysis(Context context) {
        Intent intent = new Intent(context, HttpConnectionIntentService.class);
        intent.setAction(ACTION_TEXT_ANALYSIS);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action COMPOSITE_ANALYSIS. If the service is
     * already performing a task this action will be queued.
     */
    public void startActionCompositeAnalysis(Context context) {
        Intent intent = new Intent(context, HttpConnectionIntentService.class);
        intent.setAction(ACTION_COMPOSITE_ANALYSIS);
        context.startService(intent);
    }

    /**
     *  Invoke the corresponding service
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent");
        if (intent != null) {

            switch (intent.getAction()) {

                case ACTION_TWITTER_FETCH:
                    final String twitterUserId = intent.getStringExtra(TWITTER_USER_ID);
                    mTwitterId = twitterUserId;
                    handleActionTwitterFetch(twitterUserId);
                    break;

                case ACTION_IMAGE_DOWNLOAD:
                    handleActionImageDownload();
                    break;

                case ACTION_VISION_ANALYSIS:
                    handleActionVisionAnalysis();
                    break;

                case ACTION_TEXT_ANALYSIS:
                    handleActionTextAnalysis();
                    break;

                case ACTION_COMPOSITE_ANALYSIS:
                    handleActionCompositeAnalysis();
                    break;

                default:
                    Log.w(TAG, "Unidentified action " + action + " to handle. Ignoring.");

            }
        }
    }

    /**
     * Handle action Twitter Fetch in the provided background thread with the provided
     * parameters.
     */
    private void handleActionTwitterFetch(String twitterUserId) {
        Log.i(TAG, "starting data fetching for user id = " + twitterUserId);
        broadcastWithTweets(new Intent(), MainActivity.ResponseReceiver.ACTION_TWITTER_FETCH_START, mTweets);

        try {
            TwitterDataFetcher twitterDataFetcher = new TwitterDataFetcher();
            twitterDataFetcher.requestBearerToken(Constants.TWITTER_TOKEN_URL);
            twitterDataFetcher.fetchTweet("https://api.twitter.com/1.1/search/tweets.json?q=from:" + twitterUserId);
            mTweets = twitterDataFetcher.getTweets();
        } catch (IOException e) {
            Log.e(TAG, "IOException while handling TWITTER_FETCH action");
            e.printStackTrace();
        }

        if (mTweets.size() == 0) {
            broadcastResult(new Intent(), MainActivity.ResponseReceiver.ACTION_NO_TWEETS);
        }
        else {
            broadcastWithTweets(new Intent(), MainActivity.ResponseReceiver.ACTION_TWITTER_FETCH_COMPLETE, mTweets);
        }
    }

    /**
     * Handle action Image Download in the provided background thread with the provided
     * parameters.
     */
    private void handleActionImageDownload() {
        Log.i(TAG, "handleActionImageDownload");

        ArrayList<String> urls = new ArrayList<>();
        for (Tweet tweet : mTweets) {
            for (String url : tweet.getMediaUrls()) {
                urls.add(url);
            }
        }

        if (urls.size() == 0) {
            broadcastResult(new Intent(), MainActivity.ResponseReceiver.ACTION_NO_IMAGES);
            return;
        }

        // Broadcast that image download is starting
        Intent messageStartIntent = new Intent();
        messageStartIntent.putExtra("numImages", urls.size());
        broadcastResult(messageStartIntent, MainActivity.ResponseReceiver.ACTION_IMAGE_DOWNLOAD_START);

        // Perform download
        ImageDownloader imageDownloader = new ImageDownloader(urls);
        ArrayList<Bitmap> bitmaps = imageDownloader.downloadImages();
        mBitmaps = bitmaps;

        // Broadcast that the image downloading is complete
        broadcastWithBitmaps(new Intent(), MainActivity.ResponseReceiver.ACTION_IMAGE_DOWNLOAD_COMPLETE, bitmaps);
    }

    /**
     * Handle vision analysis action
     */
    private void handleActionVisionAnalysis() {
        Log.i(TAG, "handleActionVisionAnalysis");

        if (mBitmaps.size() == 0) {
            return;
        }

        broadcastResult(new Intent(), MainActivity.ResponseReceiver.ACTION_VISION_ANALYSIS_START);

        ArrayList<byte[]> imageByteArrays = new ArrayList<>();
        for (Bitmap bitmap : mBitmaps) {
            imageByteArrays.add(Utils.bitmapToByteArray(bitmap));
        }
        VisionAnalysisWorker worker = new VisionAnalysisWorker();
        ArrayList<EntityAnnotation> results = worker.performVisionAnalysis(imageByteArrays);
        ImageAnalysisResult computed = (ImageAnalysisResult) AnalysisResultFactory.createAnalysisResult(
                AnalysisResult.AnalysisType.IMAGE, results, mTwitterId, mTweets.size(), mBitmaps.size());

        Intent intent = new Intent();
        intent.putExtra("vision_results", computed);
        broadcastResult(intent, MainActivity.ResponseReceiver.ACTION_VISION_ANALYSIS_COMPLETE);
    }

    /**
     * Handle text analysis action
     */
    private void handleActionTextAnalysis() {
        Log.i(TAG, "handleActionTextAnalysis");

        TextAnalysisWorker worker = new TextAnalysisWorker();
        worker.feedTweets(mTweets);

        if (worker.getBulkText().trim().equals("")) {
            return;
        }

        broadcastResult(new Intent(), MainActivity.ResponseReceiver.ACTION_TEXT_ANALYSIS_START);

        List<Entity> results = worker.performTextAnalysis();
        TextAnalysisResult computed = (TextAnalysisResult) AnalysisResultFactory.createAnalysisResult(
                AnalysisResult.AnalysisType.TEXT, results, mTwitterId, mTweets.size());

        Intent intent = new Intent();
        intent.putExtra("text_results", computed);

        broadcastResult(intent, MainActivity.ResponseReceiver.ACTION_TEXT_ANALYSIS_COMPLETE);
    }

    /**
     * Handle composite analysis action
     */
    private void handleActionCompositeAnalysis() {
        broadcastResult(new Intent(), MainActivity.ResponseReceiver.ACTION_COMPOSITE_ANALYSIS_START);

        handleActionVisionAnalysis();

        handleActionTextAnalysis();

        broadcastResult(new Intent(), MainActivity.ResponseReceiver.ACTION_COMPOSITE_ANALYSIS_COMPLETE);
    }

    /**
     *  Broadcast the start/end of the asynchronous action
     *  @action: name of the action of the broadcast
     */
    private void broadcastResult(Intent intent, String action) {
        Log.i(TAG, "sending broadcast " + action);
        intent.setAction(action);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(intent);
    }

    /**
     *  Broadcast with list of tweets
     */
    private void broadcastWithTweets(Intent intent, String action, ArrayList<Tweet> tweets) {
        Log.i(TAG, "sending " + tweets.size() + " tweets");
        intent.putExtra("tweets", tweets);
        broadcastResult(intent, action);
    }

    /**
     *  Broadcast with list of bitmaps
     */
    private void broadcastWithBitmaps(Intent intent, String action, ArrayList<Bitmap> bitmaps) {
        intent.putExtra("bitmaps", bitmaps);
        broadcastResult(intent, action);
    }
}
