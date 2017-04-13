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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.viableindustries.waterreporter.data.FeatureCollection;
import com.viableindustries.waterreporter.data.QueryFilter;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.dialogs.ShareActionDialogListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static java.lang.Boolean.TRUE;

public class TagProfileActivity extends AppCompatActivity implements ShareActionDialogListener {

    TextView tagNameView;

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

    @Bind(R.id.timeline)
    SwipeRefreshLayout timeLineContainer;

    @Bind(R.id.timeline_items)
    ListView timeLine;

    @Bind(R.id.listTabs)
    FrameLayout listTabs;

    protected TimelineAdapter timelineAdapter;

    protected List<Report> reportCollection = new ArrayList<Report>();

    private String complexQuery;

    private int actionCount = 0;

    private int reportCount = 99999999;

    private boolean actionFocus = false;

    private boolean hasGroups = false;

    private Context context;

    private SharedPreferences prefs;

    private int socialOptions;

    private String tagName;

    private Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tag);

        ButterKnife.bind(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        context = this;

        resources = getResources();

        // Retrieve tag name from intent

        try {

            Bundle extras = getIntent().getExtras();

            tagName = extras.getString("tag");

        } catch (NullPointerException e) {

            finish();

        }

        // Determine which (if any) of Facebook and Twitter
        // can be displayed in the social sharing dialog

        socialOptions = SocialShareUtility.getShareOptions(this);

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

        complexQuery = buildQuery(true, new String[][]{
                {"state", "eq", "closed"}
        });

        countReports(complexQuery, "state");

        // Retrieve first batch of posts

        if (reportCollection.isEmpty()) {

            timeLineContainer.setRefreshing(true);

            fetchReports(5, 1, buildQuery(true, null), false, false);

        }

    }

    protected void addListViewHeader() {

        LayoutInflater inflater = getLayoutInflater();

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.tag_profile_header, timeLine, false);

        tagNameView = (TextView) header.findViewById(R.id.tag_name);
//        tagNameView.setText(String.format("\u0023%s", tagName));
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

        profileStats = (LinearLayout) header.findViewById(R.id.profileStats);

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

                fetchReports(5, 1, complexQuery, false, true);

            }
        });

        groupStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hasGroups) {

                    Intent intent = new Intent(context, UserGroupsActivity.class);

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

        fetchReports(5, 1, buildQuery(true, null), true, true);

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

    private void attachScrollListener() {

        timeLine.setOnScrollListener(new EndlessScrollListener() {

            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new data needs to be appended to the list

                if (actionFocus) {

                    fetchReports(5, page, complexQuery, false, false);

                } else {

                    fetchReports(5, page, buildQuery(true, null), false, false);

                }

                return true; // ONLY if more data is actually being loaded; false otherwise.

            }

        });

    }

    private String buildQuery(boolean order, String[][] optionalFilters) {

        List<QuerySort> queryOrder = null;

        // Create order_by list and add a sort parameter

        if (order) {

            queryOrder = new ArrayList<QuerySort>();

            QuerySort querySort = new QuerySort("created", "desc");

            queryOrder.add(querySort);

        }

        // Create filter list and add a filter parameter

        List<Object> queryFilters = new ArrayList<>();

        QueryFilter userFilter = new QueryFilter("tags__tag", "any", tagName.replace("#", ""));

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

    private void fetchReports(int limit, int page, String query, final boolean refresh, final boolean replace) {

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

                    if (reportCount > 0) {

                        reportStat.setVisibility(View.VISIBLE);

                        reportCounter.setText(String.valueOf(reportCount));

                        reportCountLabel.setText(resources.getQuantityString(R.plurals.post_label, reportCount, reportCount));

                    } else {

                        reportStat.setVisibility(View.GONE);

                    }

                }

                if (!reports.isEmpty()) {

                    reportCollection.addAll(reports);

                    if (replace) {

                        reportCollection = reports;

                        populateTimeline(reportCollection);

                    } else {

                        try {

                            timelineAdapter.notifyDataSetChanged();

                        } catch (NullPointerException ne) {

                            populateTimeline(reportCollection);

                        }

                    }

                } else {

                    reportCollection = reports;

                    populateTimeline(reportCollection);

                }

                if (refresh) {

                    reportCollection = reports;

                    reportCount = featureCollection.getProperties().num_results;

                    if (reportCount > 0) {

                        reportStat.setVisibility(View.VISIBLE);

                        reportCounter.setText(String.valueOf(reportCount));

                    } else {

                        reportStat.setVisibility(View.GONE);

                    }

                    populateTimeline(reportCollection);

                }

                timeLineContainer.setRefreshing(false);

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

                        startActivity(new Intent(context, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void populateTimeline(List list) {

        timelineAdapter = new TimelineAdapter(this, list, false, socialOptions);

        // Attach the adapter to a ListView
        timeLine.setAdapter(timelineAdapter);

        attachScrollListener();

    }

    @Override
    public void onSelectShareAction(int index) {

        String target = getResources().getStringArray(socialOptions)[index];

        if (target.toLowerCase().contains("facebook")) {

            SocialShareUtility.shareOnFacebook(this);

        } else {

            SocialShareUtility.shareOnTwitter(this);

        }

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
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

    }

}
