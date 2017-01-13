package com.example.sanggon.twitterstalk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.sanggon.twitterstalk.R;
import com.example.sanggon.twitterstalk.helpers.Utils;
import com.example.sanggon.twitterstalk.models.AnalysisSegment;
import com.example.sanggon.twitterstalk.models.ImageAnalysisResult;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.data;

public class BaseAnalysisActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
