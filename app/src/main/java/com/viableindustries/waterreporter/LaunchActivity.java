//package com.viableindustries.waterreporter;
//
//import android.Manifest;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.Bundle;
//import android.app.Activity;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.widget.SwipeRefreshLayout;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.widget.ListView;
//import android.widget.Toast;
//
//import com.google.gson.Gson;
//import com.viableindustries.waterreporter.data.FeatureCollection;
//import com.viableindustries.waterreporter.data.Organization;
//import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
//import com.viableindustries.waterreporter.data.QueryParams;
//import com.viableindustries.waterreporter.data.QuerySort;
//import com.viableindustries.waterreporter.data.Report;
//import com.viableindustries.waterreporter.data.ReportService;
//import com.viableindustries.waterreporter.data.UserService;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//import pub.devrel.easypermissions.AppSettingsDialog;
//import pub.devrel.easypermissions.EasyPermissions;
//import retrofit.Callback;
//import retrofit.RestAdapter;
//import retrofit.RetrofitError;
//import retrofit.client.Response;
//
//public class LaunchActivity extends AppCompatActivity {
//
//    private SharedPreferences prefs;
//
//    private SharedPreferences coreProfile;
//
//    protected int user_id;
//
//    protected RestAdapter restAdapter = ReportService.restAdapter;
//
//    protected ReportService service = restAdapter.create(ReportService.class);
//
//    protected boolean connectionActive = false;
//
//    protected Response errorResponse;
//
//    protected void connectionStatus() {
//
//        final Context context = getApplicationContext();
//
//        ConnectivityManager connMgr = (ConnectivityManager)
//                getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//
//        if (ConnectionUtility.connectionActive(this)) {
//
//            boolean cleanSlate = prefs.getBoolean("clean_slate", false);
//
//            String accessToken = prefs.getString("access_token", "");
//
//            user_id = prefs.getInt("user_id", 0);
//
//            // We need to force legacy users to log into a fresh session
//            // to ensure that the new version can collect and store the
//            // information it needs to function correctly.
//
//            // If user_id is 0, then the user hasn't registered
//
//            if (user_id == 0 || "".equals(accessToken) || !cleanSlate) {
//
//                prefs.edit().clear().apply();
//
//                startActivityForResult(new Intent(this, SignInActivity.class), LOGIN_REQUEST);
//
//            } else {
//
//                if (reportCollection.isEmpty()) {
//
//                    requestData(5, 1, false, false);
//
//                }
//
//                fetchUserGroups();
//
//            }
//
//        } else {
//
//            CharSequence text = "Unable to refresh feed.";
//            int duration = Toast.LENGTH_SHORT;
//
//            Toast toast = Toast.makeText(context, text, duration);
//            toast.show();
//
//        }
//
//    }
//
//    protected void requestData(int limit, final int page, final boolean transition, final boolean refresh) {
//
//        final String accessToken = prefs.getString("access_token", "");
//
//        Log.d("", accessToken);
//
//        // We shouldn't need to retrieve this value again, but we'll deal with that issue later
//        user_id = prefs.getInt("user_id", 0);
//
//        // Create order_by list and add a sort parameter
//
//        List<QuerySort> queryOrder = new ArrayList<QuerySort>();
//
//        QuerySort querySort = new QuerySort("created", "desc");
//
//        queryOrder.add(querySort);
//
//        // Create query string from new QueryParams
//
//        QueryParams queryParams = new QueryParams(null, queryOrder);
//
//        String query = new Gson().toJson(queryParams);
//
//        Log.d("URL", query);
//
//        service.getReports(accessToken, "application/json", page, limit, query, new Callback<FeatureCollection>() {
//
//            @Override
//            public void success(FeatureCollection featureCollection, Response response) {
//
//                List<Report> reports = featureCollection.getFeatures();
//
//                Log.v("list", reports.toString());
//
//                timeline.setRefreshing(false);
//
//                if (refresh || reportCollection.isEmpty()) {
//
//                    reportCollection.clear();
//
//                    reportCollection.addAll(reports);
//
//                    scrollListener.resetState();
//
//                    try {
//
//                        timelineAdapter.notifyDataSetChanged();
//
//                        listView.smoothScrollToPosition(0);
//
//                    } catch (NullPointerException e) {
//
//                        populateTimeline(reportCollection);
//
//                    }
//
//                } else {
//
//                    if (page > 1) {
//
//                        reportCollection.addAll(reports);
//
//                        timelineAdapter.notifyDataSetChanged();
//
//                    }
//
//                }
//
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//
//                timeline.setRefreshing(false);
//
//                if (error == null) return;
//
//                errorResponse = error.getResponse();
//
//                // If we have a valid response object, check the status code and redirect to log in view if necessary
//
//                if (errorResponse != null) {
//
//                    int status = errorResponse.getStatus();
//
//                    if (status == 403) {
//
//                        startActivity(new Intent(LaunchActivity.this, SignInActivity.class));
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
//    private void populateTimeline(List<Report> list) {
//
//        timelineAdapter = new TimelineAdapter(this, list, false);
//
//        // Attach the adapter to a ListView
//        listView.setAdapter(timelineAdapter);
//
//        //attachScrollListener();
//
//    }
//
//    protected void fetchUserGroups() {
//
//        final String accessToken = prefs.getString("access_token", "");
//
//        Log.d("", accessToken);
//
//        // We shouldn't need to retrieve this value again, but we'll deal with that issue later
//        user_id = prefs.getInt("user_id", 0);
//
//        UserService service = UserService.restAdapter.create(UserService.class);
//
//        service.getUserOrganization(accessToken, "application/json", user_id, new Callback<OrganizationFeatureCollection>() {
//
//            @Override
//            public void success(OrganizationFeatureCollection organizationCollectionResponse, Response response) {
//
//                List<Organization> organizations = organizationCollectionResponse.getFeatures();
//
//                String orgIds = "";
//
//                if (!organizations.isEmpty()) {
//
//                    for (Organization organization : organizations) {
//
//                        orgIds += String.format(",%s", organization.id);
//
//                    }
//
//                }
//
//                // Reset the user's stored group IDs.
//
//                prefs.edit().putString("user_groups", orgIds).apply();
//
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//
//                if (error == null) return;
//
//                errorResponse = error.getResponse();
//
//                // If we have a valid response object, check the status code and redirect to log in view if necessary
//
//                if (errorResponse != null) {
//
//                    int status = errorResponse.getStatus();
//
//                    if (status == 403) {
//
//                        startActivity(new Intent(LaunchActivity.this, SignInActivity.class));
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
//    private void attachScrollListener() {
//
//        listView.setOnScrollListener(scrollListener);
//
//    }
//
//    protected void verifyPermissions() {
//
//        String[] permissions = {
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.CAMERA,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//        };
//
//        if (EasyPermissions.hasPermissions(this, permissions)) {
//
//            requestData(5, 1, false, false);
//
//        } else {
//
//            // Ask for all permissions since the app is useless without them
//            EasyPermissions.requestPermissions(this, getString(R.string.rationale_all_permissions),
//                    RC_ALL_PERMISSIONS, permissions);
//
//        }
//
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_launch);
//
//        ButterKnife.bind(this);
//
//        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
//
//        coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);
//
//        // Set up EndlessScrollListener
//
//        scrollListener = new EndlessScrollListener() {
//            @Override
//            public boolean onLoadMore(int page, int totalItemsCount) {
//
//                // Triggered only when new data needs to be appended to the list
//                requestData(5, page, false, false);
//
//                return true; // ONLY if more data is actually being loaded; false otherwise.
//
//            }
//        };
//
//        timeline.setOnRefreshListener(
//                new SwipeRefreshLayout.OnRefreshListener() {
//                    @Override
//                    public void onRefresh() {
//                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");
//                        // This method performs the actual data-refresh operation.
//                        // The method calls setRefreshing(false) when it's finished.
//
//                        requestData(5, 1, false, true);
//
//                    }
//                }
//        );
//
//        // Set color of swipe refresh arrow animation
//
//        timeline.setColorSchemeResources(R.color.waterreporter_blue);
//
//        // Attach EndlessScrollListener to timeline ListView
//
//        attachScrollListener();
//
//        // Check permissions and handle missing requirements as necessary
//
//        verifyPermissions();
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
//        connectionStatus();
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
//        ButterKnife.unbind(this);
//
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        MenuInflater menuInflater = getMenuInflater();
//
//        menuInflater.inflate(R.menu.main, menu);
//
//        return super.onCreateOptionsMenu(menu);
//
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        if (!connectionActive) return false;
//
//        int id = item.getItemId();
//
//        return true;
//
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        if (requestCode == RC_SETTINGS_SCREEN) {
//            // Do something after user returned from app settings screen, like showing a Toast.
//            Toast.makeText(this, R.string.returned_from_app_settings_to_activity, Toast.LENGTH_SHORT)
//                    .show();
//        } else {
//
//            if (resultCode == RESULT_OK) {
//
//                // The user is logged in and may already have reports in the system.
//                // Let's attempt to fetch the user's report collection and, if none exists,
//                // direct the user to submit their first report.
//
//                verifyPermissions();
//
//                fetchUserGroups();
//
//            }
//
//        }
//
//    }
//
//    @Override
//    public void onBackPressed() {
//
//        Intent a = new Intent(Intent.ACTION_MAIN);
//        a.addCategory(Intent.CATEGORY_HOME);
//        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(a);
//
//    }
//
//}
