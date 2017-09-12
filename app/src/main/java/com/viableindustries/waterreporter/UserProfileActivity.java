package com.viableindustries.waterreporter;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
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

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.interfaces.api.post.ReportService;
import com.viableindustries.waterreporter.data.interfaces.api.user.UserService;
import com.viableindustries.waterreporter.data.objects.FeatureCollection;
import com.viableindustries.waterreporter.data.objects.organization.Organization;
import com.viableindustries.waterreporter.data.objects.organization.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.objects.post.Report;
import com.viableindustries.waterreporter.data.objects.post.ReportHolder;
import com.viableindustries.waterreporter.data.objects.query.QueryFilter;
import com.viableindustries.waterreporter.data.objects.query.QueryParams;
import com.viableindustries.waterreporter.data.objects.query.QuerySort;
import com.viableindustries.waterreporter.data.objects.user.User;
import com.viableindustries.waterreporter.data.objects.user.UserGroupList;
import com.viableindustries.waterreporter.data.objects.user.UserHolder;
import com.viableindustries.waterreporter.user_interface.adapters.TimelineAdapter;
import com.viableindustries.waterreporter.user_interface.dialogs.ReportActionDialogListener;
import com.viableindustries.waterreporter.utilities.CancelableCallback;
import com.viableindustries.waterreporter.utilities.CircleTransform;
import com.viableindustries.waterreporter.utilities.EndlessScrollListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static java.lang.Boolean.TRUE;

public class UserProfileActivity extends AppCompatActivity implements ReportActionDialogListener {

    private TextView userDescription;

    private ImageView userAvatar;

    private TextView reportCounter;

    private TextView actionCounter;

    private TextView groupCounter;

    private TextView reportCountLabel;

    private TextView actionCountLabel;

    private TextView groupCountLabel;

    private LinearLayout reportStat;

    private LinearLayout actionStat;

    private LinearLayout groupStat;

    @Bind(R.id.timeline)
    SwipeRefreshLayout timeLineContainer;

    @Bind(R.id.timeline_items)
    ListView timeLine;

    @Bind(R.id.listTabs)
    FrameLayout listTabs;

    private LinearLayout promptBlock;

    private TextView promptMessage;

    private Button startPostButton;

    private TimelineAdapter timelineAdapter;

    private final List<Report> reportCollection = new ArrayList<>();

    private String complexQuery;

    private int userId;

    private int actionCount;

    private int reportCount = 99999999;

    private boolean actionFocus = false;

    private boolean hasGroups = false;

    private SharedPreferences prefs;

    private User user;

    private int socialOptions;

    private Resources resources;

