package com.viableindustries.waterreporter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.FeatureCollection;
import com.viableindustries.waterreporter.data.GroupListHolder;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.OrganizationHolder;
import com.viableindustries.waterreporter.data.OrganizationMemberList;
import com.viableindustries.waterreporter.data.OrganizationService;
import com.viableindustries.waterreporter.data.QueryFilter;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.Territory;
import com.viableindustries.waterreporter.data.TerritoryGroupList;
import com.viableindustries.waterreporter.data.TerritoryHolder;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserCollection;
import com.viableindustries.waterreporter.data.UserOrgPatch;
import com.viableindustries.waterreporter.data.UserService;

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

import static java.lang.Boolean.TRUE;

public class TerritoryActivity extends AppCompatActivity {

    LinearLayout profileMeta;

    LinearLayout profileStats;

    LinearLayout reportStat;

    TextView reportCounter;

    TextView reportCountLabel;

    LinearLayout actionStat;

    TextView actionCounter;

    TextView actionCountLabel;

    LinearLayout groupStat;

    TextView groupCounter;

    TextView groupCountLabel;

    TextView territoryName;

    LinearLayout sharePrompt;

    Button jumpStart;

    @Bind(R.id.timeline)
    SwipeRefreshLayout timeLineContainer;

    @Bind(R.id.timeline_items)
    ListView timeLine;

    @Bind(R.id.listTabs)
    FrameLayout listTabs;

    protected TimelineAdapter timelineAdapter;

    protected List<Report> reportCollection = new ArrayList<Report>();

    private String territoryNameText;

    private String complexQuery;

    private ViewGroup.LayoutParams listViewParams;

    private int territoryId;

    private int actionCount = 0;

    private int reportCount = 99999999;

    private boolean actionFocus = false;

    private boolean hasGroups = false;

    private Context context;

    private Territory territory;

    private SharedPreferences prefs;

    private int socialOptions;

    private Resources resources;

