package com.viableindustries.waterreporter;

import android.content.Intent;
import android.os.Build;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.ShareActionProvider;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Submission;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Ryan Hamley on 10/28/14.
 * Activity displays detailed information on your submissions after choosing
 * one in the submissions list.
 */
public class SubmissionDetailActivity extends ActionBarActivity {
    @InjectView(R.id.report_label) TextView reportText;
    @InjectView(R.id.activity_label) TextView activityText;
    @InjectView(R.id.date_label) TextView dateText;
    @InjectView(R.id.comments_label) TextView commentsText;
    @InjectView(R.id.submission_image) ImageView imageView;

    private Submission submission;
    private String mDateStr;
    private String[] pollutionTypes = {"Pollution Type", "Discolored water", "Eroded stream banks", "Excessive algae",
            "Excessive trash", "Exposed soil", "Faulty construction entryway", "Faulty silt fences",
            "Fish kill", "Foam", "Livestock in stream", "Oil and grease", "Other", "Pipe Discharge",
            "Sewer overflow", "Stormwater", "Winter manure application"};
    private String[] activityTypes = {"Activity Type", "Canoeing", "Diving", "Fishing", "Flatwater kayaking", "Hiking",
            "Living the dream", "Rock climbing", "Sailing", "Scouting wildlife", "Snorkeling",
            "Stand-up paddleboarding", "Stream cleanup", "Surfing", "Swimming", "Tubing", "Water Skiing",
            "Whitewater kayaking", "Whitewater rafting"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_detail);

        ButterKnife.inject(this);

        Intent intent = getIntent();
        if(intent != null && intent.hasExtra("SubmissionId")){
            long id = intent.getLongExtra("SubmissionId", 0);
            submission = Submission.findById(Submission.class, id);

            String mTypeStr, mActivityStr;
            int activityId = Integer.parseInt(submission.activity.replaceAll("\\D+",""));

            if(submission.type.equals("[{\"id\":1}]")){
                mTypeStr = "Activity Report";
                mActivityStr = activityTypes[activityId];
            } else {
                mTypeStr = "Pollution Report";
                mActivityStr = pollutionTypes[activityId];
            }

            reportText.setText(mTypeStr);
            activityText.setText(mActivityStr);
            mDateStr = submission.date;
            dateText.setText("Submitted on " + mDateStr);
            commentsText.setText(submission.comments);
            if(submission.photoPath != null) {
                Picasso.with(this).load(new File(submission.photoPath)).into(imageView);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.submission_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if(mShareActionProvider != null){
            mShareActionProvider.setShareIntent(createShareSubmissionIntent());
        }

        return true;
    }

    private Intent createShareSubmissionIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        if(Build.VERSION.SDK_INT == 21){
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            //noinspection deprecation
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mDateStr);

        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_delete){
            submission.delete();
            finish();
        }

        return true;
    }
}
