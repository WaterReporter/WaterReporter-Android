//package com.viableindustries.waterreporter;
//
//import android.app.ActionBar;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.Build;
//import android.support.v4.app.NavUtils;
//import android.support.v4.content.ContextCompat;
//import android.support.v4.view.GravityCompat;
//import android.support.v4.view.MenuItemCompat;
//import android.support.v7.app.ActionBarActivity;
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.support.v7.widget.ShareActionProvider;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.squareup.picasso.Picasso;
//import com.viableindustries.waterreporter.data.GroupNameComparator;
//import com.viableindustries.waterreporter.data.Organization;
//import com.viableindustries.waterreporter.data.Report;
//import com.viableindustries.waterreporter.data.ReportService;
//import com.viableindustries.waterreporter.data.Submission;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Collections;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//import retrofit.Callback;
//import retrofit.RestAdapter;
//import retrofit.RetrofitError;
//import retrofit.client.Response;
//
///**
// * Created by Ryan Hamley on 10/28/14.
// * Activity displays detailed information on your submissions after choosing
// * one in the submissions list.
// */
//public class SubmissionDetailActivity extends AppCompatActivity {
//
//    @Bind(R.id.detail_container)
//    LinearLayout detailContainer;
//
//    @Bind(R.id.post_content)
//    LinearLayout reportContent;
//
//    @Bind(R.id.loading_spinner)
//    ProgressBar progressBar;
//
//    @Bind(R.id.date_label)
//    TextView dateText;
//
//    @Bind(R.id.comments_label)
//    TextView commentsText;
//
//    @Bind(R.id.submission_image)
//    ImageView imageView;
//
//    @Bind(R.id.group_affiliation)
//    TextView groupAffiliation;
//
//    protected int reportId;
//
//    private String mDateStr;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_submission_detail);
//
//        final ActionBar actionBar = this.getActionBar();
//
//        if (actionBar != null) {
//
//            actionBar.setHomeButtonEnabled(true);
//
//            actionBar.setDisplayHomeAsUpEnabled(true);
//
//        }
//
//        ButterKnife.bind(this);
//
//        detailContainer.setGravity(Gravity.CENTER);
//
//        Intent intent = getIntent();
//
//        if (intent != null && intent.hasExtra("SUBMISSION_ID")) {
//
//            reportId = intent.getIntExtra("SUBMISSION_ID", 0);
//
//        }
//
//        connectionActive();
//
//    }
//
//    // Check for a data connection!
//
//    protected void connectionActive() {
//
//        ConnectivityManager connMgr = (ConnectivityManager)
//                getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//
//        if (networkInfo != null && networkInfo.isConnected() && reportId > 0) {
//
//            requestData(reportId);
//
//        } else {
//
//            CharSequence text = "Looks like you're not connected to the internet, so we couldn't start your report. Please connect to a network and try again.";
//            int duration = Toast.LENGTH_LONG;
//
//            Toast toast = Toast.makeText(getBaseContext(), text, duration);
//            toast.show();
//
//        }
//
//    }
//
//    protected void requestData(int id){
//
//        progressBar.getIndeterminateDrawable().setColorFilter(
//                ContextCompat.getColor(SubmissionDetailActivity.this, R.color.base_blue),
//                android.graphics.PorterDuff.Mode.SRC_IN);
//
//        SharedPreferences prefs =
//                getSharedPreferences(getPackageName(), MODE_PRIVATE);
//
//        final String accessToken = prefs.getString("access_token", "");
//
//        RestAdapter restAdapter = ReportService.restAdapter;
//
//        ReportService service = restAdapter.create(ReportService.class);
//
//        service.getSingleReport(accessToken, "application/json", id, new Callback<Report>() {
//
//            @Override
//            public void success(Report report, Response response) {
//
//                progressBar.setVisibility(View.GONE);
//
//                detailContainer.setGravity(Gravity.TOP);
//
//                reportContent.setVisibility(View.VISIBLE);
//
//                if(report.properties.images.size() != 0){
//
//                    String filePath = report.properties.images.get(0).properties.square_retina;
//
//                    Picasso.with(getBaseContext())
//                            .load(filePath)
//                            .placeholder(R.drawable.square_placeholder)
//                            .into(imageView);
//
//                }
//
//                if (report.properties.groups.size() != 0) {
//
//                    ArrayList<Organization> organizations = report.properties.groups;
//
//                    Collections.sort(organizations, new GroupNameComparator());
//
//                    populateOrganizations(organizations);
//
//                }
//
//                mDateStr = report.properties.getFormattedDateString();
//
//                dateText.setText("Submitted on " + mDateStr);
//
//                commentsText.setText(report.properties.description);
//
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//
//                progressBar.setVisibility(View.GONE);
//
//                Response response = error.getResponse();
//
//                // If we have a valid response object, check the status code and redirect to log in view if necessary
//
//                if (response != null) {
//
//                    int status = response.getStatus();
//
//                    if (status == 403) {
//
//                        startActivity(new Intent(SubmissionDetailActivity.this, MainActivity.class));
//
//                    } else {
//
//                        CharSequence text = "Something went wrong and we couldn't retrieve this report.";
//                        int duration = Toast.LENGTH_LONG;
//
//                        Toast toast = Toast.makeText(getBaseContext(), text, duration);
//                        toast.show();
//
//                    }
//
//                }
//
//            }
//
//        });
//
//    }
//
//    private void populateOrganizations(ArrayList<Organization> orgs) {
//
//        groupAffiliation.setVisibility(View.VISIBLE);
//
//        final OrganizationListAdapter adapter = new OrganizationListAdapter(SubmissionDetailActivity.this, orgs, true);
//
//        final int adapterCount = adapter.getCount();
//
//        Log.d("count", String.valueOf(adapterCount));
//
//        for (int i = 0; i < adapterCount; i++) {
//
//            View item = adapter.getView(i, null, null);
//
//            reportContent.addView(item);
//
//        }
//
//    }
//
//    protected void deleteReport(int id){
//
//        SharedPreferences prefs =
//                getSharedPreferences(getPackageName(), MODE_PRIVATE);
//
//        final String accessToken = prefs.getString("access_token", "");
//
//        RestAdapter restAdapter = ReportService.restAdapter;
//
//        ReportService service = restAdapter.create(ReportService.class);
//
//        service.deleteSingleReport(access_token, id, new Callback<Response>() {
//
//            @Override
//            public void success(Response response, Response responseCallBack) {
//
//                if (response != null) {
//
//                    CharSequence text = "Report deleted!";
//                    int duration = Toast.LENGTH_SHORT;
//
//                    Toast toast = Toast.makeText(getBaseContext(), text, duration);
//                    toast.show();
//
//                    startActivity(new Intent(SubmissionDetailActivity.this, SubmissionsActivity.class));
//
//                    finish();
//
//                }
//
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//
//                Response response = error.getResponse();
//
//                // If we have a valid response object, check the status code and redirect to log in view if necessary
//
//                if (response != null) {
//
//                    int status = response.getStatus();
//
//                    if (status == 403) {
//
//                        startActivity(new Intent(SubmissionDetailActivity.this, MainActivity.class));
//
//                    }
//
//                }
//
//            }
//
//        });
//
//    }
//
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.submission_detail, menu);
//
//        MenuItem menuItem = menu.findItem(R.id.action_share);
//
//        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
//
//        if (mShareActionProvider != null) {
//
//            mShareActionProvider.setShareIntent(createShareSubmissionIntent());
//
//        }
//
//        return true;
//    }
//
//    private Intent createShareSubmissionIntent() {
//
//        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//
//        if (Build.VERSION.SDK_INT == 21) {
//
//            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
//
//        } else {
//
//            //noinspection deprecation
//            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//
//        }
//
//        shareIntent.setType("text/plain");
//
//        shareIntent.putExtra(Intent.EXTRA_TEXT, mDateStr);
//
//        return shareIntent;
//
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        switch (item.getItemId()) {
//            // Respond to the action bar's Up/Home button
//            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
//                return true;
//            case R.id.action_delete:
//                deleteReport(reportId);
//                //finish();
//                return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//
//    }
//
//    @Override
//    protected void onResume() {
//
//        super.onResume();
//
//        // Check for a data connection!
//
//        //connectionActive();
//
//    }
//
//    @Override
//    protected void onPause() {
//
//        super.onPause();
//
//    }
//
//    @Override
//    protected void onDestroy() {
//
//        super.onDestroy();
//
//        Picasso.with(this).cancelRequest(imageView);
//
//        ButterKnife.unbind(this);
//
//    }
//
//}
