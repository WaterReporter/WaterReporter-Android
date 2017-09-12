package com.viableindustries.waterreporter;

import android.animation.ObjectAnimator;
import android.content.Context;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.models.FeatureCollection;
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.organization.OrganizationHolder;
import com.viableindustries.waterreporter.api.models.organization.OrganizationMemberList;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.query.QueryFilter;
import com.viableindustries.waterreporter.api.models.query.QueryParams;
import com.viableindustries.waterreporter.api.models.query.QuerySort;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.api.models.user.UserCollection;
import com.viableindustries.waterreporter.api.models.user.UserOrgPatch;
import com.viableindustries.waterreporter.user_interface.adapters.TimelineAdapter;
import com.viableindustries.waterreporter.utilities.CancelableCallback;
import com.viableindustries.waterreporter.utilities.CircleTransform;
import com.viableindustries.waterreporter.utilities.EndlessScrollListener;
import com.viableindustries.waterreporter.utilities.PatternEditableBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class OrganizationProfileActivity extends AppCompatActivity {

    private LinearLayout reportStat;

    private TextView reportCounter;

    private TextView reportCountLabel;

    private LinearLayout actionStat;

    private TextView actionCounter;

    private TextView actionCountLabel;

    private LinearLayout peopleStat;

    private TextView peopleCounter;

    private TextView peopleCountLabel;

    private TextView organizationDescription;

    private ImageView organizationLogo;

    private Button joinOrganization;

    @Bind(R.id.timeline)
    SwipeRefreshLayout timeLineContainer;

    @Bind(R.id.timeline_items)
    ListView timeLine;

    @Bind(R.id.listTabs)
    FrameLayout listTabs;

    private LinearLayout promptBlock;

    private TextView promptMessage;

    private TimelineAdapter timelineAdapter;

    private final List<Report> reportCollection = new ArrayList<>();

    private String complexQuery;

    private ViewGroup.LayoutParams listViewParams;

    private int organizationId;

    private int actionCount = 0;

    private int reportCount = 99999999;

    private boolean actionFocus = false;

    private boolean hasScrolled = false;

    private boolean hasMembers = false;

    private Context mContext;

    private Organization organization;

    private SharedPreferences prefs;

    private SharedPreferences groupPrefs;

    private int socialOptions;

    private Resources resources;

    private EndlessScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_organization_profile);

        ButterKnife.bind(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        groupPrefs = getSharedPreferences(getString(R.string.group_membership_key), 0);

        mContext = this;

        resources = getResources();

        organization = OrganizationHolder.getOrganization();

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

        complexQuery = buildQuery(true, new String[][]{
                {"state", "eq", "closed"}
        });

        countReports(complexQuery, "state");

        // Retrieve the organization's members

        fetchOrganizationMembers(50, 1, organizationId);

        // Retrieve first batch of user's reports

        if (reportCollection.isEmpty()) {

            timeLineContainer.setRefreshing(true);

            fetchReports(5, 1, buildQuery(true, null), false);

        }

        scrollListener = new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new api needs to be appended to the list

                if (actionFocus) {

                    fetchReports(5, page, complexQuery, false);

                } else {

                    fetchReports(5, page, buildQuery(true, null), false);

                }

                return true; // ONLY if more api is actually being loaded; false otherwise.

            }
        };

    }

    private void startPost() {

        Intent intent = new Intent(mContext, PhotoMetaActivity.class);

        TextView tagName = (TextView) findViewById(R.id.organizationName);

        intent.putExtra("autoTag", String.format("\u0023%s", tagName.getText().toString().replaceAll("[^a-zA-Z0-9]+", "")));

        startActivity(intent);

        this.overridePendingTransition(R.anim.animation_enter_right,
                R.anim.animation_exit_left);

    }

    private void joinOrganization(final Organization organization) {

        // Retrieve API token

        final String accessToken = prefs.getString("access_token", "");

        // Retrieve user ID

        int id = prefs.getInt("user_id", 0);

        // Build request object

        Map<String, Map> userPatch = UserOrgPatch.buildRequest(organization.id, "add");

        RestClient.getUserService().updateUserOrganization(accessToken, "application/json", id, userPatch, new CancelableCallback<User>() {

            @Override
            public void onSuccess(User user, Response response) {

                String action;

                action = "joined";

                joinOrganization.setVisibility(View.GONE);

                groupPrefs.edit().putInt(organization.properties.name, organization.properties.id).apply();

                CharSequence text = String.format("Successfully %s %s", action, organization.properties.name);
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(mContext, text, duration);
                toast.show();

            }

            @Override
            public void onFailure(RetrofitError error) {

                Response response = error.getResponse();

                int status = response.getStatus();

                error.printStackTrace();

            }

        });

    }

    private void addListViewHeader() {

        LayoutInflater inflater = getLayoutInflater();

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.organization_profile_header, timeLine, false);

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

        TextView organizationName = (TextView) header.findViewById(R.id.organizationName);

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

        LinearLayout profileMeta = (LinearLayout) header.findViewById(R.id.profileMeta);

        LinearLayout profileStats = (LinearLayout) header.findViewById(R.id.profileStats);

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

        String organizationDescriptionText = organization.properties.description;
        String organizationNameText = organization.properties.name;
        String organizationLogoUrl = organization.properties.picture;

        organizationName.setText(organizationNameText);

        Picasso.with(this).load(organizationLogoUrl).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(organizationLogo);

        try {

            organizationDescription.setText(organizationDescriptionText);

            new PatternEditableBuilder().
                    addPattern(mContext, Pattern.compile("\\#(\\w+)"), ContextCompat.getColor(mContext, R.color.waterreporter_blue),
                            new PatternEditableBuilder.SpannableClickedListener() {
                                @Override
                                public void onSpanClicked(String text) {

                                    Intent intent = new Intent(mContext, TagProfileActivity.class);
                                    intent.putExtra("tag", text);
                                    mContext.startActivity(intent);

                                }
                            }).into(organizationDescription);

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
                                1000);

                        duration = 400;

                    } else {

                        organizationDescription.setEllipsize(TextUtils.TruncateAt.END);

                        animation = ObjectAnimator.ofInt(
                                organizationDescription,
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

        peopleStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hasMembers) {

                    Intent intent = new Intent(mContext, OrganizationMembersActivity.class);

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

        fetchReports(5, 1, buildQuery(true, null), true);

    }

    private void setReportCountState(int count) {

        reportCounter.setText(String.valueOf(reportCount));
        reportCountLabel.setText(resources.getQuantityString(R.plurals.post_label, reportCount, reportCount));

        if (count < 1) {

            try {

                promptBlock.setVisibility(View.VISIBLE);

                promptMessage.setText(getString(R.string.prompt_no_posts_group));

            } catch (NullPointerException e) {

                finish();

            }

        } else {

            timeLineContainer.setVisibility(View.VISIBLE);

        }

    }

    private void countReports(String query, final String filterName) {

        final String accessToken = prefs.getString("access_token", "");

        RestClient.getReportService().getReports(accessToken, "application/json", 1, 1, query, new CancelableCallback<FeatureCollection>() {

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
                        reportCounter.setText(String.valueOf(reportCount));
                        reportCountLabel.setText(resources.getQuantityString(R.plurals.post_label, reportCount, reportCount));
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

                        startActivity(new Intent(mContext, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void fetchOrganizationMembers(int limit, int page, int organizationId) {

        final String accessToken = prefs.getString("access_token", "");

        RestClient.getOrganizationService().getOrganizationMembers(accessToken, "application/json", organizationId, page, limit, null, new CancelableCallback<UserCollection>() {

            @Override
            public void onSuccess(UserCollection userCollection, Response response) {

                ArrayList<User> members = userCollection.getFeatures();

                if (!members.isEmpty()) {

                    int memberCount = userCollection.getProperties().num_results;

                    peopleCounter.setText(String.valueOf(memberCount));
                    peopleCountLabel.setText(resources.getQuantityString(R.plurals.member_label, memberCount, memberCount));

                    peopleStat.setVisibility(View.VISIBLE);

                    OrganizationMemberList.setList(members);

                    hasMembers = true;

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

                        startActivity(new Intent(mContext, SignInActivity.class));

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

    private void fetchReports(int limit, final int page, String query, final boolean refresh) {

        final String accessToken = prefs.getString("access_token", "");

        RestClient.getReportService().getReports(accessToken, "application/json", page, limit, query, new CancelableCallback<FeatureCollection>() {

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

                timeLineContainer.setRefreshing(false);

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

                        startActivity(new Intent(mContext, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void populateTimeline(List<Report> list) {

        timelineAdapter = new TimelineAdapter(this, list, false, false, getSupportFragmentManager());

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

        // Cancel all pending network requests

        CancelableCallback.cancelAll();

    }

}
