package com.viableindustries.waterreporter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.FeatureCollection;
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
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserCollection;
import com.viableindustries.waterreporter.data.UserFeatureCollection;
import com.viableindustries.waterreporter.data.UserGroupList;
import com.viableindustries.waterreporter.data.UserOrgPatch;
import com.viableindustries.waterreporter.data.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static java.lang.Boolean.TRUE;
import static java.security.AccessController.getContext;

public class OrganizationProfileActivity extends AppCompatActivity {

    LinearLayout profileMeta;

    LinearLayout profileStats;

    LinearLayout reportStat;

    TextView reportCounter;

    TextView reportCountLabel;

    LinearLayout actionStat;

    TextView actionCounter;

    TextView actionCountLabel;

    LinearLayout peopleStat;

    TextView peopleCounter;

    TextView peopleCountLabel;

    TextView organizationName;

    TextView organizationDescription;

    ImageView organizationLogo;

    Button joinOrganization;

    @Bind(R.id.timeline)
    SwipeRefreshLayout timeLineContainer;

    @Bind(R.id.timeline_items)
    ListView timeLine;

    @Bind(R.id.listTabs)
    FrameLayout listTabs;

    protected TimelineAdapter timelineAdapter;

    protected List<Report> reportCollection = new ArrayList<Report>();

    private String organizationDescriptionText;

    private String organizationNameText;

    private String organizationLogoUrl;

    private String complexQuery;

    private ViewGroup.LayoutParams listViewParams;

    private int organizationId;

    private int actionCount = 0;

    private int reportCount = 99999999;

    private boolean actionFocus = false;

    private boolean hasScrolled = false;

    private boolean hasMembers = false;

    private Context context;

    private Organization organization;

    private SharedPreferences prefs;

