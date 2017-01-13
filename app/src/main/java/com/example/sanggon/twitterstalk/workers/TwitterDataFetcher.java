package com.example.sanggon.twitterstalk.workers;

import android.util.Base64;
import android.util.Log;

import com.example.sanggon.twitterstalk.Constants;
import com.example.sanggon.twitterstalk.models.Tweet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class TwitterDataFetcher {

    private final static String TAG = "TwitterDataFetcher";

    private String bearerToken = null;
    private ArrayList<Tweet> mTweets = new ArrayList<>();

    /*
     *  Encode the consumer key and consumer secret as per Twitter API standard
     */
    private static String encodeKeys(String consumerKey, String consumerSecret) {
        try {
            String encodedConsumerKey = URLEncoder.encode(consumerKey, "UTF-8");
            String encodedConsumerSecret = URLEncoder.encode(consumerSecret, "UTF-8");

            String fullKey = encodedConsumerKey + ":" + encodedConsumerSecret;
            Log.i("encodeKeys", "fullKey = " + fullKey);

            return Base64.encodeToString(fullKey.getBytes(), Base64.NO_WRAP);
        } catch (UnsupportedEncodingException e) {
            return new String();
        }
    }

    /*
     *  Write request body to HTTP request
     */
    private static boolean writeRequest(HttpURLConnection connection, String textBody) {
        try {
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
            wr.write(textBody);
            wr.flush();
            wr.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /*
     *  Read response from HTTP response as a string
     */
    private static String readResponse(HttpURLConnection connection) {

        InputStream is = null;

        try {
            int responseCode = connection.getResponseCode();

            if (responseCode >= 400) {
                is = connection.getErrorStream();
            } else {
                is = connection.getInputStream();
            }

            StringBuilder str = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = "";

            while ((line = reader.readLine()) != null) {
                str.append(line + System.getProperty("line.separator"));
            }

            Log.i(TAG, "Response read = " + str.toString());
            return str.toString();
        } catch (IOException e) {
            Log.e(TAG, "IOException while reading response: " + e.getMessage());
            return new String();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException while closing InputStream in readResponse");
                }
            }
        }
    }

    /*
     *  Send a request to Twitter application-only authentication bearer token
     */
    public String requestBearerToken(String endpointUrl)
            throws IOException {

        if (bearerToken != null && bearerToken.equals("")) {
            // Do not make redundant authentication request
            return bearerToken;
        }

        HttpsURLConnection connection = null;
        String encodedCredentials = encodeKeys(Constants.TWITTER_API_KEY, Constants.TWITTER_API_SECRET);
        Log.i(TAG, "encodedCredentials: " + encodedCredentials);

        try {
            URL url = new URL(endpointUrl);
            connection = (HttpsURLConnection) url.openConnection();
            System.out.println(connection);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Host", "api.twitter.com");
            connection.setRequestProperty("User-Agent", "TwitterStalk");
            connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            connection.setRequestProperty("Content-Length", "29");
            connection.setUseCaches(false);

            writeRequest(connection, "grant_type=client_credentials");

            JSONObject obj = new JSONObject(readResponse(connection));

            String tokenType = (String) obj.get("token_type");
            String token = (String) obj.get("access_token");

            if (tokenType.equals("bearer")) {
                bearerToken = token;
            }

            return bearerToken;
        } catch (MalformedURLException e) {
            throw new IOException("Invalid endpoint URL specified.", e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /*
     *  Do fetch tweets. Duh.
     */
    public void fetchTweet(String endpointUrl)
            throws IOException {

        if (bearerToken == null || bearerToken.equals("")) {
            Log.e(TAG, "Attempting to fetch Twitter data without bearer token!");
            return;
        }

        HttpsURLConnection connection = null;

        try {
            URL url = new URL(endpointUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(false);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Host", "api.twitter.com");
            connection.setRequestProperty("User-Agent", "TwitterStalk");
            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
            connection.setUseCaches(false);

            JSONObject obj = new JSONObject(readResponse(connection));
            System.out.println("JSON obj = " + obj);

            JSONArray tweetsJsonArray = (JSONArray) obj.get("statuses");
            for (int i = 0; i < tweetsJsonArray.length(); ++i) {
                Log.i(TAG, "Inserted tweet json = " + tweetsJsonArray.getJSONObject(i));
                mTweets.add(new Tweet(tweetsJsonArray.getJSONObject(i)));
            }
        } catch (MalformedURLException e) {
            throw new IOException("Invalid endpoint URL specified", e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /*
     *  Return tweets list
     */
    public ArrayList<Tweet> getTweets() {
        return mTweets;
    }
}
