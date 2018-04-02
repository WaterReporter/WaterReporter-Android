package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.models.FeatureCollection;
import com.viableindustries.waterreporter.api.models.campaign.Campaign;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.query.QueryFilter;
import com.viableindustries.waterreporter.api.models.query.QueryParams;
import com.viableindustries.waterreporter.api.models.query.QuerySort;
import com.viableindustries.waterreporter.api.models.snapshot.CampaignLeader;
import com.viableindustries.waterreporter.api.models.snapshot.CampaignLeaderboard;
import com.viableindustries.waterreporter.api.models.snapshot.CampaignSnapshot;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.user_interface.adapters.CampaignLeaderListAdapter;
import com.viableindustries.waterreporter.user_interface.adapters.TimelineAdapter;
import com.viableindustries.waterreporter.user_interface.dialogs.CampaignExtrasBottomSheetDialogFragment;
import com.viableindustries.waterreporter.utilities.EndlessScrollListener;
import com.viableindustries.waterreporter.utilities.ModelStorage;
import com.viableindustries.waterreporter.utilities.UtilityMethods;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.BlurTransformation;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CampaignProfileActivity extends AppCompatActivity {

    private RelativeLayout reportStat;

    private TextView reportCounter;

    private RelativeLayout actionStat;

    private TextView actionCounter;

    private RelativeLayout peopleStat;

    private TextView peopleCounter;

    private TextView campaignName;

    private TextView campaignTagline;

    private ImageView campaignImage;

    private ImageView logoView;

    private RelativeLayout extraActions;

    private ImageView extraActionsIconView;

    private HorizontalScrollView leaderBoardComponent;

    private LinearLayout leaderBoardItems;

    @Bind(R.id.group_membership_button)
    FloatingActionButton joinOrganization;

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

    private int mCampaignId;

    private int actionCount;

    private int reportCount = 99999999;

    private boolean actionFocus = false;

    private boolean hasMembers = false;

    private Context mContext;

    private Campaign mCampaign;

    private List<CampaignLeader> campaignLeaders;

    private SharedPreferences mSharedPreferences;

    private SharedPreferences groupMembership;

    private Resources resources;

    private EndlessScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_organization_profile);

        ButterKnife.bind(this);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        groupMembership = getSharedPreferences(getString(R.string.group_membership_key), 0);

        mContext = this;

        resources = getResources();

        // Inspect intent and handle app link data

        Intent appLinkIntent = getIntent();
        Uri appLinkData = appLinkIntent.getData();

        if (appLinkData != null) {

            List<String> pathSegments = appLinkData.getPathSegments();

            try {

                mCampaignId = 0;

                if (pathSegments != null && pathSegments.size() >= 2) {

                    try {

                        mCampaignId = Integer.parseInt(pathSegments.get(pathSegments.size() - 1));

                    } catch (NumberFormatException e) {

                        mCampaignId = Integer.parseInt(pathSegments.get(pathSegments.size() - 2));

                    }

                }

                Log.d("organization--id", mCampaignId + "");

                if (mCampaignId > 0) {

                    Log.v("get-organization--data", "GO!");

                    fetchCampaign(mCampaignId);

                }

            } catch (NumberFormatException e) {

                // Retrieve stored organization data

                retrieveStoredCampaign();

            }

        } else {

            // Retrieve stored organization data

            retrieveStoredCampaign();

        }

        // Set refresh listener on report feed container

        timeLineContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual api-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.

                        fetchSnapshot(mCampaignId);

                        resetStats();

                    }
                }
        );

        // Set color and offset of swipe refresh arrow animation

        timeLineContainer.setColorSchemeResources(R.color.waterreporter_blue);

        timeLineContainer.setProgressViewOffset(false, 0, 100);

        scrollListener = new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new data should be appended to the list

                if (actionFocus) {

                    fetchPosts(5, page, complexQuery, false);

                } else {

                    fetchPosts(5, page, buildQuery(true, null), false);

                }

                return true; // ONLY if more data are actually being loaded; false otherwise.

            }
        };

    }

    private void retrieveStoredCampaign() {

        mCampaign = ModelStorage.getStoredCampaign(mSharedPreferences);

        try {

            mCampaignId = mCampaign.properties.id;

            setCampaignData(mCampaign);

        } catch (NullPointerException e1) {

            try {

                mCampaignId = mCampaign.id;

                fetchCampaign(mCampaignId);

            } catch (NullPointerException e2) {

                Log.v("NO-STORED-CAMPAIGN", e2.toString());

                startActivity(new Intent(this, MainActivity.class));

                finish();

            }

        }

    }

    private void setCampaignData(Campaign campaignData) {

        // Inflate and insert timeline header view

        addListViewHeader();

        // Retrieve leaderboard data

        fetchLeaderboard(mCampaignId);

        // Retrieve campaign snapshot data

        fetchSnapshot(mCampaignId);

        if (reportCollection.isEmpty() && timeLineContainer != null) {

            timeLineContainer.setRefreshing(true);

            fetchPosts(5, 1, buildQuery(true, null), false);

        }

    }

    private void startPost() {

        Intent intent = new Intent(mContext, PhotoMetaActivity.class);

        intent.putExtra("campaignId", mCampaignId);

        startActivity(intent);

        this.overridePendingTransition(R.anim.animation_enter_right,
                R.anim.animation_exit_left);

    }

    private void addListViewHeader() {

        if (timeLine != null) {

            LayoutInflater inflater = getLayoutInflater();

            ViewGroup header = (ViewGroup) inflater.inflate(R.layout.campaign_profile_header, timeLine, false);

            // Set up white color filter for reversed Water Reporter logo
            // and extra actions ellipsis

            logoView = (ImageView) header.findViewById(R.id.logo);

            logoView.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);

            extraActionsIconView = (ImageView) header.findViewById(R.id.extraActionsIconView);

            extraActionsIconView.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);

            // Leaderboard views

            leaderBoardComponent = (HorizontalScrollView) header.findViewById(R.id.leaderBoardComponent);

            leaderBoardItems = (LinearLayout) header.findViewById(R.id.leaderBoardItems);

            // If campaign is still active, add text and click listener to startPostButton

            boolean campaignIsExpired = UtilityMethods.dateExpired(
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
                    mCampaign.properties.expiration_date
            );

            Button startPostButton = (Button) header.findViewById(R.id.startPost);

            if (!campaignIsExpired) {

                startPostButton.setText(getString(R.string.share_post_prompt));

                startPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startPost();
                    }
                });

            } else {

                startPostButton.setVisibility(View.GONE);

            }

            campaignName = (TextView) header.findViewById(R.id.campaignName);

            campaignTagline = (TextView) header.findViewById(R.id.campaignTagline);

            campaignImage = (ImageView) header.findViewById(R.id.campaignImage);

            reportCounter = (TextView) header.findViewById(R.id.reportCount);

            actionCounter = (TextView) header.findViewById(R.id.actionCount);

            peopleCounter = (TextView) header.findViewById(R.id.peopleCount);

            reportStat = (RelativeLayout) header.findViewById(R.id.reportStat);

            actionStat = (RelativeLayout) header.findViewById(R.id.actionStat);

            peopleStat = (RelativeLayout) header.findViewById(R.id.peopleStat);

            String campaignTaglineText = mCampaign.properties.tagline;
            String campaignNameText = mCampaign.properties.name;
            String campaignImageUrl = mCampaign.properties.picture;

            campaignName.setText(campaignNameText);
            campaignTagline.setText(campaignTaglineText);

            Picasso.with(this)
                    .load(campaignImageUrl)
                    .transform(new BlurTransformation(this, 4, 1))
                    .into(campaignImage);

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

                    startActivity(new Intent(CampaignProfileActivity.this, CampaignGroupsActivity.class));

                }
            });

            peopleStat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivity(new Intent(CampaignProfileActivity.this, CampaignMembersActivity.class));

                }
            });

            // Present extra actions dialog (bottom sheet)

            extraActions = (RelativeLayout) header.findViewById(R.id.extraActions);

            extraActions.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    ModelStorage.storeModel(mSharedPreferences, mCampaign, "stored_campaign");

                    CampaignExtrasBottomSheetDialogFragment campaignExtrasBottomSheetDialogFragment =
                            new CampaignExtrasBottomSheetDialogFragment();

                    campaignExtrasBottomSheetDialogFragment.show(getSupportFragmentManager(), "campaign-extras-dialog");

                }

            });

            // Add populated header view to report timeline

            timeLine.addHeaderView(header, null, false);

        }

    }

    private void resetStats() {

        actionFocus = false;

        timeLineContainer.setRefreshing(true);

        fetchPosts(5, 1, buildQuery(true, null), true);

    }

    private void setPostCountState(int count) {

        String reportCountText = String.format("%s %s", String.valueOf(count),
                resources.getQuantityString(R.plurals.post_label, count, count));
        reportCounter.setText(reportCountText);

        if (count < 1) {

            try {

                promptBlock.setVisibility(View.VISIBLE);

                promptMessage.setText(getString(R.string.prompt_no_posts_group));

            } catch (NullPointerException e) {

                finish();

            }

        }

    }

    private void populateLeaderboard(List<CampaignLeader> campaignLeaders) {

        leaderBoardItems.removeAllViews();

        if (!campaignLeaders.isEmpty()) {

            CampaignLeaderListAdapter campaignLeaderListAdapter = new CampaignLeaderListAdapter(this, campaignLeaders);

            final int adapterCount = campaignLeaderListAdapter.getCount();

            for (int i = 0; i < adapterCount; i++) {

                View item = campaignLeaderListAdapter.getView(i, null, leaderBoardItems);

                leaderBoardItems.addView(item);

            }

        } else {

            leaderBoardComponent.setVisibility(View.GONE);

        }

    }

    private void fetchLeaderboard(int campaignId) {

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getSnapshotService().getCampaignLeaderboard(accessToken, "application/json", mCampaignId, new Callback<CampaignLeaderboard>() {

            @Override
            public void success(CampaignLeaderboard campaignLeaderboard, Response response) {

                campaignLeaders = campaignLeaderboard.getFeatures();

//                if (campaignLeaders.size() > 5) {
//
//                    campaignLeaders = campaignLeaderboard.getFeatures().subList(0, 5);
//
//                }

                populateLeaderboard(campaignLeaders);

            }

            @Override
            public void failure(RetrofitError error) {

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(CampaignProfileActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void fetchSnapshot(int campaignId) {

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getSnapshotService().getCampaign(accessToken, "application/json", mCampaignId, new Callback<CampaignSnapshot>() {

            @Override
            public void success(CampaignSnapshot campaignSnapshot, Response response) {

                String reportCountText = String.format("%s %s", String.valueOf(campaignSnapshot.posts),
                        resources.getQuantityString(R.plurals.post_label, campaignSnapshot.posts, campaignSnapshot.posts));
                reportCounter.setText(reportCountText);

                String groupCountText = String.format("%s %s", String.valueOf(campaignSnapshot.groups),
                        resources.getQuantityString(R.plurals.group_label, campaignSnapshot.groups, campaignSnapshot.groups));
                actionCounter.setText(groupCountText);

                String peopleCountText = String.format("%s %s", String.valueOf(campaignSnapshot.members),
                        resources.getQuantityString(R.plurals.member_label, campaignSnapshot.members, campaignSnapshot.members));
                peopleCounter.setText(peopleCountText);

            }

            @Override
            public void failure(RetrofitError error) {

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(CampaignProfileActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

//    private void countPosts(String query, final String filterName) {
//
//        final String accessToken = mSharedPreferences.getString("access_token", "");
//
//        RestClient.getReportService().getReports(accessToken, "application/json", 1, 1, query, new Callback<FeatureCollection>() {
//
//            @Override
//            public void success(FeatureCollection featureCollection, Response response) {
//
//                int count = featureCollection.getProperties().num_results;
//
//                switch (filterName) {
//                    case "state":
//                        if (count > 0) {
//                            actionStat.setVisibility(View.VISIBLE);
//                            actionCount = count;
//                            String actionCountText = String.format("%s %s", String.valueOf(actionCount),
//                                    resources.getQuantityString(R.plurals.action_label, actionCount, actionCount));
//                            actionCounter.setText(actionCountText);
//                        }
//                        break;
//                    default:
//                        reportCount = count;
//                        setPostCountState(reportCount);
//                        break;
//                }
//
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//
//                if (error == null) return;
//
//                Response errorResponse = error.getResponse();
//
//                // If we have a valid response object, check the status code and redirect to log in view if necessary
//
//                if (errorResponse != null) {
//
//                    int status = errorResponse.getStatus();
//
//                    if (status == 403) {
//
//                        startActivity(new Intent(CampaignProfileActivity.this, SignInActivity.class));
//
//                    }
//
//                }
//
//            }
//
//        });
//
//    }

//    private void fetchCampaignMembers(int limit, int page, int mCampaignId) {
//
//        final String accessToken = mSharedPreferences.getString("access_token", "");
//
//        RestClient.getOrganizationService().getOrganizationMembers(accessToken, "application/json", mCampaignId, page, limit, null, new Callback<UserCollection>() {
//
//            @Override
//            public void success(UserCollection userCollection, Response response) {
//
//                ArrayList<User> members = userCollection.getFeatures();
//
//                if (!members.isEmpty()) {
//
//                    int memberCount = userCollection.getProperties().num_results;
//
//                    peopleCounter.setText(String.valueOf(memberCount));
//                    peopleCountLabel.setText(resources.getQuantityString(R.plurals.member_label, memberCount, memberCount));
//
//                    peopleStat.setVisibility(View.VISIBLE);
//
//                    OrganizationMemberList.setList(members);
//
//                    hasMembers = true;
//
//                }
//
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//
//                if (error == null) return;
//
//                Response errorResponse = error.getResponse();
//
//                // If we have a valid response object, check the status code and redirect to log in view if necessary
//
//                if (errorResponse != null) {
//
//                    int status = errorResponse.getStatus();
//
//                    if (status == 403) {
//
//                        startActivity(new Intent(mContext, SignInActivity.class));
//
//                    }
//
//                }
//
//            }
//
//        });
//
//    }

    private void fetchCampaign(int campaignId) {

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getCampaignService().getSingle(accessToken, "application/json", campaignId, new Callback<Campaign>() {

            @Override
            public void success(Campaign campaign, Response response) {

                mCampaign = campaign;

                Log.v("try-organization--data", mCampaign.properties.name);

                if (timeLineContainer != null) {

                    Log.v("set-organization--data", mCampaign.properties.name);

                    setCampaignData(mCampaign);

                } else {

                    finish();

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

        QueryFilter userFilter = new QueryFilter("campaigns__id", "any", mCampaignId);

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

//                    reportStat.setVisibility(View.VISIBLE);

//                    reportCounter.setText(String.valueOf(reportCount));
//
//                    reportCountLabel.setText(resources.getQuantityString(R.plurals.post_label, reportCount, reportCount));

                    setPostCountState(reportCount);

                } else {

//                    reportStat.setVisibility(View.GONE);

                    setPostCountState(reportCount);

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

        /* IMPORTANT
            Don't set a scroll listener unless necessary,
            otherwise it may trigger infinite API requests
            when empty collection messages overflow the screen.
            */

        if (list.size() > 1) attachScrollListener();

    }

    private void fetchAuthUser() {

        final String mAccessToken = mSharedPreferences.getString("access_token", "");

        Log.d("", mAccessToken);

        // We shouldn't need to retrieve this value again, but we'll deal with that issue later
        int userId = mSharedPreferences.getInt("user_id", 0);

        RestClient.getUserService().getUser(mAccessToken, "application/json", userId, new Callback<User>() {

            @Override
            public void success(User user, Response response) {

//                if (!user.properties.isOrganizationMember(mCampaign.id)) {
//
//                    joinOrganization.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.green_1)));
//
//                    joinOrganization.setVisibility(View.VISIBLE);
//
//                    joinOrganization.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//
//                            joinOrganization(mCampaign);
//
//                        }
//
//                    });
//
//                }

                // Reset the user's stored groups.

//                SharedPreferences groupMembership = getSharedPreferences(getString(R.string.group_membership_key), 0);

//                groupMembership.edit().clear().apply();

//                for (Group group : user.properties.groups) {
//
//                    ModelStorage.storeModel(groupMembership, group, String.format("group_%s", group.properties.organizationId));
//
//                }

            }

            @Override
            public void failure(RetrofitError error) {

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(CampaignProfileActivity.this, SignInActivity.class));

                    }

                }

            }

        });

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

        Picasso.with(this).cancelRequest(campaignImage);

        ButterKnife.unbind(this);

        ModelStorage.removeModel(mSharedPreferences, "stored_organization");

    }

}