    private SharedPreferences groupPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_organization_profile);

        ButterKnife.bind(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        groupPrefs = getSharedPreferences(getString(R.string.group_membership_key), 0);

        context = this;

        organization = OrganizationHolder.getOrganization();

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

        // Retrieve the organization's members

        fetchOrganizationMembers(50, 1, organizationId);

        // Retrieve first batch of user's reports

        if (reportCollection.isEmpty()) {

            timeLineContainer.setRefreshing(true);

            fetchReports(10, 1, buildQuery(true, null), false, false);

        }

    }

    private void joinOrganization(final Organization organization) {

        // Retrieve API token

        final String accessToken = prefs.getString("access_token", "");

        // Retrieve user ID

        int id = prefs.getInt("user_id", 0);

        // Build request object

        Map<String, Map> userPatch = UserOrgPatch.buildRequest(organization.id, "add");

        UserService service = UserService.restAdapter.create(UserService.class);

        service.updateUserOrganization(accessToken, "application/json", id, userPatch, new Callback<User>() {

            @Override
            public void success(User user, Response response) {

                String action;

                action = "joined";

                joinOrganization.setVisibility(View.GONE);

                groupPrefs.edit().putInt(organization.properties.name, organization.properties.id).apply();

                CharSequence text = String.format("Successfully %s %s", action, organization.properties.name);
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

            }

            @Override
            public void failure(RetrofitError error) {

                Response response = error.getResponse();

                int status = response.getStatus();

                error.printStackTrace();

            }

        });

    }

    protected void addListViewHeader() {

        LayoutInflater inflater = getLayoutInflater();

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.organization_profile_header, timeLine, false);

        organizationName = (TextView) header.findViewById(R.id.organizationName);

        organizationDescription = (TextView) header.findViewById(R.id.organizationDescription);

        organizationLogo = (ImageView) header.findViewById(R.id.organizationLogo);

        joinOrganization = (Button) header.findViewById(R.id.group_membership_button);

        reportCounter = (TextView) header.findViewById(R.id.reportCount);

        actionCounter = (TextView) header.findViewById(R.id.actionCount);

        peopleCounter = (TextView) header.findViewById(R.id.peopleCount);

        reportCountLabel = (TextView) header.findViewById(R.id.reportCountLabel);

        actionCountLabel = (TextView) header.findViewById(R.id.actionCountLabel);

        peopleCountLabel = (TextView) header.findViewById(R.id.peopleCountLabel);

        reportStat = (LinearLayout) header.findViewById(R.id.reportStat);

        actionStat = (LinearLayout) header.findViewById(R.id.actionStat);

        peopleStat = (LinearLayout) header.findViewById(R.id.peopleStat);

        profileMeta = (LinearLayout) header.findViewById(R.id.profileMeta);

        profileStats = (LinearLayout) header.findViewById(R.id.profileStats);

        try {

            organizationId = organization.id;

        } catch (NullPointerException e) {

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

        // Check group membership

        int selected = groupPrefs.getInt(organization.properties.name, 0);

        if (selected == 0) {

            joinOrganization.setVisibility(View.VISIBLE);

            joinOrganization.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    joinOrganization(organization);

                }

            });

        }

        organizationDescriptionText = organization.properties.description;
        organizationNameText = organization.properties.name;
        organizationLogoUrl = organization.properties.picture;

        organizationName.setText(organizationNameText);

        Picasso.with(this).load(organizationLogoUrl).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(organizationLogo);

        try {

            organizationDescription.setText(organizationDescriptionText);

            organizationDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ObjectAnimator animation;

                    int duration;

                    int maxLines = TextViewCompat.getMaxLines(organizationDescription);

                    if (maxLines == 2) {

                        organizationDescription.setEllipsize(null);

                        animation = ObjectAnimator.ofInt(
                                organizationDescription,
                                "maxLines",
                                2,
                                20);

                        duration = 350;

                    } else {

                        organizationDescription.setEllipsize(TextUtils.TruncateAt.END);

                        animation = ObjectAnimator.ofInt(
                                organizationDescription,
                                "maxLines",
                                20,
                                2);

                        duration = 200;

                    }

                    animation.setDuration(100);
                    animation.setInterpolator(new LinearOutSlowInInterpolator());
                    animation.start();

                }
            });

        } catch (NullPointerException ne) {

            organizationDescription.setVisibility(View.GONE);

        }

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

                fetchReports(10, 1, complexQuery, false, true);

            }
        });

        peopleStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hasMembers) {

                    Intent intent = new Intent(context, OrganizationMembersActivity.class);

                    startActivity(intent);

                }

            }
        });

        // Add populated header view to report timeline

        timeLine.addHeaderView(header, null, false);

    }

    private void resetStats() {

        reportCounter.setTextColor(ContextCompat.getColor(OrganizationProfileActivity.this, R.color.base_blue));
        reportCountLabel.setTextColor(ContextCompat.getColor(OrganizationProfileActivity.this, R.color.base_blue));

        actionCounter.setTextColor(ContextCompat.getColor(OrganizationProfileActivity.this, R.color.material_blue_grey950));
        actionCountLabel.setTextColor(ContextCompat.getColor(OrganizationProfileActivity.this, R.color.material_blue_grey950));

        actionFocus = false;

        timeLineContainer.setRefreshing(true);

        fetchReports(10, 1, buildQuery(true, null), true, true);

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

                        }
                        break;
                    default:
                        reportCount = count;
                        reportCounter.setText(String.valueOf(reportCount));
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

    protected void fetchOrganizationMembers(int limit, int page, int organizationId) {

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        OrganizationService service = OrganizationService.restAdapter.create(OrganizationService.class);

        service.getOrganizationMembers(accessToken, "application/json", organizationId, page, limit, null, new Callback<UserCollection>() {

            @Override
            public void success(UserCollection userCollection, Response response) {

                ArrayList<User> members = userCollection.getFeatures();

                if (!members.isEmpty()) {

                    peopleCounter.setText(String.valueOf(userCollection.getProperties().num_results));

                    peopleStat.setVisibility(View.VISIBLE);

                    OrganizationMemberList.setList(members);

                    hasMembers = true;

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

                    fetchReports(10, page, complexQuery, false, false);

                } else {

                    fetchReports(10, page, buildQuery(true, null), false, false);

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

        List<QueryFilter> queryFilters = new ArrayList<QueryFilter>();

        QueryFilter userFilter = new QueryFilter("groups__id", "any", organizationId);

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

        timelineAdapter = new TimelineAdapter(context, list, false);

        // Attach the adapter to a ListView
        timeLine.setAdapter(timelineAdapter);

        attachScrollListener();

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

        Picasso.with(this).cancelRequest(organizationLogo);

        ButterKnife.unbind(this);

    }

}
