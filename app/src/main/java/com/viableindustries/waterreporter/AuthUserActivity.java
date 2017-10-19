package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.models.FeatureCollection;
import com.viableindustries.waterreporter.api.models.group.Group;
import com.viableindustries.waterreporter.api.models.group.GroupFeatureCollection;
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.organization.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.post.ReportHolder;
import com.viableindustries.waterreporter.api.models.query.QueryFilter;
import com.viableindustries.waterreporter.api.models.query.QueryParams;
import com.viableindustries.waterreporter.api.models.query.QuerySort;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.api.models.user.UserGroupList;
import com.viableindustries.waterreporter.constants.Constants;
import com.viableindustries.waterreporter.user_interface.adapters.TimelineAdapter;
import com.viableindustries.waterreporter.user_interface.dialogs.ReportActionDialog;
import com.viableindustries.waterreporter.user_interface.view_holders.UserProfileHeaderView;
import com.viableindustries.waterreporter.utilities.ApiDispatcher;
import com.viableindustries.waterreporter.utilities.EndlessScrollListener;
import com.viableindustries.waterreporter.utilities.ModelStorage;
import com.viableindustries.waterreporter.utilities.UploadStateReceiver;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AuthUserActivity extends AppCompatActivity implements
        ReportActionDialog.ReportActionDialogCallback,
        UserProfileHeaderView.UserProfileHeaderCallback {

    @Bind(R.id.uploadProgressBar)
    ProgressBar uploadProgressBar;

    @Bind(R.id.uploadProgress)
    LinearLayout uploadProgress;

    @Bind(R.id.timeline)
    SwipeRefreshLayout timeLineContainer;

    @Bind(R.id.timeline_items)
    ListView timeLine;

    @Bind(R.id.listTabs)
    FrameLayout listTabs;

    @Bind(R.id.log_out)
    ImageButton logOutButton;

    private TimelineAdapter timelineAdapter;

    private final List<Report> reportCollection = new ArrayList<>();

    private String complexQuery;

    private int userId;

    private int actionCount;

    private int reportCount = 99999999;

    private boolean actionFocus = false;

    private SharedPreferences mSharedPreferences;

    private SharedPreferences mCoreProfile;

    private User user;

    private Resources resources;

    private EndlessScrollListener scrollListener;

    private String mAccessToken;

    // An instance of the status broadcast receiver
    private UploadStateReceiver mUploadStateReceiver;

    private final String CLASS_TAG = "MainActivity";

    private UserProfileHeaderView mUserProfileHeaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_auth_user);

        ButterKnife.bind(this);

        uploadProgressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.splash_blue), android.graphics.PorterDuff.Mode.SRC_IN);

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        mCoreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

        mAccessToken = mSharedPreferences.getString("access_token", "");

        Log.d("storedavatar", mCoreProfile.getString("picture", ""));

        resources = getResources();

        // Retrieve stored User object

        retrieveStoredUser();

        if (mSharedPreferences.getInt("user_id", 0) == userId) {

            logOutButton.setVisibility(View.VISIBLE);

        }

        // Set refresh listener on report feed container

        timeLineContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual api-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.

                        countReports(complexQuery, "state");

                        resetStats();

                    }
                }
        );

        // Set color of swipe refresh arrow animation

        timeLineContainer.setColorSchemeResources(R.color.waterreporter_blue);

        // Inflate and insert timeline header view

        addListViewHeader();

        // Count reports with actions

        complexQuery = String.format(getResources().getString(R.string.complex_actions_query), userId, userId);

        countReports(complexQuery, "state");

        // Retrieve the user's groups

        fetchUserGroups(userId);

        // Retrieve first batch of user's reports

        if (reportCollection.isEmpty()) {

            timeLineContainer.setRefreshing(true);

            fetchPosts(5, 1, buildQuery(true, null), false);

        }

        scrollListener = new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new api needs to be appended to the list

                if (actionFocus) {

                    fetchPosts(5, page, complexQuery, false);

                } else {

                    fetchPosts(5, page, buildQuery(true, null), false);

                }

                return true; // ONLY if more api is actually being loaded; false otherwise.

            }
        };

    }

    private void retrieveStoredUser() {

        user = ModelStorage.getStoredUser(mCoreProfile, "auth_user");

        try {

            userId = user.properties.id;

        } catch (NullPointerException _e) {

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

    }

    private void setPostCountState(int count) {

        mUserProfileHeaderView.reportCounter.setText(String.valueOf(reportCount));
        mUserProfileHeaderView.reportCountLabel.setText(resources.getQuantityString(R.plurals.post_label, reportCount, reportCount));

        if (count < 1) {

            if (mUserProfileHeaderView.promptBlock != null) {

                mUserProfileHeaderView.startPostButton.setVisibility(View.GONE);

                mUserProfileHeaderView.promptBlock.setVisibility(View.VISIBLE);

                mUserProfileHeaderView.promptMessage.setText(getString(R.string.prompt_no_posts_auth_user));

            }

        } else {

            timeLineContainer.setVisibility(View.VISIBLE);

        }

    }

    private void startPost() {

        Intent intent = new Intent(this, PhotoMetaActivity.class);

        startActivity(intent);

        this.overridePendingTransition(R.anim.animation_enter_right,
                R.anim.animation_exit_left);

    }

    private void addListViewHeader() {

        mUserProfileHeaderView = new UserProfileHeaderView();

        LayoutInflater inflater = getLayoutInflater();

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.user_profile_header, timeLine, false);

        mUserProfileHeaderView.buildHeader(this, header, user);

        timeLine.addHeaderView(header, null, false);

    }

    private void countReports(String query, final String filterName) {

        RestClient.getReportService().getReports(mAccessToken, "application/json", 1, 1, query, new Callback<FeatureCollection>() {

            @Override
            public void success(FeatureCollection featureCollection, Response response) {

                int count = featureCollection.getProperties().num_results;

                switch (filterName) {
                    case "state":
                        if (count > 0) {
                            mUserProfileHeaderView.actionStat.setVisibility(View.VISIBLE);
                            actionCount = count;
                            mUserProfileHeaderView.actionCounter.setText(String.valueOf(actionCount));
                            mUserProfileHeaderView.actionCountLabel.setText(resources.getQuantityString(R.plurals.action_label, actionCount, actionCount));
                        }
                        break;
                    default:
                        reportCount = count;
                        setPostCountState(reportCount);
                        break;
                }

            }

            @Override
            public void failure(RetrofitError error) {

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(AuthUserActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void fetchUserGroups(int userId) {

        RestClient.getUserService().getUserGroups(mAccessToken, "application/json", userId, new Callback<GroupFeatureCollection>() {

            @Override
            public void success(GroupFeatureCollection groupFeatureCollection, Response response) {

                ArrayList<Group> groups = groupFeatureCollection.getFeatures();

                if (!groups.isEmpty()) {

                    int groupCount = groups.size();

                    mUserProfileHeaderView.groupCounter.setText(String.valueOf(groupCount));
                    mUserProfileHeaderView.groupCountLabel.setText(resources.getQuantityString(R.plurals.group_label, groupCount, groupCount));

                    mUserProfileHeaderView.groupStat.setVisibility(View.VISIBLE);

                    UserGroupList.setList(groups);

                }

                // Reset the user's stored groups.

                SharedPreferences groupMembership = getSharedPreferences(getString(R.string.group_membership_key), 0);

                groupMembership.edit().clear().apply();

                for (Group group : groups) {

                    ModelStorage.storeModel(groupMembership, group, String.format("group_%s", group.properties.organizationId));

                }

            }

            @Override
            public void failure(RetrofitError error) {

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(AuthUserActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void attachScrollListener() {

        timeLine.setOnScrollListener(scrollListener);

    }

    private String buildQuery(boolean order, String[][] optionalFilters) {

        List<QuerySort> queryOrder = null;

        // Create order_by list and add a sort parameter

        if (order) {

            queryOrder = new ArrayList<>();

            QuerySort querySort = new QuerySort("created", "desc");

            queryOrder.add(querySort);

        }

        // Create filter list and add a filter parameter

        List<Object> queryFilters = new ArrayList<>();

        QueryFilter userFilter = new QueryFilter("owner_id", "eq", userId);

        queryFilters.add(userFilter);

        if (optionalFilters != null) {

            for (String[] filterComponents : optionalFilters) {

                QueryFilter optionalFilter = new QueryFilter(filterComponents[0], filterComponents[1], filterComponents[2]);

                queryFilters.add(optionalFilter);

            }

        }

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(queryFilters, queryOrder);

        return new Gson().toJson(queryParams);

    }

    public void fetchPosts(int limit, final int page, String query, final boolean refresh) {

        RestClient.getReportService().getReports(mAccessToken, "application/json", page, limit, query, new Callback<FeatureCollection>() {

            @Override
            public void success(FeatureCollection featureCollection, Response response) {

                List<Report> reports = featureCollection.getFeatures();

                Log.v("list", reports.toString());

                reportCount = featureCollection.getProperties().num_results;

                if (reportCount > 0) {

                    mUserProfileHeaderView.reportStat.setVisibility(View.VISIBLE);

                    mUserProfileHeaderView.reportCounter.setText(String.valueOf(reportCount));

                    mUserProfileHeaderView.reportCountLabel.setText(resources.getQuantityString(R.plurals.post_label, reportCount, reportCount));

                } else {

                    mUserProfileHeaderView.reportStat.setVisibility(View.GONE);

                    setPostCountState(reportCount);

                }

                if (refresh || reportCollection.isEmpty()) {

                    reportCollection.clear();

                    reportCollection.addAll(reports);

                    scrollListener.resetState();

                    try {

                        timelineAdapter.notifyDataSetChanged();

                        timeLine.smoothScrollToPosition(0);

                    } catch (NullPointerException e) {

                        populateTimeline(reportCollection);

                    }

                } else {

                    if (page > 1) {

                        reportCollection.addAll(reports);

                        timelineAdapter.notifyDataSetChanged();

                    }

                }

                try {

                    timeLineContainer.setRefreshing(false);

                } catch (NullPointerException e) {

                    finish();

                }

            }

            @Override
            public void failure(RetrofitError error) {

                try {

                    timeLineContainer.setRefreshing(false);

                } catch (NullPointerException e) {

                    finish();

                }

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(AuthUserActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void populateTimeline(List<Report> list) {

        timelineAdapter = new TimelineAdapter(AuthUserActivity.this, list, true, false, getSupportFragmentManager());

        // Attach the adapter to a ListView
        if (timeLine != null) {

            timeLine.setAdapter(timelineAdapter);

            attachScrollListener();

        }

    }

    public void viewOptions(View view) {

        startActivity(new Intent(this, ProfileSettingsActivity.class));

    }

    public void showActions() {

        actionFocus = true;

        if (timeLine != null) {

            timeLine.setSelection(0);

        }

        timeLineContainer.setRefreshing(true);

        fetchPosts(5, 1, complexQuery, true);

    }

    public void resetStats() {

        actionFocus = false;

        timeLineContainer.setRefreshing(true);

        fetchPosts(5, 1, buildQuery(true, null), true);

    }

    @Override
    public void onPostDelete(Report post) {

        ReportHolder.setReport(null);

        resetStats();

    }

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

    private void afterPostSend() {

        ApiDispatcher.setTransmissionActive(mSharedPreferences, false);
        mSharedPreferences.edit().putInt("PENDING_IMAGE_ID", 0).apply();
        uploadProgress.setVisibility(View.GONE);
        fetchPosts(5, 1, buildQuery(true, null), true);

    }

    @Override
    protected void onStart() {

        super.onStart();

        // Retrieve stored User object

        retrieveStoredUser();

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
    public void onResume() {

        super.onResume();

        // Retrieve stored User object

        retrieveStoredUser();

        // Check for active transmissions

        if (ApiDispatcher.transmissionActive(this) && uploadProgress != null) {

            uploadProgress.setVisibility(View.VISIBLE);

        }

        // Check for completed request not handled in the receiver's onReceive

        if (ApiDispatcher.getPendingPostId(this) > 0) {

            afterPostSend();

        }

        registerBroadcastReceiver();

    }

    @Override
    public void onPause() {

        super.onPause();

        // Cancel all pending network requests

        //Callback.cancelAll();

        Picasso.with(this).cancelRequest(mUserProfileHeaderView.userAvatar);

        // If the DownloadStateReceiver still exists, unregister it
        if (mUploadStateReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mUploadStateReceiver);
        }

    }

    @Override
    protected void onStop() {

        super.onStop();

        // Cancel all pending network requests

        //Callback.cancelAll();

        Picasso.with(this).cancelRequest(mUserProfileHeaderView.userAvatar);

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        // Cancel all pending network requests

        //Callback.cancelAll();

        Picasso.with(this).cancelRequest(mUserProfileHeaderView.userAvatar);

        ButterKnife.unbind(this);

        // If the DownloadStateReceiver still exists, unregister it and set it to null
        if (mUploadStateReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mUploadStateReceiver);
            mUploadStateReceiver = null;
        }

    }

}