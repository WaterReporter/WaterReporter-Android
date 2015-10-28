package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orm.query.Select;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.FeatureCollection;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.ImageProperties;
import com.viableindustries.waterreporter.data.ImageService;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportPhoto;
import com.viableindustries.waterreporter.data.ReportPostBody;
import com.viableindustries.waterreporter.data.ReportPostResponse;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.GeometryResponse;
import com.viableindustries.waterreporter.data.Submission;
import com.viableindustries.waterreporter.progress.CountingTypedFile;
import com.viableindustries.waterreporter.progress.ProgressListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

/**
 * Created by Ryan Hamley on 10/28/14.
 * Activity displays a list of user submissions which can be submitted to the API via pull
 * to refresh action
 */
public class SubmissionsActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    @Bind(R.id.swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.listview_submissions)
    ListView submissionsListView;

    private List<Report> submissions;
    private SubmittedAdapter adapter;
    private boolean postFailed = false;
    private static final String onResume = "onResume";
    private static final String onRefresh = "onRefresh";
    private static final String NAME_KEY = "user_name";
    private static final String EMAIL_KEY = "user_email";
    private static final String TITLE_KEY = "user_title";

    // Check for a data connection!

    protected void connectionActive() {

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            submissionsListView.setVisibility(View.VISIBLE);

            fetchRemoteReports();

        } else {

            submissionsListView.setVisibility(View.GONE);

            CharSequence text = "Looks like you're not connected to the internet, so we couldn't retrieve your reports. Please connect to a network and try again.";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(getBaseContext(), text, duration);
            toast.show();

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_submissions);

        ButterKnife.bind(this);

        swipeRefreshLayout.setOnRefreshListener(this);

        swipeRefreshLayout.setColorSchemeResources(R.color.waterreporter_blue,
                R.color.waterreporter_dark);

        connectionActive();

    }

    @Override
    protected void onResume() {

        super.onResume();

        if (!swipeRefreshLayout.isRefreshing()) {

            connectionActive();

        }

    }

    @Override
    protected void onRestart() {

        super.onRestart();

        if (submissions != null) submissions.clear();

        connectionActive();

    }

    @Override
    protected void onPause() {

        super.onPause();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.submissions, menu);
        return true;
    }

    @Override
    public void onRefresh() {

        connectionActive();

    }

    protected void createListElements() {

        adapter = new SubmittedAdapter();

        submissionsListView.setAdapter(adapter);

        submissionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Report report = submissions.get(i);

                int id = report.id;

                Intent intent = new Intent(getBaseContext(), SubmissionDetailActivity.class)
                        .putExtra("SUBMISSION_ID", id);

                startActivity(intent);

            }
        });

    }

    protected void onFetchSuccess(List<Report> reports) {

        submissions = reports;

        if (adapter == null) createListElements();

        adapter.notifyDataSetChanged();

        submissionsListView.invalidateViews();

        swipeRefreshLayout.setRefreshing(false);

    }

    protected void onFetchError() {

        swipeRefreshLayout.setRefreshing(false);

        submissionsListView.invalidateViews();

        CharSequence text =
                "Error fetching reports. Please try again later.";

        Toast toast = Toast.makeText(getBaseContext(), text,
                Toast.LENGTH_SHORT);

        toast.show();

    }

    protected void fetchRemoteReports() {

        //swipeRefreshLayout.setRefreshing(true);

        // Retrieve feature IDs from the local database and use them in a

        SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        Log.d("", access_token);

        // Retrieve the user id

        int user_id = prefs.getInt("user_id", 0);

        // Add query filters to match user ID and feature IDs not in the local database

        String query = "{\"filters\":[{\"name\":\"owner_id\",\"op\":\"eq\",\"val\":" + user_id + "}],\"order_by\":[{\"field\":\"created\",\"direction\":\"desc\"}]}";

        ReportService service = ReportService.restAdapter.create(ReportService.class);

        service.getReports(access_token, "application/json", 100, query, new Callback<FeatureCollection>() {

            @Override
            public void success(FeatureCollection featureCollection, Response response) {

                List<Report> reports = featureCollection.getFeatures();

                Log.v("list", reports.toString());

                if (!reports.isEmpty()) {

                    onFetchSuccess(reports);

                } else {

                    // If the user somehow ends up in a situation where they have zero reports
                    // and still found themselves in this activity (maybe after deleting their
                    // one and only report), send them to the main activity so they can start
                    // building up their report collection again

                    startActivity(new Intent(getBaseContext(), MainActivity.class));

                }

            }

            @Override
            public void failure(RetrofitError error) {

                Response response = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (response != null) {

                    int status = response.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(SubmissionsActivity.this, MainActivity.class));

                    }

                }

            }

        });

    }

    private static class SubmittedViewHolder {
        //public ImageView checkMark;
        public TextView text;
        //public ProgressBar progressBar;
        public View saved;
    }

    private class SubmittedAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return submissions.size();
        }

        @Override
        public Report getItem(int position) {
            return submissions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            SubmittedViewHolder holder;

            if (convertView == null) {

                convertView = getLayoutInflater()
                        .inflate(R.layout.list_item_submission, parent, false);

                holder = new SubmittedViewHolder();

                holder.text = (TextView)
                        convertView.findViewById(R.id.list_item_submission_textview);

                holder.text.setText("Submitted on " + getItem(position).properties.getFormattedDateString());

                convertView.setTag(holder);

            } else {

                holder = (SubmittedViewHolder) convertView.getTag();

            }

            holder.saved = convertView.findViewById(R.id.saved);

            holder.saved.setVisibility(View.VISIBLE);

            return convertView;

        }
    }

}