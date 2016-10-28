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
import android.widget.FrameLayout;
import android.widget.ImageButton;
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
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserGroupList;
import com.viableindustries.waterreporter.data.UserHolder;
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

    @Bind(R.id.groupStat)
    LinearLayout groupStat;

    @Bind(R.id.profileMeta)
    LinearLayout profileMeta;

    @Bind(R.id.profileStats)
    LinearLayout profileStats;

    @Bind(R.id.timeline_items)
    ListView timeLine;

    @Bind(R.id.listTabs)
    FrameLayout listTabs;

    @Bind(R.id.log_out)
    ImageButton logOutButton;

    protected TimelineAdapter timelineAdapter;

    protected List<Report> reportCollection = new ArrayList<Report>();

    private String userDescriptionText;

    private String userTitleText;

    private String userNameText;

    private String userAvatarUrl;

    private String userOrganization;

    private String bookMark;

    private String groupList;

    private String complexQuery;

    private ViewGroup.LayoutParams listViewParams;

    private int userId;

    private int actionCount = 0;

    private int reportCount = 99999999;

    private int groupCount = 0;

    private boolean actionFocus = false;

    private boolean hasScrolled = false;

    private boolean hasGroups = false;

    private SharedPreferences prefs;

    private SharedPreferences coreProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile);

        ButterKnife.bind(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        // Retrieve stored User object

        User user = UserHolder.getUser();

        // These are the User attributes we need to start populating the view

        userId = user.properties.id;

        if (prefs.getInt("user_id", 0) == userId) {

            logOutButton.setVisibility(View.VISIBLE);

        }

        userTitleText = user.properties.title;
        userDescriptionText = user.properties.description;
        userNameText = String.format("%s %s", user.properties.first_name, user.properties.last_name);
        userOrganization = user.properties.organization_name;
        userAvatarUrl = user.properties.picture;

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

                animation.setDuration(100);
                animation.setInterpolator(new LinearOutSlowInInterpolator());
                animation.start();

            }
        });

        Picasso.with(this).load(userAvatarUrl).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(userAvatar);

        // Count reports with actions

        complexQuery = String.format(getResources().getString(R.string.complex_actions_query), userId, userId);

//        countReports(buildQuery(false, new String[][]{
//                {"state", "eq", "closed"}
//        }), "state");

        countReports(complexQuery, "state");

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

                reportCounter.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.base_blue));
                reportCountLabel.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.base_blue));

                actionCounter.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.material_blue_grey950));
                actionCountLabel.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.material_blue_grey950));

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

                actionCounter.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.base_blue));
                actionCountLabel.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.base_blue));

                reportCounter.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.material_blue_grey950));
                reportCountLabel.setTextColor(ContextCompat.getColor(UserProfileActivity.this, R.color.material_blue_grey950));

                if (timeLine != null) {

                    timeLine.setSelection(0);

                }

//                fetchReports(10, 1, buildQuery(true, new String[][]{
//                        {"state", "eq", "closed"}
//                }), false, true);
                fetchReports(10, 1, complexQuery, false, true);

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

    }

    protected void addListViewHeader() {

//        if (if)


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
//                            actionStat.setVisibility(View.VISIBLE);
                            actionCount = count;
                            actionCounter.setText(String.valueOf(actionCount));
                            actionStat.setVisibility(View.VISIBLE);

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

                ArrayList<Organization> organizations = organizationCollectionResponse.getFeatures();

                if (!organizations.isEmpty()) {

                    groupCounter.setText(String.valueOf(organizations.size()));

                    groupStat.setVisibility(View.VISIBLE);

                    UserGroupList.setList(organizations);

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

        timeLine.setOnScrollListener(new EndlessScrollListener() {

            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new data needs to be appended to the list

                if (actionFocus) {

//                    fetchReports(10, page, buildQuery(true, new String[][]{
//                            {"state", "eq", "closed"}
//                    }), false, false);
                    fetchReports(10, page, complexQuery, false, false);

                } else {

                    fetchReports(10, page, buildQuery(true, null), false, false);

                }

                return true; // ONLY if more data is actually being loaded; false otherwise.

            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

//                if (scrollState == 0) {

                View mView = timeLine.getChildAt(0);

                int top = mView.getTop();

                final Handler h = new Handler();

                final Runnable changeHeight = new Runnable() {

                    @Override
                    public void run() {

                        listTabs.setLayoutParams(listViewParams);

                        //listTabs.requestLayout();

                    }
                };

                int headerHeight = profileMeta.getHeight() + profileStats.getHeight();

                listViewParams = (ViewGroup.LayoutParams) listTabs.getLayoutParams();

                // see if top Y is at 0 and first visible position is at 0
                if (top == 0 && timeLine.getFirstVisiblePosition() == 0) {

                    listTabs.animate().translationY(0);

                    listViewParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

                    h.postDelayed(changeHeight, 500);

                    hasScrolled = false;

                } else {

                    if (!hasScrolled) {

                        listTabs.animate().translationY(0 - headerHeight);

                        listViewParams.height = listTabs.getHeight() + headerHeight;

                        h.postDelayed(changeHeight, 0);

                        hasScrolled = true;

                    }

                }

            }

//            }

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

                    //CharSequence text = "Your report collection is empty. Tap on the plus sign in the menu bar to start a new report.";
                    //int duration = Toast.LENGTH_LONG;

                    //Toast toast = Toast.makeText(UserProfileActivity.this, text, duration);
                    //toast.show();

                }

                if (refresh) {

                    reportCollection = reports;

                    populateTimeline(reportCollection);

                }

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

        timelineAdapter = new TimelineAdapter(UserProfileActivity.this, list, true);

        // Attach the adapter to a ListView
        timeLine.setAdapter(timelineAdapter);

        attachScrollListener();

    }

    public void logOut(View view) {

        // Clear stored token and user id values

        prefs.edit().putString("access_token", "")
                .putInt("user_id", 0).apply();

        // Clear stored active user profile

        coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

        coreProfile.edit().clear().apply();

        startActivity(new Intent(this, SignInActivity.class));

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
