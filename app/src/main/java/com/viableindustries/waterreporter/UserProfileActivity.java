package com.viableindustries.waterreporter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.squareup.picasso.Target;
import com.viableindustries.waterreporter.data.Comment;
import com.viableindustries.waterreporter.data.CommentPost;
import com.viableindustries.waterreporter.data.CommentService;
import com.viableindustries.waterreporter.data.FeatureCollection;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.QueryFilter;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportPhoto;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserGroupList;
import com.viableindustries.waterreporter.data.UserHolder;
import com.viableindustries.waterreporter.data.UserService;
import com.viableindustries.waterreporter.dialogs.CommentActionDialogListener;
import com.viableindustries.waterreporter.dialogs.CommentPhotoDialogListener;
import com.viableindustries.waterreporter.dialogs.ReportActionDialogListener;
import com.viableindustries.waterreporter.dialogs.ShareActionDialogListener;

import java.io.File;
import java.io.FileOutputStream;
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

public class UserProfileActivity extends AppCompatActivity implements ReportActionDialogListener {

    TextView userTitle;

    TextView userDescription;

    ImageView userAvatar;

    TextView reportCounter;

    TextView actionCounter;

    TextView groupCounter;

    TextView reportCountLabel;

    TextView actionCountLabel;

    TextView groupCountLabel;

    LinearLayout reportStat;

    LinearLayout actionStat;

    LinearLayout groupStat;

    LinearLayout profileMeta;

    LinearLayout profileStats;

    @Bind(R.id.timeline)
    SwipeRefreshLayout timeLineContainer;

    @Bind(R.id.postList)
    RecyclerView postList;

    @Bind(R.id.listTabs)
    FrameLayout listTabs;

    @Bind(R.id.log_out)
    ImageButton logOutButton;

    private PostCardAdapter postCardAdapter;

    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    protected TimelineAdapter timelineAdapter;

    protected List<Report> reportCollection = new ArrayList<Report>();

    private String userDescriptionText;

    private String userTitleText;

    private String userNameText;

    private String userAvatarUrl;

    private String userOrganization;

    private String complexQuery;

    private int userId;

    private int actionCount;

    private int reportCount = 99999999;

    private boolean actionFocus = false;

    private boolean hasGroups = false;

    private SharedPreferences prefs;

    private SharedPreferences coreProfile;

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

        coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

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

        if (prefs.getInt("user_id", 0) == userId) {

            logOutButton.setVisibility(View.VISIBLE);

        }

        // Set refresh listener on report feed container

        timeLineContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.

//                        countReports(complexQuery, "state");

//                        resetStats();

                    }
                }
        );

        // Set color of swipe refresh arrow animation

        timeLineContainer.setColorSchemeResources(R.color.waterreporter_blue);

        // Inflate and insert timeline header view

//        addListViewHeader();

        // Count reports with actions

        complexQuery = String.format(getResources().getString(R.string.complex_actions_query), userId, userId);

//        countReports(complexQuery, "state");

        // Retrieve the user's groups

//        fetchUserGroups(userId);

        // Retrieve first batch of user's reports

        if (reportCollection.isEmpty()) {

            timeLineContainer.setRefreshing(true);

            fetchReports(5, 1, buildQuery(true, null), false);

        }

        scrollListener = new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new data needs to be appended to the list

                if (actionFocus) {

                    fetchReports(5, page, complexQuery, false);

                } else {

                    fetchReports(5, page, buildQuery(true, null), false);

                }

                return true; // ONLY if more data is actually being loaded; false otherwise.

            }
        };

    }

    protected void addListViewHeader() {

        LayoutInflater inflater = getLayoutInflater();

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.user_profile_header, postList, false);

        TextView userName = (TextView) header.findViewById(R.id.userName);

        userTitle = (TextView) header.findViewById(R.id.userTitle);

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

        profileMeta = (LinearLayout) header.findViewById(R.id.profileMeta);

        profileStats = (LinearLayout) header.findViewById(R.id.profileStats);

        userTitleText = user.properties.title;
        userDescriptionText = user.properties.description;
        userNameText = String.format("%s %s", user.properties.first_name, user.properties.last_name);
        userOrganization = user.properties.organization_name;

        // Locate valid avatar field

        userAvatarUrl = user.properties.picture;

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

        // Attach click listeners to stat elements

        reportStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                resetStats();

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

                if (postList != null) {

                    mLayoutManager.scrollToPositionWithOffset(0, 0);

                }

                timeLineContainer.setRefreshing(true);

                fetchReports(5, 1, complexQuery, true);

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

//        postList.addHeaderView(header, null, false);

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

                        startActivity(new Intent(UserProfileActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    protected void fetchUserGroups(int userId) {

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        UserService service = UserService.restAdapter.create(UserService.class);

        service.getUserOrganization(accessToken, "application/json", userId, new Callback<OrganizationFeatureCollection>() {

            @Override
            public void success(OrganizationFeatureCollection organizationCollectionResponse, Response response) {

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

//        postList.setOnScrollListener(scrollListener);

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

    public void fetchReports(int limit, final int page, String query, final boolean refresh) {

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

//                    reportStat.setVisibility(View.VISIBLE);
//
//                    reportCounter.setText(String.valueOf(reportCount));
//
//                    reportCountLabel.setText(resources.getQuantityString(R.plurals.post_label, reportCount, reportCount));

                } else {

//                    reportStat.setVisibility(View.GONE);

                }

                if (refresh || reportCollection.isEmpty()) {

                    reportCollection.clear();

                    reportCollection.addAll(reports);

                    scrollListener.resetState();

                    try {

                        postCardAdapter.notifyDataSetChanged();

                        mLayoutManager.scrollToPositionWithOffset(0, 0);

                    } catch (NullPointerException e) {

                        populateTimeline(reportCollection);

                    }

                } else {

                    if (page > 1) {

                        reportCollection.addAll(reports);

                        postCardAdapter.notifyDataSetChanged();

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

        mLayoutManager = new LinearLayoutManager(this);
        postList.setLayoutManager(mLayoutManager);

        postCardAdapter = new PostCardAdapter(this, list, false, true, 0);

        // Attach the adapter to a ListView

        if (postList != null) {

            postList.setAdapter(postCardAdapter);

            attachScrollListener();

        }

        // Add standard RecyclerView animator

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        postList.setItemAnimator(itemAnimator);

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

        fetchReports(5, 1, buildQuery(true, null), true);

    }

    private void deleteReport() {

        timeLineContainer.setRefreshing(true);

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        ReportService service = ReportService.restAdapter.create(ReportService.class);

        final Report report = ReportHolder.getReport();

        service.deleteSingleReport(accessToken, report.id, new Callback<Response>() {

            @Override
            public void success(Response response, Response response_) {

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

//                resetStats();

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