    private EndlessScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile);

        ButterKnife.bind(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        SharedPreferences coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

        Log.d("storedavatar", coreProfile.getString("picture", ""));

        resources = getResources();

        // Retrieve stored User object

        user = UserHolder.getUser();

        try {

            userId = user.properties.id;

        } catch (NullPointerException e) {

            startActivity(new Intent(this, MainActivity.class));

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

    private void addListViewHeader() {

        LayoutInflater inflater = getLayoutInflater();

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.user_profile_header, timeLine, false);

        promptBlock = (LinearLayout) header.findViewById(R.id.promptBlock);
        promptMessage = (TextView) header.findViewById(R.id.prompt);
        startPostButton = (Button) header.findViewById(R.id.startPost);

        TextView userName = (TextView) header.findViewById(R.id.userName);

        TextView userTitle = (TextView) header.findViewById(R.id.userTitle);

        userDescription = (TextView) header.findViewById(R.id.userDescription);

        userAvatar = (ImageView) header.findViewById(R.id.userAvatar);

        reportCounter = (TextView) header.findViewById(R.id.reportCount);

        actionCounter = (TextView) header.findViewById(R.id.actionCount);

        groupCounter = (TextView) header.findViewById(R.id.groupCount);

        reportCountLabel = (TextView) header.findViewById(R.id.reportCountLabel);

        actionCountLabel = (TextView) header.findViewById(R.id.actionCountLabel);

        groupCountLabel = (TextView) header.findViewById(R.id.groupCountLabel);

        reportStat = (LinearLayout) header.findViewById(R.id.reportStat);

        actionStat = (LinearLayout) header.findViewById(R.id.actionStat);

        groupStat = (LinearLayout) header.findViewById(R.id.groupStat);

        LinearLayout profileMeta = (LinearLayout) header.findViewById(R.id.profileMeta);

        LinearLayout profileStats = (LinearLayout) header.findViewById(R.id.profileStats);

        String userTitleText = user.properties.title;
        String userDescriptionText = user.properties.description;
        String userNameText = String.format("%s %s", user.properties.first_name, user.properties.last_name);
        String userOrganization = user.properties.organization_name;

        // Locate valid avatar field

        String userAvatarUrl = user.properties.picture;

        Picasso.with(this)
                .load(userAvatarUrl)
                .placeholder(R.drawable.user_avatar_placeholder_003)
                .transform(new CircleTransform()).into(userAvatar);

        userName.setText(userNameText);

        try {

            if (!userOrganization.isEmpty()) {

                userTitle.setText(String.format("%s at %s", userTitleText, userOrganization));

            } else {

                userTitle.setText(userTitleText);

            }

        } catch (NullPointerException ne) {

            userTitle.setVisibility(View.GONE);

        }

        try {

            userDescription.setText(userDescriptionText);

        } catch (NullPointerException ne) {

            userDescription.setVisibility(View.GONE);

        }

        userDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ObjectAnimator animation;

                int duration;

                int maxLines = TextViewCompat.getMaxLines(userDescription);

                if (maxLines == 2) {

                    userDescription.setEllipsize(null);

                    animation = ObjectAnimator.ofInt(
                            userDescription,
                            "maxLines",
                            2,
                            1000);

                    duration = 400;

                } else {

                    userDescription.setEllipsize(TextUtils.TruncateAt.END);

                    animation = ObjectAnimator.ofInt(
                            userDescription,
                            "maxLines",
                            1000,
                            2);

                    duration = 200;

                }

                animation.setDuration(duration);
                animation.setInterpolator(new LinearOutSlowInInterpolator());
                animation.start();

            }
        });

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

                actionCounter.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.base_blue));
                actionCountLabel.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.base_blue));

                reportCounter.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.material_blue_grey950));
                reportCountLabel.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.material_blue_grey950));

                if (timeLine != null) {

                    timeLine.setSelection(0);

                }

                timeLineContainer.setRefreshing(true);

                fetchPosts(5, 1, complexQuery, true);

            }
        });

        groupStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hasGroups) {

                    Intent intent = new Intent(UserProfileActivity.this, UserGroupsActivity.class);

                    intent.putExtra("GENERIC_USER", TRUE);

                    startActivity(intent);

                }

            }
        });

        timeLine.addHeaderView(header, null, false);

    }

    private void setReportCountState(int count) {

        reportCounter.setText(String.valueOf(reportCount));
        reportCountLabel.setText(resources.getQuantityString(R.plurals.post_label, reportCount, reportCount));

        if (count < 1) {

            if (promptBlock != null) {

                startPostButton.setVisibility(View.GONE);

                promptBlock.setVisibility(View.VISIBLE);

                promptMessage.setText(getString(R.string.prompt_no_posts_user, user.properties.first_name));

            }

        } else {

            timeLineContainer.setVisibility(View.VISIBLE);

        }

    }

    private void countReports(String query, final String filterName) {

        final String accessToken = prefs.getString("access_token", "");

        RestAdapter restAdapter = ReportService.restAdapter;

        ReportService service = restAdapter.create(ReportService.class);

        service.getReports(accessToken, "application/json", 1, 1, query, new CancelableCallback<FeatureCollection>() {

            @Override
            public void onSuccess(FeatureCollection featureCollection, Response response) {

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
                        setReportCountState(reportCount);
                        break;
                }

            }

            @Override
            public void onFailure(RetrofitError error) {

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

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        UserService service = UserService.restAdapter.create(UserService.class);

        service.getUserOrganization(accessToken, "application/json", userId, new CancelableCallback<OrganizationFeatureCollection>() {

            @Override
            public void onSuccess(OrganizationFeatureCollection organizationCollectionResponse, Response response) {

                ArrayList<Organization> organizations = organizationCollectionResponse.getFeatures();

                if (!organizations.isEmpty()) {

                    int groupCount = organizations.size();

                    groupCounter.setText(String.valueOf(groupCount));
                    groupCountLabel.setText(resources.getQuantityString(R.plurals.group_label, groupCount, groupCount));

                    groupStat.setVisibility(View.VISIBLE);

                    UserGroupList.setList(organizations);

                    hasGroups = true;

                }

            }

            @Override
            public void onFailure(RetrofitError error) {

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

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        Log.d("URL", query);

        RestAdapter restAdapter = ReportService.restAdapter;

        ReportService service = restAdapter.create(ReportService.class);

        service.getReports(accessToken, "application/json", page, limit, query, new CancelableCallback<FeatureCollection>() {

            @Override
            public void onSuccess(FeatureCollection featureCollection, Response response) {

                List<Report> reports = featureCollection.getFeatures();

                Log.v("list", reports.toString());

                if (reportCount == 99999999) {

                    reportCount = featureCollection.getProperties().num_results;

                }

                if (reportCount > 0) {

                    reportStat.setVisibility(View.VISIBLE);

                    reportCounter.setText(String.valueOf(reportCount));

                    reportCountLabel.setText(resources.getQuantityString(R.plurals.post_label, reportCount, reportCount));

                    setReportCountState(reportCount);

                } else {

                    reportStat.setVisibility(View.GONE);

                    setReportCountState(reportCount);

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
            public void onFailure(RetrofitError error) {

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

    private void resetStats() {

        reportCounter.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.base_blue));
        reportCountLabel.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.base_blue));

        actionCounter.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.material_blue_grey950));
        actionCountLabel.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.material_blue_grey950));

        actionFocus = false;

        timeLineContainer.setRefreshing(true);

        fetchPosts(5, 1, buildQuery(true, null), true);

    }

    private void deleteReport() {

        timeLineContainer.setRefreshing(true);

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        ReportService service = ReportService.restAdapter.create(ReportService.class);

        final Report report = ReportHolder.getReport();

        service.deleteSingleReport(accessToken, report.id, new CancelableCallback<Response>() {

            @Override
            public void onSuccess(Response response, Response response_) {

                reportCount -= 1;

                if (reportCount > 0) {

                    reportCounter.setText(String.valueOf(reportCount));

                } else {

                    reportStat.setVisibility(View.GONE);

                }

                if ("closed".equals(report.properties.state)) {

                    actionCount -= 1;

                    if (actionCount > 0) {

                        actionCounter.setText(String.valueOf(actionCount));

                    } else {

                        actionStat.setVisibility(View.GONE);

                    }

                }

                ReportHolder.setReport(null);

                resetStats();

            }

            @Override
            public void onFailure(RetrofitError error) {

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
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        Picasso.with(this).cancelRequest(userAvatar);

        ButterKnife.unbind(this);

        // Cancel all pending network requests

        CancelableCallback.cancelAll();

    }

}