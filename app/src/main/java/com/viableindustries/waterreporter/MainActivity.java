package com.viableindustries.waterreporter;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.models.FeatureCollection;
import com.viableindustries.waterreporter.api.models.group.Group;
import com.viableindustries.waterreporter.api.models.group.GroupFeatureCollection;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.post.ReportHolder;
import com.viableindustries.waterreporter.api.models.query.QueryParams;
import com.viableindustries.waterreporter.api.models.query.QuerySort;
import com.viableindustries.waterreporter.constants.Constants;
import com.viableindustries.waterreporter.user_interface.adapters.TimelineAdapter;
import com.viableindustries.waterreporter.user_interface.dialogs.ReportActionDialog;
import com.viableindustries.waterreporter.utilities.ApiDispatcher;
import com.viableindustries.waterreporter.utilities.ConnectionUtility;
import com.viableindustries.waterreporter.utilities.EndlessScrollListener;
import com.viableindustries.waterreporter.utilities.ModelStorage;
import com.viableindustries.waterreporter.utilities.UploadStateReceiver;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements
        EasyPermissions.PermissionCallbacks,
        ReportActionDialog.ReportActionDialogCallback {

    @Bind(R.id.uploadProgressBar)
    ProgressBar uploadProgressBar;

    @Bind(R.id.uploadProgress)
    LinearLayout uploadProgress;

    @Bind(R.id.timeline)
    SwipeRefreshLayout timeline;

    @Bind(R.id.timeline_items)
    ListView listView;

    static final int REGISTRATION_REQUEST = 1;

    private static final int LOGIN_REQUEST = 2;

    private SharedPreferences mSharedPreferences;

    private int userId;

    private TimelineAdapter timelineAdapter;

    private final List<Report> reportCollection = new ArrayList<>();

    private final boolean connectionActive = false;

    private Response errorResponse;

    private static final int RC_ALL_PERMISSIONS = 100;

    private static final int RC_SETTINGS_SCREEN = 125;

    private static final String TAG = "MainActivity";

    private EndlessScrollListener scrollListener;

    private int socialOptions;

    // An instance of the status broadcast receiver
    private UploadStateReceiver mUploadStateReceiver;

    private final String CLASS_TAG = "AuthUserActivity";

    private void registerBroadcastReceiver() {

        /*
         * Creates an intent filter for DownloadStateReceiver that intercepts broadcast Intents
         */

        // The filter's action is BROADCAST_ACTION
        IntentFilter statusIntentFilter = new IntentFilter(
                Constants.BROADCAST_ACTION);

        // Sets the filter's category to DEFAULT
        statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        // Instantiates a new DownloadStateReceiver
        mUploadStateReceiver = new UploadStateReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

//                String storedPost = intent.getStringExtra("stored_post");
//
//                if (storedPost != null && !storedPost.isEmpty()) afterPostSend();

                /*
             * Gets the status from the Intent's extended api, and chooses the appropriate action
             */
                switch (intent.getIntExtra(Constants.EXTENDED_DATA_STATUS,
                        Constants.STATE_ACTION_COMPLETE)) {
                    // Logs "started" state
                    case Constants.STATE_ACTION_STARTED:
                        //
                        break;
                    // Logs "connecting to network" state
                    case Constants.STATE_ACTION_CONNECTING:
                        //
                        break;
                    // Logs "parsing the RSS feed" state
                    case Constants.STATE_ACTION_PARSING:
                        //
                        break;
                    // Logs "Writing the parsed api to the content provider" state
                    case Constants.STATE_ACTION_WRITING:
                        //
                        break;
                    // Starts displaying api when the RSS download is complete
                    case Constants.STATE_ACTION_COMPLETE:
                        // Logs the status
                        Log.d(CLASS_TAG, "State: COMPLETE");
                        afterPostSend();
                        break;
                    default:
                        break;
                }

            }
        };

        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mUploadStateReceiver,
                statusIntentFilter);

    }

    private void connectionStatus() {

        final Context context = getApplicationContext();

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (ConnectionUtility.connectionActive(this)) {

            boolean cleanSlate = mSharedPreferences.getBoolean("clean_slate", false);

            String mAccessToken = mSharedPreferences.getString("access_token", "");

            userId = mSharedPreferences.getInt("user_id", 0);

            // We need to force legacy users to log into a fresh session
            // to ensure that the new version can collect and store the
            // information it needs to function correctly.

            // If user_id is 0, then the user hasn't registered

            if (userId == 0 || "".equals(mAccessToken) || !cleanSlate) {

                mSharedPreferences.edit().clear().apply();

                startActivityForResult(new Intent(this, SignInActivity.class), LOGIN_REQUEST);

            } else {

                if (reportCollection.isEmpty()) {

                    fetchPosts(5, 1, false);

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

    void fetchPosts(int limit, final int page, final boolean refresh) {

        final String mAccessToken = mSharedPreferences.getString("access_token", "");

        // We shouldn't need to retrieve this value again, but we'll deal with that issue later
        userId = mSharedPreferences.getInt("user_id", 0);

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<>();

        QuerySort querySort = new QuerySort("created", "desc");

        queryOrder.add(querySort);

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(null, queryOrder);

        String query = new Gson().toJson(queryParams);

        Log.d("URL", query);

        RestClient.getReportService().getReports(mAccessToken, "application/json", page, limit, query, new Callback<FeatureCollection>() {

            @Override
            public void success(FeatureCollection featureCollection, Response response) {

                List<Report> reports = featureCollection.getFeatures();

                Log.v("list", reports.toString());

                timeline.setRefreshing(false);

                if (refresh || reportCollection.isEmpty()) {

                    reportCollection.clear();

                    reportCollection.addAll(reports);

                    scrollListener.resetState();

                    try {

                        timelineAdapter.notifyDataSetChanged();

                        listView.smoothScrollToPosition(0);

                    } catch (NullPointerException e) {

                        populateTimeline(reportCollection);

                    }

                } else {

                    if (page > 1) {

                        reportCollection.addAll(reports);

                        timelineAdapter.notifyDataSetChanged();

                    }

                }

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

    private void populateTimeline(List<Report> list) {

        timelineAdapter = new TimelineAdapter(this, list, false, false, getSupportFragmentManager());

        // Attach the adapter to a ListView
        listView.setAdapter(timelineAdapter);

        //attachScrollListener();

    }

    private void fetchUserGroups() {

        final String mAccessToken = mSharedPreferences.getString("access_token", "");

        Log.d("", mAccessToken);

        // We shouldn't need to retrieve this value again, but we'll deal with that issue later
        userId = mSharedPreferences.getInt("user_id", 0);

        RestClient.getUserService().getUserGroups(mAccessToken, "application/json", userId, new Callback<GroupFeatureCollection>() {

            @Override
            public void success(GroupFeatureCollection groupFeatureCollection, Response response) {

                ArrayList<Group> groups = groupFeatureCollection.getFeatures();

                // Reset the user's stored group IDs.

                SharedPreferences groupMembership = getSharedPreferences(getString(R.string.group_membership_key), 0);

                groupMembership.edit().clear().apply();

                for (Group group : groups) {

                    ModelStorage.storeModel(groupMembership, group, group.properties.organization.properties.name);

//                    groupMembership.edit().putInt(group.properties.organization.properties.name, group.properties.organization.properties.id).apply();

                }

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

    private void attachScrollListener() {

        listView.setOnScrollListener(scrollListener);

    }

    private void verifyPermissions() {

        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        if (EasyPermissions.hasPermissions(this, permissions)) {

            fetchPosts(5, 1, false);

        } else {

            // Ask for all permissions since the app is useless without them
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_all_permissions),
                    RC_ALL_PERMISSIONS, permissions);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this, getString(R.string.rationale_ask_again))
                    .setTitle(getString(R.string.title_settings_dialog))
                    .setPositiveButton(getString(R.string.setting))
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            // At this point the user has had several opportunities to grant
                            // the necessary permissions. Stop tolerating rogue user behaviors here.

                            Intent a = new Intent(Intent.ACTION_MAIN);
                            a.addCategory(Intent.CATEGORY_HOME);
                            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(a);

                            ActivityCompat.finishAffinity(MainActivity.this);

                        }
                    })
                    .setRequestCode(RC_SETTINGS_SCREEN)
                    .build()
                    .show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        uploadProgressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.splash_blue), android.graphics.PorterDuff.Mode.SRC_IN);

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        SharedPreferences mCoreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

        // Set up EndlessScrollListener

        scrollListener = new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new api needs to be appended to the list
                fetchPosts(5, page, false);

                return true; // ONLY if more api is actually being loaded; false otherwise.

            }
        };

        timeline.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual api-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.

                        fetchPosts(5, 1, true);

                    }
                }
        );

        // Set color of swipe refresh arrow animation

        timeline.setColorSchemeResources(R.color.waterreporter_blue);

        // Attach EndlessScrollListener to timeline ListView

        attachScrollListener();

        // Check permissions and handle missing requirements as necessary

        verifyPermissions();

    }

    private void afterPostSend() {

        ApiDispatcher.setTransmissionActive(mSharedPreferences, false);
//        mSharedPreferences.edit().putInt("POST", 0).apply();
        mSharedPreferences.edit().remove("POST_SAVED_VIA_SERVICE").apply();
        uploadProgress.setVisibility(View.GONE);
        fetchPosts(5, 1, true);

    }

    @Override
    public void onPostDelete(Report post) {

        ReportHolder.setReport(null);

        timeline.setRefreshing(true);

        fetchPosts(5, 1, true);

    }

    @Override
    protected void onStart() {

        super.onStart();

        // Check for a api connection!

        connectionStatus();

        // Check for active transmissions

        if ((ApiDispatcher.transmissionActive(this) || ApiDispatcher.getPendingPostId(this) > 0) && uploadProgress != null) {

            uploadProgress.setVisibility(View.VISIBLE);

        }

        // Check for completed request not handled in the receiver's onReceive

        if (ApiDispatcher.getPendingPostId(this) > 0) {

            afterPostSend();

        }

    }

    @Override
    protected void onResume() {

        super.onResume();

        // Check for a api connection!

        connectionStatus();

        // Check for active transmissions

        if ((ApiDispatcher.transmissionActive(this) || ApiDispatcher.getPendingPostId(this) > 0) && uploadProgress != null) {

            uploadProgress.setVisibility(View.VISIBLE);

        }

        // Check for completed request not handled in the receiver's onReceive

        if (ApiDispatcher.getPendingPostId(this) > 0) {

            afterPostSend();

        }

        registerBroadcastReceiver();

    }

    @Override
    protected void onPause() {

        super.onPause();

        // If the DownloadStateReceiver still exists, unregister it
        if (mUploadStateReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mUploadStateReceiver);
        }

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

        // Cancel all pending network requests

        //Callback.cancelAll();

        // If the DownloadStateReceiver still exists, unregister it and set it to null
        if (mUploadStateReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mUploadStateReceiver);
            mUploadStateReceiver = null;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_SETTINGS_SCREEN) {
            // Do something after user returned from app settings screen, like showing a Toast.
            Toast.makeText(this, R.string.returned_from_app_settings_to_activity, Toast.LENGTH_SHORT)
                    .show();
        } else {

            if (resultCode == RESULT_OK) {

                // The user is logged in and may already have reports in the system.
                // Let's attempt to fetch the user's report collection and, if none exists,
                // direct the user to submit their first report.

                verifyPermissions();

                fetchUserGroups();

            }

        }

    }

    @Override
    public void onBackPressed() {

        // Cancel all pending network requests

        //Callback.cancelAll();

        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);

    }

}