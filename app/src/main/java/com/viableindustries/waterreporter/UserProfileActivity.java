package com.viableindustries.waterreporter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

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
import com.viableindustries.waterreporter.user_interface.adapters.TimelineAdapter;
import com.viableindustries.waterreporter.user_interface.dialogs.ReportActionDialogListener;
import com.viableindustries.waterreporter.user_interface.view_holders.UserProfileHeaderView;
import com.viableindustries.waterreporter.utilities.EndlessScrollListener;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UserProfileActivity extends AppCompatActivity implements
        ReportActionDialogListener,
        UserProfileHeaderView.UserProfileHeaderCallback {

    @Bind(R.id.timeline)
    SwipeRefreshLayout timeLineContainer;

    @Bind(R.id.timeline_items)
    ListView timeLine;

    @Bind(R.id.listTabs)
    FrameLayout listTabs;

    private TimelineAdapter timelineAdapter;

    private final List<Report> reportCollection = new ArrayList<>();

    private String complexQuery;

    private int userId;

    private int actionCount;

    private int reportCount = 99999999;

    private boolean actionFocus = false;

    private boolean hasGroups = false;

    private SharedPreferences mSharedPreferences;

    private User user;

    private Resources resources;

    private EndlessScrollListener scrollListener;

    private UserProfileHeaderView mUserProfileHeaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile);

        ButterKnife.bind(this);

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        SharedPreferences coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

        Log.d("storedavatar", coreProfile.getString("picture", ""));

        resources = getResources();

        // Retrieve stored User object

        retrieveStoredUser();

        // Set refresh listener on report feed container

        timeLineContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual api-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.

                        countPosts(complexQuery, "state");

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

        countPosts(complexQuery, "state");

        // Retrieve the user's groups

        fetchUserGroups(userId);

        scrollListener = new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new data should be appended to the list

                if (actionFocus) {

                    fetchPosts(5, page, complexQuery, false);

                } else {

                    fetchPosts(5, page, buildQuery(true, null), false);

                }

                return true; // ONLY if more data are actually being loaded; false otherwise.

            }
        };

    }

    private void retrieveStoredUser() {

        user = ModelStorage.getStoredUser(mSharedPreferences);

        try {

            userId = user.properties.id;

            // Retrieve first batch of user's reports

            if (reportCollection.isEmpty()) {

                timeLineContainer.setRefreshing(true);

                fetchPosts(5, 1, buildQuery(true, null), false);

            }

        } catch (NullPointerException _e) {

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

    }

    private void addListViewHeader() {

        mUserProfileHeaderView = new UserProfileHeaderView();

        LayoutInflater inflater = getLayoutInflater();

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.user_profile_header, timeLine, false);

        mUserProfileHeaderView.buildHeader(this, header, user);

        timeLine.addHeaderView(header, null, false);

    }

    private void setPostCountState(int count) {

        mUserProfileHeaderView.reportCounter.setText(String.valueOf(reportCount));
        mUserProfileHeaderView.reportCountLabel.setText(resources.getQuantityString(R.plurals.post_label, reportCount, reportCount));

        if (count < 1) {

            try {

                mUserProfileHeaderView.startPostButton.setVisibility(View.GONE);

                mUserProfileHeaderView.promptBlock.setVisibility(View.VISIBLE);

                mUserProfileHeaderView.promptMessage.setText(getString(R.string.prompt_no_posts_user, user.properties.first_name));

            } catch (NullPointerException e) {

                finish();

            }

        } else {

            timeLineContainer.setVisibility(View.VISIBLE);

        }

    }

    private void countPosts(String query, final String filterName) {

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getReportService().getReports(accessToken, "application/json", 1, 1, query, new Callback<FeatureCollection>() {

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

                        startActivity(new Intent(UserProfileActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void fetchUserGroups(int userId) {

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getUserService().getUserGroups(accessToken, "application/json", userId, new Callback<GroupFeatureCollection>() {

            @Override
            public void success(GroupFeatureCollection groupFeatureCollection, Response response) {

                ArrayList<Group> groups = groupFeatureCollection.getFeatures();

                if (!groups.isEmpty()) {

                    int groupCount = groups.size();

                    mUserProfileHeaderView.groupCounter.setText(String.valueOf(groupCount));
                    mUserProfileHeaderView.groupCountLabel.setText(resources.getQuantityString(R.plurals.group_label, groupCount, groupCount));

                    mUserProfileHeaderView.groupStat.setVisibility(View.VISIBLE);

                    UserGroupList.setList(groups);

                    hasGroups = true;

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

                        startActivity(new Intent(UserProfileActivity.this, SignInActivity.class));

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

    private void fetchPosts(int limit, final int page, String query, final boolean refresh) {

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getReportService().getReports(accessToken, "application/json", page, limit, query, new Callback<FeatureCollection>() {

            @Override
            public void success(FeatureCollection featureCollection, Response response) {

                List<Report> reports = featureCollection.getFeatures();

                Log.v("list", reports.toString());

                if (reportCount == 99999999) {

                    reportCount = featureCollection.getProperties().num_results;

                }

                if (reportCount > 0) {

                    mUserProfileHeaderView.reportStat.setVisibility(View.VISIBLE);

                    mUserProfileHeaderView.reportCounter.setText(String.valueOf(reportCount));

                    mUserProfileHeaderView.reportCountLabel.setText(resources.getQuantityString(R.plurals.post_label, reportCount, reportCount));

                    setPostCountState(reportCount);

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

                        startActivity(new Intent(UserProfileActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void populateTimeline(List<Report> list) {

        timelineAdapter = new TimelineAdapter(UserProfileActivity.this, list, true, false, getSupportFragmentManager());

        // Attach the adapter to a ListView

        if (timeLine != null) {

            timeLine.setAdapter(timelineAdapter);

            /* IMPORTANT
            Don't set a scroll listener unless necessary,
            otherwise it may trigger infinite API requests
            when empty collection messages overflow the screen.
            */

            if (list.size() > 1) attachScrollListener();

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

        mUserProfileHeaderView.reportCounter.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.base_blue));
        mUserProfileHeaderView.reportCountLabel.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.base_blue));

        mUserProfileHeaderView.actionCounter.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.material_blue_grey950));
        mUserProfileHeaderView.actionCountLabel.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.material_blue_grey950));

        actionFocus = false;

        timeLineContainer.setRefreshing(true);

        fetchPosts(5, 1, buildQuery(true, null), true);

    }

    private void deleteReport() {

        timeLineContainer.setRefreshing(true);

        final String accessToken = mSharedPreferences.getString("access_token", "");

        final Report report = ReportHolder.getReport();

        RestClient.getReportService().deleteSingleReport(accessToken, report.id, new Callback<Response>() {

            @Override
            public void success(Response response, Response response_) {

                reportCount -= 1;

                if (reportCount > 0) {

                    mUserProfileHeaderView.reportCounter.setText(String.valueOf(reportCount));

                } else {

                    mUserProfileHeaderView.reportStat.setVisibility(View.GONE);

                }

                if ("closed".equals(report.properties.state)) {

                    actionCount -= 1;

                    if (actionCount > 0) {

                        mUserProfileHeaderView.actionCounter.setText(String.valueOf(actionCount));

                    } else {

                        mUserProfileHeaderView.actionStat.setVisibility(View.GONE);

                    }

                }

                ReportHolder.setReport(null);

                resetStats();

            }

            @Override
            public void failure(RetrofitError error) {

                timeLineContainer.setRefreshing(false);

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(UserProfileActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void editReport() {

        Intent intent = new Intent(UserProfileActivity.this, PhotoMetaActivity.class);

        intent.putExtra("EDIT_MODE", true);

        startActivity(intent);

    }

    @Override
    public void onSelectAction(int index) {

        if (index == 1) {

            deleteReport();

        } else {

            editReport();

        }

    }

    @Override
    public void onResume() {

        super.onResume();

        // Retrieve stored User object

        if (user == null) retrieveStoredUser();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        Picasso.with(this).cancelRequest(mUserProfileHeaderView.userAvatar);

        ButterKnife.unbind(this);

        ModelStorage.removeModel(mSharedPreferences, "stored_user");

        // Cancel all pending network requests

        //Callback.cancelAll();

    }

}