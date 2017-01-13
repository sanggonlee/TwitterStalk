package com.example.sanggon.twitterstalk.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sanggon.twitterstalk.models.CompositeAnalysisResult;
import com.example.sanggon.twitterstalk.models.TextAnalysisResult;
import com.example.sanggon.twitterstalk.services.HttpConnectionIntentService;
import com.example.sanggon.twitterstalk.R;
import com.example.sanggon.twitterstalk.helpers.Utils;
import com.example.sanggon.twitterstalk.adapters.ImageProgressAdapter;
import com.example.sanggon.twitterstalk.models.AnalysisResult;
import com.example.sanggon.twitterstalk.models.ImageAnalysisResult;
import com.example.sanggon.twitterstalk.models.Tweet;
import com.example.sanggon.twitterstalk.workers.TextAnalysisWorker;

import java.util.ArrayList;

import static com.example.sanggon.twitterstalk.models.AnalysisResult.AnalysisType.IMAGE_TEXT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String TAG = "MainActivity";

    private ResponseReceiver mReceiver;
    private IntentFilter mBroadcastFilter;
    private ArrayList<Tweet> mTweets = new ArrayList<>();

    private ImageProgressAdapter mImageProgressAdapter = new ImageProgressAdapter(MainActivity.this);

    /*      UI Components       */
    private EditText mTwitterUserIdEditText;

    private Button mButtonPicture;
    private Button mButtonText;
    private Button mButtonPictureAndText;

    private TextView mProgressText;

    private Dialog mAnalysisCompositionDialog;
    private SeekBarWrapper mSeekBar;
    private Button mAnalysisCompositionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(0xFFFFFFFF);
        setSupportActionBar(toolbar);

        initializeBroadcastReceiver();

        mTwitterUserIdEditText = (EditText) findViewById(R.id.userid_search_field);
        mTwitterUserIdEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().startsWith("@")) {
                    // Always prepend @ at the beginning of the user id search EditText
                    Utils.prependToEditText(mTwitterUserIdEditText, "@");
                }
            }
        });

        View rootView = findViewById(R.id.activity_main);
        rootView.setOnClickListener(this);

        mButtonPicture = (Button) findViewById(R.id.button_picture_based);
        mButtonPicture.setOnClickListener(this);

        mButtonText = (Button) findViewById(R.id.button_text_based);
        mButtonText.setOnClickListener(this);

        mButtonPictureAndText = (Button) findViewById(R.id.button_picture_and_text);
        mButtonPictureAndText.setOnClickListener(this);

        mProgressText = (TextView) findViewById(R.id.progress_text);

        initializeCompositeAnalysisUIs();
    }

    @Override
    protected void onResume() {
        registerReceiver(mReceiver, mBroadcastFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        // Unregister at onPause() instead of onStop() because onStop() might not be called when
        // the system does not have enough memory to process after onPause() is called
        //try {
            unregisterReceiver(mReceiver);
        //} catch (IllegalArgumentException e) {
            // Ignore
        //}
        super.onPause();
    }

    @Override
    protected void onStop() {
        clearProgressView();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_main_search:
                clearProgressView();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_picture_based:
                pictureBasedAnalysis();
                break;

            case R.id.button_text_based:
                textBasedAnalysis();
                break;

            case R.id.button_picture_and_text:
                showAnalysisCompositionDialog();
                break;

            case R.id.both_analysis_seekbar_button:
                compositeAnalysis();
                mAnalysisCompositionDialog.hide();
                break;
        }

        if (!(v instanceof EditText)) {
            hideKeyboard();
        }
    }

    public void hideKeyboard() {
        InputMethodManager manager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private void initializeBroadcastReceiver() {
        Log.i(TAG, "Initializing the BroadcastReceiver..");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ResponseReceiver.ACTION_TWITTER_FETCH_START);
        filter.addAction(ResponseReceiver.ACTION_TWITTER_FETCH_COMPLETE);
        filter.addAction(ResponseReceiver.ACTION_NO_TWEETS);
        filter.addAction(ResponseReceiver.ACTION_NO_IMAGES);
        filter.addAction(ResponseReceiver.ACTION_IMAGE_DOWNLOAD_START);
        filter.addAction(ResponseReceiver.ACTION_IMAGE_DOWNLOAD_COMPLETE);
        filter.addAction(ResponseReceiver.ACTION_VISION_ANALYSIS_START);
        filter.addAction(ResponseReceiver.ACTION_VISION_ANALYSIS_COMPLETE);
        filter.addAction(ResponseReceiver.ACTION_TEXT_ANALYSIS_START);
        filter.addAction(ResponseReceiver.ACTION_TEXT_ANALYSIS_COMPLETE);
        filter.addAction(ResponseReceiver.ACTION_COMPOSITE_ANALYSIS_START);
        filter.addAction(ResponseReceiver.ACTION_COMPOSITE_ANALYSIS_COMPLETE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        mBroadcastFilter = filter;
        mReceiver = new ResponseReceiver();
    }

    private void initializeCompositeAnalysisUIs() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.both_analysis_seekbar_dialog, (ViewGroup)findViewById(R.id.both_analysis_seekbar_dialog));

        mAnalysisCompositionDialog = new Dialog(this);
        mAnalysisCompositionDialog.setContentView(layout);

        mSeekBar = new SeekBarWrapper((SeekBar)layout.findViewById(R.id.both_analysis_seekbar));

        mAnalysisCompositionButton = (Button)layout.findViewById(R.id.both_analysis_seekbar_button);
        mAnalysisCompositionButton.setOnClickListener(this);
    }

    public void updateProgress(String msg) {
        Log.i(TAG, "update progress with text = " + msg);

        if (mProgressText.getVisibility() == View.GONE) {
            mProgressText.setVisibility(View.VISIBLE);
        }

        mProgressText.setText(msg);
        mProgressText.setVisibility(View.VISIBLE);

        hideButtons();
    }

    public void hideButtons() {
        mButtonPicture.setVisibility(View.GONE);
        mButtonText.setVisibility(View.GONE);
        mButtonPictureAndText.setVisibility(View.GONE);
    }

    public void showButtons() {
        mButtonPicture.setVisibility(View.VISIBLE);
        mButtonText.setVisibility(View.VISIBLE);
        mButtonPictureAndText.setVisibility(View.VISIBLE);
    }

    private void clearProgressView() {
        mProgressText.setVisibility(View.GONE);
        mImageProgressAdapter.clear();

        showButtons();
    }

    public void pictureBasedAnalysis() {
        String userId = mTwitterUserIdEditText.getText().toString();
        Log.i(TAG, "userId = " + userId);

        // Start a series of intent services to perform image based analysis
        HttpConnectionIntentService intentService = new HttpConnectionIntentService();
        intentService.startImageAnalysis(this, userId);
    }

    public void textBasedAnalysis() {
        String userId = mTwitterUserIdEditText.getText().toString();
        Log.i(TAG, "userId = " + userId);

        // Start a series of intent services to perform text based analysis
        HttpConnectionIntentService intentService = new HttpConnectionIntentService();
        intentService.startTextAnalysis(this, userId);
    }

    public void compositeAnalysis() {
        String userId = mTwitterUserIdEditText.getText().toString();

        // Start a series of intent services to perform image + text based analysis
        HttpConnectionIntentService intentService = new HttpConnectionIntentService();
        intentService.startCompositeAnalysis(this, userId);
    }

    public void showAnalysisCompositionDialog() {
        mAnalysisCompositionDialog.show();
    }

    public void startAnalysisActivity(AnalysisResult result) {
        Intent intent = new Intent();

        switch (result.getType()) {
            case IMAGE:
                intent.setClass(this, ImageAnalysisActivity.class);
                break;

            case TEXT:
                intent.setClass(this, TextAnalysisActivity.class);
                break;

            case IMAGE_TEXT:
                intent.setClass(this, CompositeAnalysisActivity.class);
                break;
        }

        intent.putExtra("analysis_data", result);
        startActivity(intent);
    }

    public class ResponseReceiver extends BroadcastReceiver {

        private final static String TAG = "ResponseReceiver";

        public final static String ACTION_TWITTER_FETCH_START = "ACTION_TWITTER_FETCH_START";
        public final static String ACTION_TWITTER_FETCH_COMPLETE = "ACTION_TWITTER_FETCH_COMPLETE";

        public final static String ACTION_NO_TWEETS = "NO_TWEETS";
        public final static String ACTION_NO_IMAGES = "NO_IMAGES";

        public final static String ACTION_IMAGE_DOWNLOAD_START = "ACTION_IMAGE_DOWNLOAD_START";
        public final static String ACTION_IMAGE_DOWNLOAD_COMPLETE = "ACTION_IMAGE_DOWNLOAD_COMPLETE";

        public final static String ACTION_VISION_ANALYSIS_START = "ACTION_VISION_ANALYSIS_START";
        public final static String ACTION_VISION_ANALYSIS_COMPLETE = "ACTION_VISION_ANALYSIS_COMPLETE";

        public final static String ACTION_TEXT_ANALYSIS_START = "ACTION_TEXT_ANALYSIS_START";
        public final static String ACTION_TEXT_ANALYSIS_COMPLETE = "ACTION_TEXT_ANALYSIS_COMPLETE";

        public final static String ACTION_COMPOSITE_ANALYSIS_START = "ACTION_COMPOSITE_ANALYSIS_START";
        public final static String ACTION_COMPOSITE_ANALYSIS_COMPLETE = "ACTION_COMPOSITE_ANALYSIS_COMPLETE";

        private Boolean mIsDoingComposite = false;
        private ImageAnalysisResult mImageResult;
        private TextAnalysisResult mTextResult;

        public ResponseReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "Received broadcast with action " + action);

            switch (action) {
                case ACTION_TWITTER_FETCH_START:
                    updateProgress(getResources().getString(R.string.twitter_fetch_start_message));
                    break;

                case ACTION_TWITTER_FETCH_COMPLETE:
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.twitter_fetch_complete_message), Toast.LENGTH_SHORT)
                            .show();
                    MainActivity.this.mTweets = intent.getParcelableArrayListExtra("tweets");
                    Log.i(TAG, "got " + mTweets.size() +" tweets");
                    break;

                case ACTION_NO_TWEETS:
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.twitter_no_tweets_message), Toast.LENGTH_LONG)
                            .show();
                    clearProgressView();
                    break;

                case ACTION_NO_IMAGES:
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.no_images_message), Toast.LENGTH_LONG)
                            .show();
                    break;

                case ACTION_IMAGE_DOWNLOAD_START:
                    Integer numImages = intent.getIntExtra("numImages", 0);
                    if (numImages == 0) {
                        Log.w(TAG, "Number of images to download is 0! Investigate!");
                        return;
                    }
                    updateProgress("Getting " + numImages + (numImages > 1 ? " images" : " image") + " total...");
                    break;

                case ACTION_IMAGE_DOWNLOAD_COMPLETE:
                    ArrayList<Bitmap> bitmaps = intent.getParcelableArrayListExtra("bitmaps");

                    mImageProgressAdapter = new ImageProgressAdapter(MainActivity.this);
                    mImageProgressAdapter.setBitmaps(bitmaps);

                    GridView gridView = (GridView) findViewById(R.id.images_grid);
                    gridView.setAdapter(mImageProgressAdapter);
                    gridView.setVisibility(View.VISIBLE);

                    Toast.makeText(MainActivity.this, "Completed getting images!", Toast.LENGTH_SHORT)
                            .show();
                    break;

                case ACTION_VISION_ANALYSIS_START:
                    updateProgress(getResources().getString(R.string.vision_analysis_start_message));
                    break;

                case ACTION_VISION_ANALYSIS_COMPLETE:
                    ImageAnalysisResult imageResult = intent.getParcelableExtra("vision_results");
                    mImageResult = imageResult;

                    if (!mIsDoingComposite) {
                        startAnalysisActivity(imageResult);
                    }

                    Toast.makeText(MainActivity.this, "Images analysis complete!", Toast.LENGTH_SHORT)
                            .show();
                    break;

                case ACTION_TEXT_ANALYSIS_START:
                    updateProgress(getString(R.string.text_analysis_start_message));
                    break;

                case ACTION_TEXT_ANALYSIS_COMPLETE:
                    TextAnalysisResult textResult = intent.getParcelableExtra("text_results");
                    mTextResult = textResult;

                    if (!mIsDoingComposite) {
                        startAnalysisActivity(textResult);
                    }

                    Toast.makeText(MainActivity.this, "Text analysis complete!", Toast.LENGTH_SHORT)
                        .show();
                    break;

                case ACTION_COMPOSITE_ANALYSIS_START:
                    mIsDoingComposite = true;
                    break;

                case ACTION_COMPOSITE_ANALYSIS_COMPLETE:
                    mIsDoingComposite = false;
                    CompositeAnalysisResult result = new CompositeAnalysisResult(mImageResult, mTextResult, mSeekBar.getValue());
                    startAnalysisActivity(result);
                    break;

                default:
                    Log.w(TAG, "Received an unidentifiable broadcast with action " + action);
            }
        }
    }

    public class SeekBarWrapper {
        private SeekBar seekbar;
        private Integer value = 0;

        public SeekBarWrapper(SeekBar seekbar) {

            if (seekbar == null) {
                Log.e(TAG, "SeekBar is null");
                return;
            }

            this.seekbar = seekbar;
            this.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    value = progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        public Integer getValue() {
            return value;
        }
    }
}
