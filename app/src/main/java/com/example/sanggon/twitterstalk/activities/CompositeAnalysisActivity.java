package com.example.sanggon.twitterstalk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.sanggon.twitterstalk.R;
import com.example.sanggon.twitterstalk.helpers.Utils;
import com.example.sanggon.twitterstalk.models.AnalysisSegment;
import com.example.sanggon.twitterstalk.models.CompositeAnalysisResult;

import java.util.ArrayList;
import java.util.List;

public class CompositeAnalysisActivity extends BaseAnalysisActivity {

    private final static String TAG = "CompositeAnalysisActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_composite_analysis);

        ArrayList<String> labels = new ArrayList<>();

        Intent intent = getIntent();
        CompositeAnalysisResult data = intent.getParcelableExtra("analysis_data");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(0xFFFFFFFF);
        toolbar.setTitle(data.toTitle());
        setSupportActionBar(toolbar);

        TextView userIdView = (TextView)findViewById(R.id.composite_analysis_userid);
        userIdView.setText(getString(R.string.prepended_twitter_id, data.getTwitterId()));

        TextView numEntitiesView = (TextView)findViewById(R.id.composite_analysis_num_entities);
        numEntitiesView.setText(getString(R.string.composite_analysis_num_entities, data.getNumImages(), data.getNumTweets()));

        List<AnalysisSegment> segments = data.getSegmentsAsList();
        for (int i = 0; i < segments.size(); ++i) {
            AnalysisSegment seg = segments.get(i);
            labels.add(Utils.formatSegment(i+1, seg.getDescription(), seg.getScore()));
        }

        ListView labelList = (ListView)findViewById(R.id.composite_analysis_listview);
        labelList.setDivider(null);
        labelList.setAdapter(new ArrayAdapter<>(
                CompositeAnalysisActivity.this, android.R.layout.simple_gallery_item, labels));
    }
}
