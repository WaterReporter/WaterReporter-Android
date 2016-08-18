package com.viableindustries.waterreporter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
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
import com.viableindustries.waterreporter.data.QueryFilter;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.UserService;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static java.security.AccessController.getContext;

public class UserProfileActivity extends AppCompatActivity {

    @Bind(R.id.userName)
    TextView userName;

    @Bind(R.id.userTitle)
    TextView userTitle;

    @Bind(R.id.userDescription)
    TextView userDescription;

    @Bind(R.id.userAvatar)
    ImageView userAvatar;

    @Bind(R.id.reportCount)
    TextView reportCounter;

    @Bind(R.id.actionCount)
    TextView actionCounter;

    @Bind(R.id.groupCount)
    TextView groupCounter;

    @Bind(R.id.reportCountLabel)
    TextView reportCountLabel;

    @Bind(R.id.actionCountLabel)
    TextView actionCountLabel;

    @Bind(R.id.groupCountLabel)
    TextView groupCountLabel;

    @Bind(R.id.reportStat)
    LinearLayout reportStat;

    @Bind(R.id.actionStat)
    LinearLayout actionStat;

    @Bind(R.id.profileMeta)
    LinearLayout profileMeta;

    @Bind(R.id.profileStats)
    LinearLayout profileStats;

//    @Bind(R.id.profileViewPager)
//    ViewPager viewPager;
//
//    @Bind(R.id.sliding_tabs)
//    TabLayout tabLayout;

