package com.viableindustries.waterreporter;

import android.content.Context;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.models.FeatureCollection;
import com.viableindustries.waterreporter.api.models.organization.GroupListHolder;
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.organization.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.query.QueryFilter;
import com.viableindustries.waterreporter.api.models.query.QueryParams;
import com.viableindustries.waterreporter.api.models.query.QuerySort;
import com.viableindustries.waterreporter.user_interface.adapters.TimelineAdapter;

import com.viableindustries.waterreporter.utilities.EndlessScrollListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static java.lang.Boolean.TRUE;

public class TagProfileActivity extends AppCompatActivity {

    private TextView tagNameView;

    private LinearLayout reportStat;

    private TextView reportCounter;

    private TextView reportCountLabel;

    private LinearLayout actionStat;

    private TextView actionCounter;

    private TextView actionCountLabel;

    private LinearLayout groupStat;

    private TextView groupCounter;

    private TextView groupCountLabel;

    private LinearLayout promptBlock;

    private TextView promptMessage;

    @Bind(R.id.timeline)
    SwipeRefreshLayout timeLineContainer;

    @Bind(R.id.timeline_items)
    ListView timeLine;

    @Bind(R.id.listTabs)
    FrameLayout listTabs;

    private TimelineAdapter timelineAdapter;

    private final List<Report> reportCollection = new ArrayList<>();

    private String complexQuery;

    private int actionCount = 0;

    private int reportCount = 99999999;

    private boolean actionFocus = false;

    private boolean hasGroups = false;

    private Context mContext;

    private SharedPreferences mSharedPreferences;

    private int socialOptions;

    private String tagName;

    private Resources resources;

