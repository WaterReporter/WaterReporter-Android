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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
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
import com.viableindustries.waterreporter.data.UserFeatureCollection;
import com.viableindustries.waterreporter.data.UserGroupList;
import com.viableindustries.waterreporter.data.UserService;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static java.lang.Boolean.TRUE;
import static java.security.AccessController.getContext;

public class OrganizationProfileActivity extends AppCompatActivity {

    @Bind(R.id.organizationName)
    TextView organizationName;

    @Bind(R.id.organizationDescription)
    TextView organizationDescription;

    @Bind(R.id.organizationLogo)
    ImageView organizationLogo;

    @Bind(R.id.reportCount)
    TextView reportCounter;

    @Bind(R.id.actionCount)
    TextView actionCounter;

    @Bind(R.id.peopleCount)
    TextView peopleCounter;

    @Bind(R.id.reportCountLabel)
    TextView reportCountLabel;

    @Bind(R.id.actionCountLabel)
    TextView actionCountLabel;

    @Bind(R.id.peopleCountLabel)
    TextView peopleCountLabel;

    @Bind(R.id.reportStat)
    LinearLayout reportStat;

    @Bind(R.id.actionStat)
    LinearLayout actionStat;

    @Bind(R.id.peopleStat)
    LinearLayout peopleStat;

    @Bind(R.id.profileMeta)
    LinearLayout profileMeta;

    @Bind(R.id.profileStats)
    LinearLayout profileStats;

    @Bind(R.id.timeline_items)
    ListView timeLine;

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

    private int memberCount = 0;

    private boolean actionFocus = false;

    private boolean hasScrolled = false;

    private boolean hasMembers = false;

    private Context context;

    private Organization organization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_organization_profile);

        ButterKnife.bind(this);

        context = this;

        organization = OrganizationHolder.getOrganization();

//        organizationId = getIntent().getExtras().getInt("ORGANIZATION_ID");
//        organizationDescriptionText = getIntent().getExtras().getString("USER_DESCRIPTION");
//        organizationNameText = getIntent().getExtras().getString("USER_NAME");
//        organizationLogoUrl = getIntent().getExtras().getString("USER_AVATAR");

        organizationId = organization.id;
        organizationDescriptionText = organization.properties.description;
        organizationNameText = organization.properties.name;
        organizationLogoUrl = organization.properties.picture;

        organizationName.setText(organizationNameText);

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

                    animation.setDuration(duration);
                    animation.setInterpolator(new LinearOutSlowInInterpolator());
                    animation.start();

                }
            });

        } catch (NullPointerException ne) {

            organizationDescription.setVisibility(View.GONE);

        }

        Picasso.with(this).load(organizationLogoUrl).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(organizationLogo);

        // Count reports with actions

//        complexQuery = String.format(getResources().getString(R.string.complex_actions_query), organizationId, organizationId);

        complexQuery = buildQuery(true, new String[][]{
                {"state", "eq", "closed"}
        });

        countReports(complexQuery, "state");

        // Retrieve the user's groups

        fetchOrganizationMembers(organizationId);

        // Retrieve first batch of user's reports

        if (reportCollection.isEmpty()) {

            fetchReports(10, 1, buildQuery(true, null), false, false);

        }

        // Attach click listeners to stat elements

        reportStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                reportCounter.setTextColor(ContextCompat.getColor(context, R.color.base_blue));
                reportCountLabel.setTextColor(ContextCompat.getColor(context, R.color.base_blue));

                actionCounter.setTextColor(ContextCompat.getColor(context, R.color.material_blue_grey950));
                actionCountLabel.setTextColor(ContextCompat.getColor(context, R.color.material_blue_grey950));

                if (timeLine != null) {

                    if (!actionFocus) {

                        timeLine.setSelection(0);

                    } else {

                        actionFocus = false;

                        fetchReports(10, 1, buildQuery(true, null), false, true);

                    }

                }

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

                fetchReports(10, 1, complexQuery, false, true);

            }
        });

//        peopleStat.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (hasMembers) {
//
//                    Intent intent = new Intent(context, UserGroupsActivity.class);
//
//                    intent.putExtra("GENERIC_USER", TRUE);
//
//                    startActivity(intent);
//
//                }
//
//            }
//        });

    }

    protected void countReports(String query, final String filterName) {

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        RestAdapter restAdapter = ReportService.restAdapter;

        ReportService service = restAdapter.create(ReportService.class);

        service.getReports(access_token, "application/json", 1, 1, query, new Callback<FeatureCollection>() {

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

    protected void fetchOrganizationMembers(int organizationId) {

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        Log.d("", access_token);

        OrganizationService service = OrganizationService.restAdapter.create(OrganizationService.class);

        service.getOrganizationMembers(access_token, "application/json", organizationId, 25, null, new Callback<UserFeatureCollection>() {

            @Override
            public void success(UserFeatureCollection userFeatureCollection, Response response) {

                ArrayList<User> members = userFeatureCollection.getFeatures();

                memberCount = members.size();

                peopleCounter.setText(String.valueOf(memberCount));

                if (!members.isEmpty()) {

                    hasMembers = true;

                    OrganizationMemberList.setList(members);

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

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (scrollState == 0) {

                    View mView = timeLine.getChildAt(0);

                    int top = mView.getTop();

                    final Handler h = new Handler();

                    final Runnable changeHeight = new Runnable() {

                        @Override
                        public void run() {

                            timeLine.setLayoutParams(listViewParams);

                            timeLine.requestLayout();

                        }
                    };

                    int headerHeight = profileMeta.getHeight() + profileStats.getHeight();

                    listViewParams = (ViewGroup.LayoutParams) timeLine.getLayoutParams();

                    // see if top Y is at 0 and first visible position is at 0
                    if (top == 0 && timeLine.getFirstVisiblePosition() == 0) {

                        timeLine.animate().translationY(0);

                        listViewParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                        h.postDelayed(changeHeight, 500);

                        hasScrolled = false;

                    } else {

                        if (!hasScrolled) {

                            timeLine.animate().translationY(0 - headerHeight);

                            listViewParams.height = timeLine.getHeight() + headerHeight;

                            h.postDelayed(changeHeight, 0);

                            hasScrolled = true;

                        }

                    }

                }

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

        SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        Log.d("", access_token);

        Log.d("URL", query);

        RestAdapter restAdapter = ReportService.restAdapter;

        ReportService service = restAdapter.create(ReportService.class);

        service.getReports(access_token, "application/json", page, limit, query, new Callback<FeatureCollection>() {

            @Override
            public void success(FeatureCollection featureCollection, Response response) {

                List<Report> reports = featureCollection.getFeatures();

                Log.v("list", reports.toString());

                if (reportCount == 99999999) {

                    reportCount = featureCollection.getProperties().num_results;

                    reportCounter.setText(String.valueOf(reportCount));

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

                    CharSequence text = "The organization is not affiliated with any reports.";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                }

                if (refresh) {

                    reportCollection = reports;

                    populateTimeline(reportCollection);

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

    private void populateTimeline(List list) {

        timelineAdapter = new TimelineAdapter(context, list);

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