    @Bind(R.id.timeline)
    SwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.timeline_items)
    ListView timeLine;

    protected TimelineAdapter timelineAdapter;

    //private SwipeRefreshLayout swipeRefreshLayout;

    //private ListView timeLine;

    protected List<Report> reportCollection = new ArrayList<Report>();

    // Number of pages in our ViewPager
    private Integer NUM_PAGES = 3;

    // The pager widget, which handles animation and allows swiping horizontally
    //private ViewPager mPager;

    // The pager adapter, which provides the pages to the view pager widget
    private PagerAdapter mPagerAdapter;

    private String userDescriptionText;

    private String userTitleText;

    private String userNameText;

    private String userAvatarUrl;

    private String userOrganization;

    private String bookMark;

    private float originListHeight;

    private int userId;

    private int actionCount = 0;

    private int reportCount = 99999999;

    private int groupCount = 0;

    private boolean actionFocus = false;

    private boolean hasScrolled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile);

        ButterKnife.bind(this);

        userId = getIntent().getExtras().getInt("USER_ID");
        userTitleText = getIntent().getExtras().getString("USER_TITLE");
        userDescriptionText = getIntent().getExtras().getString("USER_DESCRIPTION");
        userNameText = getIntent().getExtras().getString("USER_NAME");
        userOrganization = getIntent().getExtras().getString("USER_ORGANIZATION");
        userAvatarUrl = getIntent().getExtras().getString("USER_AVATAR");

        userName.setText(userNameText);

        userTitle.setText(userTitleText);

        userDescription.setText(userDescriptionText);

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
                            20);

                    duration = 350;

                } else {

                    userDescription.setEllipsize(TextUtils.TruncateAt.END);

                    animation = ObjectAnimator.ofInt(
                            userDescription,
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

        Picasso.with(this).load(userAvatarUrl).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(userAvatar);

        // Count reports with actions

        countReports(buildQuery(false, new String[][]{
                {"state", "eq", "closed"}
        }), "state");

        // Retrieve the user's groups

        fetchUserGroups(userId);

        // Retrieve first batch of user's reports

        if (reportCollection.isEmpty()) {

            fetchReports(10, 1, buildQuery(true, null), false, false);

        }

        // Attach click listeners to stat elements

        reportStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (timeLine != null) {

                    if (!actionFocus) {

                        //timeLine.smoothScrollToPosition(0);
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

                if (timeLine != null) {

                    //timeLine.smoothScrollToPosition(0);
                    timeLine.setSelection(0);

                }

                fetchReports(10, 1, buildQuery(true, new String[][]{
                        {"state", "eq", "closed"}
                }), false, true);

            }
        });

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        swipeRefreshLayout.setRefreshing(false);

                        int headerHeight = profileMeta.getHeight() + profileStats.getHeight();

                        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) swipeRefreshLayout.getLayoutParams();

                        if (hasScrolled && swipeRefreshLayout.getHeight() > originListHeight) {

                            //originListHeight = swipeRefreshLayout.getHeight();

                            //hasScrolled = true;

                            //params.height = swipeRefreshLayout.getHeight() - headerHeight;

                            //swipeRefreshLayout.animate().translationY(headerHeight / 2);

                            swipeRefreshLayout.animate().translationY(0);

                            params.height = swipeRefreshLayout.getHeight() - headerHeight;

                            hasScrolled = false;

                        }

                        swipeRefreshLayout.setLayoutParams(params);

                        swipeRefreshLayout.requestLayout();

                    }
                }

        );

        // Set color of swipe refresh arrow animation

        //swipeRefreshLayout.setColorSchemeResources(R.color.waterreporter_blue);

        swipeRefreshLayout.setColorSchemeColors(0,0,0,0);

        //swipeRefreshLayout.setProgressBackgroundColor(android.R.color.transparent);

        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, android.R.color.transparent));

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
                        actionCount = count;
                        actionCounter.setText(String.valueOf(actionCount));
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

                        startActivity(new Intent(UserProfileActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    protected void fetchUserGroups(int userId) {

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        Log.d("", access_token);

        UserService service = UserService.restAdapter.create(UserService.class);

        service.getUserOrganization(access_token, "application/json", userId, new Callback<OrganizationFeatureCollection>() {

            @Override
            public void success(OrganizationFeatureCollection organizationCollectionResponse, Response response) {

                List<Organization> organizations = organizationCollectionResponse.getFeatures();

                groupCount = organizations.size();

                groupCounter.setText(String.valueOf(groupCount));

                if (!organizations.isEmpty()) {

                    for (Organization organization : organizations) {

                        Log.d("orgName", organization.properties.name);

                    }

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

        timeLine.setOnScrollListener(new EndlessScrollListener() {

            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new data needs to be appended to the list

                if (actionFocus) {

                    fetchReports(10, page, buildQuery(true, new String[][]{
                            {"state", "eq", "closed"}
                    }), false, false);

                } else {

                    fetchReports(10, page, buildQuery(true, null), false, false);

                }

                return true; // ONLY if more data is actually being loaded; false otherwise.

            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                // Don't take any action on changed

            }

        });

        timeLine.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                float y = timeLine.getY();

                int motionEventAction = motionEvent.getAction();

                Log.d("event", motionEventAction + "");

//                originListHeight = timeLine.getHeight();

                String currentId = ((TextView) view.findViewById(R.id.tracker)).getText().toString();

                Log.d("id", currentId + "");

                Log.d("bookmark", bookMark + "");

                Log.d("offset", y + "");

                int headerHeight = profileMeta.getHeight() + profileStats.getHeight();

                ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) swipeRefreshLayout.getLayoutParams();

                if (!hasScrolled) {

                    originListHeight = swipeRefreshLayout.getHeight();

                    hasScrolled = true;

                    swipeRefreshLayout.animate().translationY(0 - headerHeight);

                    params.height = swipeRefreshLayout.getHeight() + headerHeight;

                }

//                if (bookMark.equals(currentId) && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//
//                    params.height = timeLine.getHeight() - headerHeight;
//
//                    timeLine.animate().translationY(headerHeight);
//
//                    //params.height = timeLine.getHeight() - headerHeight;
//
//                    hasScrolled = false;
//
//                }

                swipeRefreshLayout.setLayoutParams(params);

                swipeRefreshLayout.requestLayout();

                return false;

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

                    if (bookMark == null) {

                        bookMark = String.valueOf(reports.get(0).id);

                    }

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

                    CharSequence text = "Your report collection is empty. Tap on the plus sign in the menu bar to start a new report.";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(UserProfileActivity.this, text, duration);
                    toast.show();

                }

                if (refresh) {

                    reportCollection = reports;

                    populateTimeline(reportCollection);

                }

                //swipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void failure(RetrofitError error) {

                //swipeRefreshLayout.setRefreshing(false);

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

    private void populateTimeline(List list) {

        timelineAdapter = new TimelineAdapter(UserProfileActivity.this, list);

        // Attach the adapter to a ListView
        timeLine.setAdapter(timelineAdapter);

        attachScrollListener();

    }

    // A simple pager adapter that allows users to browse a person's feed and groups.
    private class ProfilePagerAdapter extends FragmentPagerAdapter {

        Context ctxt = null;

        public ProfilePagerAdapter(Context ctxt, FragmentManager fm) {
            super(fm);
            this.ctxt = ctxt;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return UserFeedFragment.newInstance(userId);
                case 1:
                    return UserGroupsFragment.newInstance(userId);
                case 2:
                    return UserGroupsFragment.newInstance(userId);
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public String getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "Reports";
                case 1:
                    return "Actions";
                case 2:
                    return "Groups";
                default:
                    return "Tab";
            }

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

    }

}