    private EndlessScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tag);

        ButterKnife.bind(this);

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        mContext = this;

        resources = getResources();

        // Retrieve tag name from intent

        try {

            Bundle extras = getIntent().getExtras();

            tagName = extras.getString("tag");

        } catch (NullPointerException e) {

            finish();

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

        complexQuery = buildQuery(true, "report", new String[][]{
                {"state", "eq", "closed"}
        });

        countReports(complexQuery, "state");

        // Count related groups

        fetchOrganizations(10, 1, buildQuery(false, "group", null));

        // Retrieve first batch of posts

        if (reportCollection.isEmpty()) {

            timeLineContainer.setRefreshing(true);

            fetchReports(5, 1, buildQuery(true, "report", null), false);

        }

        scrollListener = new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new api needs to be appended to the list

                if (actionFocus) {

                    fetchReports(5, page, complexQuery, false);

                } else {

                    fetchReports(5, page, buildQuery(true, "report", null), false);

                }

                return true; // ONLY if more api is actually being loaded; false otherwise.

            }
        };

    }

    private void addListViewHeader() {

        LayoutInflater inflater = getLayoutInflater();

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.tag_profile_header, timeLine, false);

        promptBlock = (LinearLayout) header.findViewById(R.id.promptBlock);
        promptMessage = (TextView) header.findViewById(R.id.prompt);
        Button startPostButton = (Button) header.findViewById(R.id.startPost);

        // Add text and click listener to startPostButton

        startPostButton.setText(getString(R.string.share_post_prompt));

        startPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPost();
            }
        });

        tagNameView = (TextView) header.findViewById(R.id.tag_name);
        tagNameView.setText(tagName);

        reportCounter = (TextView) header.findViewById(R.id.reportCount);

        actionCounter = (TextView) header.findViewById(R.id.actionCount);

        reportCountLabel = (TextView) header.findViewById(R.id.reportCountLabel);

        actionCountLabel = (TextView) header.findViewById(R.id.actionCountLabel);

        reportStat = (LinearLayout) header.findViewById(R.id.reportStat);

        actionStat = (LinearLayout) header.findViewById(R.id.actionStat);

        groupCounter = (TextView) header.findViewById(R.id.groupCount);

        groupCountLabel = (TextView) header.findViewById(R.id.groupCountLabel);

        groupStat = (LinearLayout) header.findViewById(R.id.groupStat);

        LinearLayout profileStats = (LinearLayout) header.findViewById(R.id.profileStats);

        // Attach click listeners to stat elements

        reportStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resetStats();

            }
        });

        actionStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                actionFocus = true;

                actionCounter.setTextColor(ContextCompat.getColor(mContext, R.color.base_blue));
                actionCountLabel.setTextColor(ContextCompat.getColor(mContext, R.color.base_blue));

                reportCounter.setTextColor(ContextCompat.getColor(mContext, R.color.material_blue_grey950));
                reportCountLabel.setTextColor(ContextCompat.getColor(mContext, R.color.material_blue_grey950));

                if (timeLine != null) {

                    timeLine.setSelection(0);

                }

                timeLineContainer.setRefreshing(true);

                fetchReports(5, 1, complexQuery, true);

            }
        });

        groupStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hasGroups) {

                    Intent intent = new Intent(mContext, RelatedGroupsActivity.class);

                    intent.putExtra("GENERIC_USER", TRUE);

                    startActivity(intent);

                }

            }
        });

        // Add populated header view to report timeline

        timeLine.addHeaderView(header, null, false);

    }

    private void resetStats() {

        reportCounter.setTextColor(ContextCompat.getColor(TagProfileActivity.this, R.color.base_blue));
        reportCountLabel.setTextColor(ContextCompat.getColor(TagProfileActivity.this, R.color.base_blue));

        actionCounter.setTextColor(ContextCompat.getColor(TagProfileActivity.this, R.color.material_blue_grey950));
        actionCountLabel.setTextColor(ContextCompat.getColor(TagProfileActivity.this, R.color.material_blue_grey950));

        actionFocus = false;

        timeLineContainer.setRefreshing(true);

        fetchReports(5, 1, buildQuery(true, "report", null), true);

    }

    private void setReportCountState(int count) {

        reportCounter.setText(String.valueOf(reportCount));
        reportCountLabel.setText(resources.getQuantityString(R.plurals.post_label, reportCount, reportCount));

        if (count < 1) {

            promptBlock.setVisibility(View.VISIBLE);

            promptMessage.setText(getString(R.string.prompt_no_posts_hashtag));

        } else {

            timeLineContainer.setVisibility(View.VISIBLE);

        }

    }

    private void countReports(String query, final String filterName) {

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getReportService().getReports(accessToken, "application/json", 1, 1, query, new Callback<FeatureCollection>() {

            @Override
            public void success(FeatureCollection featureCollection, Response response) {

                int count = featureCollection.getProperties().num_results;

                switch (filterName) {
                    case "state":
                        if (count > 0) {
                            actionStat.setVisibility(View.VISIBLE);
                            actionCount = count;
                            actionCounter.setText(String.valueOf(actionCount));
                            actionCountLabel.setText(resources.getQuantityString(R.plurals.action_label, actionCount, actionCount));
                        }
                        break;
                    default:
                        reportCount = count;
                        reportCounter.setText(String.valueOf(reportCount));
                        reportCountLabel.setText(resources.getQuantityString(R.plurals.post_label, reportCount, reportCount));
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

                        startActivity(new Intent(mContext, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void attachScrollListener() {

        timeLine.setOnScrollListener(scrollListener);

    }

    private String buildQuery(boolean order, String collection, String[][] optionalFilters) {

        List<QuerySort> queryOrder = null;

        List<Object> queryFilters = new ArrayList<>();

        // Create order_by list and add a sort parameter

        if (order) {

            queryOrder = new ArrayList<>();

            QuerySort querySort = new QuerySort("created", "desc");

            queryOrder.add(querySort);

        }

        // Create filter list and add a filter parameter

        if (collection.equals("group")) {

            // Set tag filter to match from beginning of token

            QueryFilter tagFilter = new QueryFilter("tag", "ilike", String.format("%s%s", tagName.replace("#", ""), "%"));

            QueryFilter complexVal = new QueryFilter("tags", "any", tagFilter);

            QueryFilter complexFilter = new QueryFilter("reports", "any", complexVal);

            queryFilters.add(complexFilter);

        } else {

            // Set tag filter to match from beginning of token

            QueryFilter tagFilter = new QueryFilter("tag", "ilike", String.format("%s%s", tagName.replace("#", ""), "%"));

            QueryFilter complexVal = new QueryFilter("tags", "any", tagFilter);

            queryFilters.add(complexVal);

            if (optionalFilters != null) {

                for (String[] filterComponents : optionalFilters) {

                    QueryFilter optionalFilter = new QueryFilter(filterComponents[0], filterComponents[1], filterComponents[2]);

                    queryFilters.add(optionalFilter);

                }

            }

        }

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(queryFilters, queryOrder);

        return new Gson().toJson(queryParams);

    }

    private void fetchOrganizations(int limit, int page, final String query) {

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getOrganizationService().getOrganizations(accessToken, "application/json", page, limit, query, new Callback<OrganizationFeatureCollection>() {

            @Override
            public void success(OrganizationFeatureCollection organizationFeatureCollection, Response response) {

                ArrayList<Organization> organizations = organizationFeatureCollection.getFeatures();

                if (!organizations.isEmpty()) {

                    int groupCount = organizations.size();

                    groupCounter.setText(String.valueOf(groupCount));
                    groupCountLabel.setText(resources.getQuantityString(R.plurals.group_label, groupCount, groupCount));

                    groupStat.setVisibility(View.VISIBLE);

                    GroupListHolder.setList(organizations);

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

                        startActivity(new Intent(mContext, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void fetchReports(int limit, final int page, String query, final boolean refresh) {

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

                    reportStat.setVisibility(View.VISIBLE);

                    reportCounter.setText(String.valueOf(reportCount));

                    reportCountLabel.setText(resources.getQuantityString(R.plurals.post_label, reportCount, reportCount));

                } else {

                    try {

                        reportStat.setVisibility(View.GONE);

                        setReportCountState(reportCount);

                    } catch (NullPointerException e) {

                        finish();

                    }

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

                        startActivity(new Intent(mContext, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void populateTimeline(List<Report> list) {

        timelineAdapter = new TimelineAdapter(this, list, false, false, getSupportFragmentManager());

        // Attach the adapter to a ListView
        if (timeLine != null) {

            timeLine.setAdapter(timelineAdapter);

            attachScrollListener();

        }

    }

    private void startPost() {

        Intent intent = new Intent(this, PhotoMetaActivity.class);

        intent.putExtra("autoTag", tagNameView.getText().toString());

        startActivity(intent);

        this.overridePendingTransition(R.anim.animation_enter_right,
                R.anim.animation_exit_left);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {

        super.onStop();

        // Cancel all pending network requests

        //Callback.cancelAll();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

        // Cancel all pending network requests

        //Callback.cancelAll();

    }

}