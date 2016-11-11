package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.viableindustries.waterreporter.data.FeatureCollection;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.UserService;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.timeline)
    SwipeRefreshLayout timeline;

    @Bind(R.id.timeline_items)
    ListView listView;

    static final int REGISTRATION_REQUEST = 1;

    static final int LOGIN_REQUEST = 2;

    protected SharedPreferences prefs = null;

    protected int user_id;

    protected RestAdapter restAdapter = ReportService.restAdapter;

    protected ReportService service = restAdapter.create(ReportService.class);

    protected TimelineAdapter timelineAdapter;

    protected List<Report> reportCollection = new ArrayList<Report>();

    protected boolean connectionActive = false;

    protected Response errorResponse;

    protected void connectionStatus() {

        final Context context = getApplicationContext();

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (ConnectionUtility.connectionActive(this)) {

            String access_token = prefs.getString("access_token", "");

            user_id = prefs.getInt("user_id", 0);

            if (user_id == 0 || access_token.equals("")) {

                startActivityForResult(new Intent(this, SignInActivity.class), LOGIN_REQUEST);

            } else {

                if (reportCollection.isEmpty()) {

                    requestData(10, 1, false, false);

                }

                fetchUserGroups();

            }

        } else {

            CharSequence text = "Unable to refresh feed.";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

        }

    }

    protected void requestData(int limit, int page, final boolean transition, final boolean refresh) {

        SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        Log.d("", access_token);

        // We shouldn't need to retrieve this value again, but we'll deal with that issue later
        user_id = prefs.getInt("user_id", 0);

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<QuerySort>();

        QuerySort querySort = new QuerySort("created", "desc");

        queryOrder.add(querySort);

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(null, queryOrder);

        String query = new Gson().toJson(queryParams);

        Log.d("URL", query);

        service.getReports(access_token, "application/json", page, limit, query, new Callback<FeatureCollection>() {

            @Override
            public void success(FeatureCollection featureCollection, Response response) {

                List<Report> reports = featureCollection.getFeatures();

                Log.v("list", reports.toString());

                if (!reports.isEmpty()) {

                    // If the submission view was requested

                    if (transition) {

                        startActivity(new Intent(MainActivity.this, SubmissionsActivity.class));

                    }

                    reportCollection.addAll(reports);

                    try {

                        timelineAdapter.notifyDataSetChanged();

                    } catch (NullPointerException ne) {

                        populateTimeline(reportCollection);

                    }

                } else {

                    CharSequence text = "Your report collection is empty. Tap on the plus sign in the menu bar to start a new report.";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(getBaseContext(), text, duration);
                    toast.show();

                }

                if (refresh) {

                    reportCollection = reports;

                    populateTimeline(reportCollection);

                }

                timeline.setRefreshing(false);

            }

            @Override
            public void failure(RetrofitError error) {

                timeline.setRefreshing(false);

                if (error == null) return;

                errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(MainActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void populateTimeline(List list) {

        timelineAdapter = new TimelineAdapter(this, list, false);

        // Attach the adapter to a ListView
        listView.setAdapter(timelineAdapter);

        attachScrollListener();

    }

    protected void fetchUserGroups() {

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        Log.d("", access_token);

        // We shouldn't need to retrieve this value again, but we'll deal with that issue later
        user_id = prefs.getInt("user_id", 0);

        UserService service = UserService.restAdapter.create(UserService.class);

        service.getUserOrganization(access_token, "application/json", user_id, new Callback<OrganizationFeatureCollection>() {

            @Override
            public void success(OrganizationFeatureCollection organizationCollectionResponse, Response response) {

                List<Organization> organizations = organizationCollectionResponse.getFeatures();

                String orgIds = "";

                if (!organizations.isEmpty()) {

                    for (Organization organization : organizations) {

                        orgIds += String.format(",%s", organization.id);

                    }

                }

                // Reset the user's stored group IDs.

                prefs.edit().putString("user_groups", orgIds).apply();

            }

            @Override
            public void failure(RetrofitError error) {

                if (error == null) return;

                errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(MainActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    // Transition to the organization list view

    public void viewGroups(View v) {

        startActivity(new Intent(this, OrganizationListActivity.class));

        finish();

    }

    private void attachScrollListener() {

        listView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new data needs to be appended to the list
                requestData(10, page, false, false);

                return true; // ONLY if more data is actually being loaded; false otherwise.

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        timeline.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        requestData(10, 1, false, true);
                    }
                }
        );

        // Set color of swipe refresh arrow animation

        timeline.setColorSchemeResources(R.color.waterreporter_blue);

    }

    @Override
    protected void onResume() {

        super.onResume();

        // Check for a data connection!

        connectionStatus();

    }

    @Override
    protected void onPause() {

        super.onPause();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (!connectionActive) return false;

        int id = item.getItemId();

        return true;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
//        if (requestCode == REGISTRATION_REQUEST) {
//            // Make sure the request was successful
//            if (resultCode == RESULT_OK) {
//
//                // Since the user just registered, direct them to create a new report
//                startActivity(new Intent(this, PhotoActivity.class));
//
//            }
//
//        } else if (requestCode == LOGIN_REQUEST) {
//
//            if (resultCode == RESULT_OK) {
//
//                // The user is logged in and may already have reports in the system.
//                // Let's attempt to fetch the user's report collection and, if none exist,
//                // direct the user to submit their first report.
//                requestData(10, 1, false, false);
//
//                fetchUserGroups();
//
//            }
//
//        }

        if (resultCode == RESULT_OK) {

            // The user is logged in and may already have reports in the system.
            // Let's attempt to fetch the user's report collection and, if none exist,
            // direct the user to submit their first report.
            requestData(10, 1, false, false);

            fetchUserGroups();

        }

    }

}
