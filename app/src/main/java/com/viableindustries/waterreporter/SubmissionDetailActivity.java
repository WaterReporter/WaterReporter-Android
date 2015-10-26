package com.viableindustries.waterreporter;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.ShareActionProvider;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Submission;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Ryan Hamley on 10/28/14.
 * Activity displays detailed information on your submissions after choosing
 * one in the submissions list.
 */
public class SubmissionDetailActivity extends AppCompatActivity {

    @Bind(R.id.date_label)
    TextView dateText;

    @Bind(R.id.comments_label)
    TextView commentsText;

    @Bind(R.id.submission_image)
    ImageView imageView;

    private Submission submission;

    private String mDateStr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_submission_detail);

        final ActionBar actionBar = this.getActionBar();

        if (actionBar != null) {

            actionBar.setHomeButtonEnabled(true);

            actionBar.setDisplayHomeAsUpEnabled(true);

        }

        ButterKnife.bind(this);

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra("SubmissionId")) {

            long id = intent.getLongExtra("SubmissionId", 0);

            submission = Submission.findById(Submission.class, id);

            mDateStr = submission.report_date;

            dateText.setText("Submitted on " + mDateStr);

            commentsText.setText(submission.report_description);

            if (submission.galleryPath != null && submission.galleryPath.length() > 0) {

                Log.d("remote", submission.galleryPath);

                Picasso.with(this)
                        .load(submission.galleryPath)
                        .placeholder(R.drawable.square_placeholder)
                        .into(imageView);

            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.submission_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mShareActionProvider != null) {

            mShareActionProvider.setShareIntent(createShareSubmissionIntent());

        }

        return true;
    }

    private Intent createShareSubmissionIntent() {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        if (Build.VERSION.SDK_INT == 21) {

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

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_delete:
                submission.delete();
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);

    }
}