    private EndlessScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_territory);

        ButterKnife.bind(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        context = this;

        resources = getResources();

        // Determine which (if any) of Facebook and Twitter
        // can be displayed in the social sharing dialog

        socialOptions = SocialShareUtility.getShareOptions(this);

        territory = TerritoryHolder.getTerritory();

        // Set refresh listener on report feed container

        timeLineContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual data-refresh operation.
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

                // Triggered only when new data needs to be appended to the list

                if (actionFocus) {

                    fetchReports(5, page, complexQuery, false);

                } else {

                    fetchReports(5, page, buildQuery(true, "report", null), false);

                }

                return true; // ONLY if more data is actually being loaded; false otherwise.

            }
        };

        // Add click listener to share button

        jumpStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startPost();

            }
        });


    }

    protected void addListViewHeader() {

        LayoutInflater inflater = getLayoutInflater();

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.territory_header, timeLine, false);

        sharePrompt = (LinearLayout) header.findViewById(R.id.share_cta);

        jumpStart = (Button) header.findViewById(R.id.jump_start);

        // Add click listener to share button

        jumpStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startPost();

            }
        });

        territoryName = (TextView) header.findViewById(R.id.territoryName);

        reportCounter = (TextView) header.findViewById(R.id.reportCount);

        actionCounter = (TextView) header.findViewById(R.id.actionCount);

        groupCounter = (TextView) header.findViewById(R.id.groupCount);

        reportCountLabel = (TextView) header.findViewById(R.id.reportCountLabel);

        actionCountLabel = (TextView) header.findViewById(R.id.actionCountLabel);

        groupCountLabel = (TextView) header.findViewById(R.id.groupCountLabel);

        reportStat = (LinearLayout) header.findViewById(R.id.reportStat);

        actionStat = (LinearLayout) header.findViewById(R.id.actionStat);

        groupStat = (LinearLayout) header.findViewById(R.id.groupStat);

        profileMeta = (LinearLayout) header.findViewById(R.id.profileMeta);

        profileStats = (LinearLayout) header.findViewById(R.id.profileStats);

        try {

            territoryId = territory.id;

        } catch (NullPointerException e) {

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

        territoryNameText = territory.properties.huc_8_name;

        territoryName.setText(territoryNameText);

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

                actionCounter.setTextColor(ContextCompat.getColor(context, R.color.base_blue));
                actionCountLabel.setTextColor(ContextCompat.getColor(context, R.color.base_blue));

                reportCounter.setTextColor(ContextCompat.getColor(context, R.color.material_blue_grey950));
                reportCountLabel.setTextColor(ContextCompat.getColor(context, R.color.material_blue_grey950));

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

                    Intent intent = new Intent(context, RelatedGroupsActivity.class);

                    intent.putExtra("GENERIC_USER", TRUE);

                    startActivity(intent);

                }

            }
        });

        // Add populated header view to report timeline

        timeLine.addHeaderView(header, null, false);

    }

    private void resetStats() {

        reportCounter.setTextColor(ContextCompat.getColor(TerritoryActivity.this, R.color.base_blue));
        reportCountLabel.setTextColor(ContextCompat.getColor(TerritoryActivity.this, R.color.base_blue));

        actionCounter.setTextColor(ContextCompat.getColor(TerritoryActivity.this, R.color.material_blue_grey950));
        actionCountLabel.setTextColor(ContextCompat.getColor(TerritoryActivity.this, R.color.material_blue_grey950));

        actionFocus = false;

        timeLineContainer.setRefreshing(true);

        fetchReports(5, 1, buildQuery(true, "report", null), true);

    }

    protected void countReports(String query, final String filterName) {

        final String accessToken = prefs.getString("access_token", "");

        RestAdapter restAdapter = ReportService.restAdapter;

        ReportService service = restAdapter.create(ReportService.class);

        service.getReports(accessToken, "application/json", 1, 1, query, new Callback<FeatureCollection>() {

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

                        startActivity(new Intent(context, SignInActivity.class));

                    }

                }

            }

        });

    }

    protected void fetchOrganizations(int limit, int page, final String query) {

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        RestAdapter restAdapter = OrganizationService.restAdapter;

        OrganizationService service = restAdapter.create(OrganizationService.class);

        service.getOrganizations(accessToken, "application/json", page, limit, query, new Callback<OrganizationFeatureCollection>() {

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

                        startActivity(new Intent(context, SignInActivity.class));

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

            queryOrder = new ArrayList<QuerySort>();

            QuerySort querySort = new QuerySort("created", "desc");

            queryOrder.add(querySort);

        }

        if (collection.equals("group")) {

            QueryFilter territoryFilter = new QueryFilter("huc_8_name", "eq", territory.properties.huc_8_name);

            QueryFilter complexVal = new QueryFilter("territory", "has", territoryFilter);

            QueryFilter complexFilter = new QueryFilter("reports", "any", complexVal);

            queryFilters.add(complexFilter);

        } else {

            QueryFilter complexVal = new QueryFilter("huc_8_name", "eq", territory.properties.huc_8_name);

            QueryFilter territoryFilter = new QueryFilter("territory", "has", complexVal);

            queryFilters.add(territoryFilter);

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

    private void fetchReports(int limit, final int page, String query, final boolean refresh) {

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        Log.d("URL", query);

        RestAdapter restAdapter = ReportService.restAdapter;

        ReportService service = restAdapter.create(ReportService.class);

        service.getReports(accessToken, "application/json", page, limit, query, new Callback<FeatureCollection>() {

            @Override
            public void success(FeatureCollection featureCollection, Response response) {

                List<Report> reports = featureCollection.getFeatures();

                Log.v("list", reports.toString());

                if (reportCount == 99999999) {

                    reportCount = featureCollection.getProperties().num_results;

                }

                if (reportCount > 0) {

                    sharePrompt.setVisibility(View.GONE);

                    reportStat.setVisibility(View.VISIBLE);

                    reportCounter.setText(String.valueOf(reportCount));

                    reportCountLabel.setText(resources.getQuantityString(R.plurals.post_label, reportCount, reportCount));

                } else {

                    try {

                        reportStat.setVisibility(View.GONE);

                        sharePrompt.setVisibility(View.VISIBLE);

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

                        startActivity(new Intent(context, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void populateTimeline(List<Report> list) {

        timelineAdapter = new TimelineAdapter(this, list, false, socialOptions);

        // Attach the adapter to a ListView
        if (timeLine != null) {

            timeLine.setAdapter(timelineAdapter);

            attachScrollListener();

        }

    }

    private void startPost() {

        Intent intent = new Intent(this, PhotoMetaActivity.class);

        intent.putExtra("autoTag", String.format("\u0023%s", territoryName.getText().toString().replaceAll("[^a-zA-Z0-9]+", "")));

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
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

    }

}
