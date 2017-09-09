package com.viableindustries.waterreporter;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.viableindustries.waterreporter.data.CancelableCallback;
import com.viableindustries.waterreporter.data.FeatureCollection;
import com.viableindustries.waterreporter.data.GroupListHolder;
import com.viableindustries.waterreporter.data.HucFeature;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.OrganizationService;
import com.viableindustries.waterreporter.data.QueryFilter;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.Territory;
import com.viableindustries.waterreporter.data.TerritoryHolder;
import com.viableindustries.waterreporter.dialogs.TimelineFilterDialog;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static java.lang.Boolean.TRUE;

public class TerritoryActivity extends AppCompatActivity implements TimelineFilterDialog.TimelineFilterDialogCallback {

    FlexboxLayout profileMeta;

    LinearLayout profileStats;

    FlexboxLayout fblPostCount;

    TextView postCountLabel;

    FlexboxLayout fblActionCount;

    TextView actionCountLabel;

    FlexboxLayout fblGroupCount;

    TextView groupCountLabel;

    TextView territoryName;

    TextView territoryStates;

    LinearLayout promptBlock;

    TextView promptMessage;

    Button startPostButton;

    FlexboxLayout fblFilterGroup;

    TextView tvFilterCategory;

    @Bind(R.id.customActionBar)
    LinearLayout customActionBar;

    @Bind(R.id.actionBarTitle)
    TextView actionBarTitle;

    @Bind(R.id.actionBarSubtitle)
    TextView actionBarSubtitle;

    @Bind(R.id.backArrow)
    RelativeLayout backArrow;

    //    @Bind(R.id.mapview)
    MapView mapView;

    private MapboxMap mMapboxMap;

    @Bind(R.id.timeline_items)
    ListView timeLine;

//    @Bind(R.id.accessMap)
    FloatingActionButton accessMap;

    protected TimelineAdapter timelineAdapter;

    protected List<Report> reportCollection = new ArrayList<Report>();

    private String territoryNameText;

    private int territoryId;

    private String mActionQuery;

    private String mLikeQuery;

    private String mCommentQuery;

    private String mStoryQuery;

    private String mMasterQuery;

    private int actionCount = 0;

    private int mStoryCount = 0;

    private int mLikeCount = 0;

    private int mCommentCount = 0;

    private int reportCount = 99999999;

    private boolean actionFocus = false;

    private int mapButtonTopOffset;

    private Context mContext;

    private Territory territory;

    private SharedPreferences prefs;

    private Resources resources;

    private EndlessScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here.
        Mapbox.getInstance(this, getString(R.string.mapBoxToken));

        setContentView(R.layout.activity_territory);

        ButterKnife.bind(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        mContext = this;

        resources = getResources();

//        mapButtonTopOffset = 320 - 28;

        float scale = getResources().getDisplayMetrics().density;
        mapButtonTopOffset = (int) (296 * scale);

        if (Build.VERSION.SDK_INT >= 19) {

            setStatusBarTranslucent(true);

            int statusBarHeight = AttributeTransformUtility.getStatusBarHeight(resources);

            mapButtonTopOffset -= statusBarHeight;

        }

//        float scale = getResources().getDisplayMetrics().density;
//        final int mapButtonTopOffsetPixels = (int) (mapButtonTopOffset * scale);

//        accessMap.animate().y(mapButtonTopOffsetPixels).setDuration(250).start();
//
//        accessMap.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.splash_blue)));
//
//        accessMap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ReportHolder.setReport(null);
//                startActivity(new Intent(TerritoryActivity.this, TerritoryMapActivity.class));
//            }
//        });

        territory = TerritoryHolder.getTerritory();

        // Hide the docked metadata view
        customActionBar.setBackgroundColor(Color.TRANSPARENT);
        actionBarTitle.setAlpha(0.0f);
        actionBarSubtitle.setAlpha(0.0f);

        // Set refresh listener on report feed container

//        timeLineContainer.setOnRefreshListener(
//                new SwipeRefreshLayout.OnRefreshListener() {
//                    @Override
//                    public void onRefresh() {
//                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");
//                        // This method performs the actual data-refresh operation.
//                        // The method calls setRefreshing(false) when it's finished.
//
//                        countReports(complexQuery, "state");
//
//                        resetStats();
//
//                    }
//                }
//        );

        // Set color of swipe refresh arrow animation

//        timeLineContainer.setColorSchemeResources(R.color.waterreporter_blue);

        // Inflate and insert timeline header view

        addListViewHeader();

        // Initialize MapView

        mapView.onCreate(savedInstanceState);

        // With the header layout in place, render the map

        renderMap(mapView);

        // Generate all required query strings

        generateQueries();

//        countReports(mActionQuery, "state");

        // Count related groups

        fetchOrganizations(10, 1, buildQuery(false, "group", null));

        // Retrieve first batch of posts

        if (reportCollection.isEmpty()) {

//            timeLineContainer.setRefreshing(true);

            mMasterQuery = buildQuery(true, "report", null);

            fetchPosts(5, 1, mMasterQuery, true);

        }

        scrollListener = new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new data needs to be appended to the list

                fetchPosts(5, page, mMasterQuery, false);

                return true; // ONLY if more data is actually being loaded; false otherwise.

            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                // Determine the vertical offset of the profile view in the ListView header

                int[] locations = new int[2];
                profileMeta.getLocationOnScreen(locations);
                int x = locations[0];
                int y = locations[1];
                Log.v("header-offset", "" + y);
                if (y <= 24) {

                    if (actionBarTitle.getAlpha() < 1.0) {

                        int colorFrom = Color.TRANSPARENT;
                        int colorTo = ContextCompat.getColor(TerritoryActivity.this, R.color.splash_blue);
                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                        colorAnimation.setDuration(250); // milliseconds
                        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                customActionBar.setBackgroundColor((int) animator.getAnimatedValue());
                            }

                        });
                        colorAnimation.start();

                        actionBarTitle.setAlpha(1.0f);
                        actionBarSubtitle.setAlpha(0.8f);

                    }

                } else {

                    if (actionBarTitle.getAlpha() > 0.0) {

                        int colorFrom = ContextCompat.getColor(TerritoryActivity.this, R.color.splash_blue);
                        int colorTo = Color.TRANSPARENT;
                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                        colorAnimation.setDuration(200); // milliseconds
                        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                customActionBar.setBackgroundColor((int) animator.getAnimatedValue());
                            }

                        });
                        colorAnimation.start();

                        actionBarTitle.setAlpha(0.0f);
                        actionBarSubtitle.setAlpha(0.0f);

                    }

                }

            }

        };

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    protected void generateQueries() {

        // Count reports with actions

        mActionQuery = buildQuery(true, "report", new Object[][]{
                {"state", "eq", "closed"}
        });

        // Count reports with stories

        mStoryQuery = buildQuery(true, "report", new Object[][]{
                {"social", "any", new QueryFilter("report_id", "is_not_null", null)}
        });

        // Count reports with likes

        mLikeQuery = buildQuery(true, "report", new Object[][]{
                {"likes", "any", new QueryFilter("report_id", "is_not_null", null)}
        });

        // Count reports with comments

        mCommentQuery = buildQuery(true, "report", new Object[][]{
                {"comments", "any", new QueryFilter("report_id", "is_not_null", null)}
        });

    }

    protected void renderMap(final MapView mapView) {

        Runnable r = new Runnable() {
            @Override
            public void run() {

                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(final MapboxMap mapboxMap) {

                        mapboxMap.getUiSettings().setAllGesturesEnabled(false);

                        mapboxMap.setMinZoomPreference(6.0f);
                        mapboxMap.setMaxZoomPreference(10.0f);

                        CameraPosition position = new CameraPosition.Builder()
                                .target(new LatLng(0, 0)) // Sets the new camera position
                                .zoom(10) // Sets the zoom to level 10
                                .build(); // Builds the CameraPosition object from the builder

                        mapboxMap.animateCamera(CameraUpdateFactory
                                .newCameraPosition(position), 500);

                        TerritoryHelpers.fetchTerritoryGeometry(mContext, territory, new TerritoryGeometryCallbacks() {

                            @Override
                            public void onSuccess(@NonNull HucFeature hucFeature) {

                                List<LatLng> latLngs = new ArrayList<LatLng>();

                                LatLng southWest = new LatLng(hucFeature.properties.bounds.get(1), hucFeature.properties.bounds.get(0));
                                LatLng northEast = new LatLng(hucFeature.properties.bounds.get(3), hucFeature.properties.bounds.get(2));

                                latLngs.add(southWest);
                                latLngs.add(northEast);

                                territoryStates.setText(hucFeature.properties.states.concat);
                                actionBarSubtitle.setText(hucFeature.properties.states.concat);

                                // Move camera to watershed bounds
                                LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(latLngs).build();
                                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 20), 500);

                            }

                            @Override
                            public void onError(@NonNull RetrofitError error) {

                                Response errorResponse = error.getResponse();

                                // If we have a valid response object, check the status code and redirect to log in view if necessary

                                if (errorResponse != null) {

                                    int status = errorResponse.getStatus();

                                    if (status == 403) {

                                        mContext.startActivity(new Intent(mContext, SignInActivity.class));

                                    }

                                }
                            }

                        });

                        String code = AttributeTransformUtility.getTerritoryCode(territory);
                        String url = String.format("https://huc.waterreporter.org/8/%s", code);

                        try {

                            URL geoJsonUrl = new URL(url);
                            GeoJsonSource geoJsonSource = new GeoJsonSource("geojson", geoJsonUrl);
                            mapboxMap.addSource(geoJsonSource);

                            // Create a FillLayer with style properties

                            FillLayer layer = new FillLayer("geojson", "geojson");

                            layer.withProperties(
                                    fillColor("#9843c4"),
                                    fillOpacity(0.4f)
                            );

                            mapboxMap.addLayer(layer);

                        } catch (MalformedURLException e) {

                            Log.d("Malformed URL", e.getMessage());

                        }

                    }
                });

            }

        };

        Handler h = new Handler();
        h.postDelayed(r, 100);

    }

    protected void setReportCountState(int count) {

        if (promptBlock != null) {

            if (count < 1) {

                promptBlock.setVisibility(View.VISIBLE);

                promptMessage.setText(getString(R.string.prompt_no_posts_watershed));

            } else {

                promptBlock.setVisibility(View.GONE);

                promptMessage.setText("");

            }

        }

    }

    protected void addListViewHeader() {

        LayoutInflater inflater = getLayoutInflater();

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.watershed_profile_header, timeLine, false);

        mapView = (MapView) header.findViewById(R.id.mapView);

        accessMap = (FloatingActionButton) header.findViewById(R.id.accessMap);

        accessMap.animate().y(mapButtonTopOffset).setDuration(1600).start();

        accessMap.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.splash_blue)));

        accessMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReportHolder.setReport(null);
                startActivity(new Intent(TerritoryActivity.this, TerritoryMapActivity.class));
            }
        });

        promptBlock = (LinearLayout) header.findViewById(R.id.promptBlock);
        promptMessage = (TextView) header.findViewById(R.id.prompt);
        startPostButton = (Button) header.findViewById(R.id.startPost);

        // Add text and click listener to startPostButton

        startPostButton.setText(getString(R.string.share_post_prompt));

        startPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPost();
            }
        });

        territoryName = (TextView) header.findViewById(R.id.territoryName);

        territoryStates = (TextView) header.findViewById(R.id.states);

        fblPostCount = (FlexboxLayout) header.findViewById(R.id.postCount);

//        fblActionCount = (FlexboxLayout) header.findViewById(R.id.actionCount);

        fblGroupCount = (FlexboxLayout) header.findViewById(R.id.groupCount);

        postCountLabel = (TextView) header.findViewById(R.id.postCountLabel);

        actionCountLabel = (TextView) header.findViewById(R.id.actionCountLabel);

        groupCountLabel = (TextView) header.findViewById(R.id.groupCountLabel);

        profileMeta = (FlexboxLayout) header.findViewById(R.id.profileMeta);

        profileStats = (LinearLayout) header.findViewById(R.id.profileStats);

        fblFilterGroup = (FlexboxLayout) header.findViewById(R.id.filterGroup);

        tvFilterCategory = (TextView) header.findViewById(R.id.filterCategory);

        fblFilterGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TimelineFilterDialog timelineFilterDialog = new TimelineFilterDialog();

                timelineFilterDialog.show(getSupportFragmentManager(), "timeline-filter-dialog");

            }
        });

        try {

            territoryId = territory.id;

        } catch (NullPointerException e) {

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

        territoryNameText = AttributeTransformUtility.parseWatershedName(territory, false);

        territoryName.setText(territoryNameText);
        actionBarTitle.setText(territoryNameText);

        // Attach click listeners to stat elements

        fblGroupCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, RelatedGroupsActivity.class);

                intent.putExtra("GENERIC_USER", TRUE);

                startActivity(intent);

            }
        });

        // Add populated header view to report timeline

        timeLine.addHeaderView(header, null, false);

    }

    protected void fetchOrganizations(int limit, int page, final String query) {

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        RestAdapter restAdapter = OrganizationService.restAdapter;

        OrganizationService service = restAdapter.create(OrganizationService.class);

        service.getOrganizations(accessToken, "application/json", page, limit, query, new CancelableCallback<OrganizationFeatureCollection>() {

            @Override
            public void onSuccess(OrganizationFeatureCollection organizationFeatureCollection, Response response) {

                ArrayList<Organization> organizations = organizationFeatureCollection.getFeatures();

                if (!organizations.isEmpty()) {

                    int groupCount = organizations.size();

                    fblGroupCount.setVisibility(View.VISIBLE);
                    groupCountLabel.setText(String.format("%s %s", groupCount, resources.getQuantityString(R.plurals.group_label, groupCount, groupCount)).toLowerCase());

                    GroupListHolder.setList(organizations);

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

    private String buildQuery(boolean order, String collection, Object[][] optionalFilters) {

        List<QuerySort> queryOrder = null;

        List<Object> queryFilters = new ArrayList<>();

        // Create order_by list and add a sort parameter

        if (order) {

            queryOrder = new ArrayList<QuerySort>();

            QuerySort querySort = new QuerySort("created", "desc");

            queryOrder.add(querySort);

        }

        if (collection.equals("group")) {

            QueryFilter territoryFilter = new QueryFilter("huc_8_code", "eq", territory.properties.huc_8_code);

            QueryFilter complexVal = new QueryFilter("territory", "has", territoryFilter);

            QueryFilter complexFilter = new QueryFilter("reports", "any", complexVal);

            queryFilters.add(complexFilter);

        } else {

            QueryFilter complexVal = new QueryFilter("huc_8_code", "eq", territory.properties.huc_8_code);

            QueryFilter territoryFilter = new QueryFilter("territory", "has", complexVal);

            queryFilters.add(territoryFilter);

            if (optionalFilters != null) {

                for (Object[] filterComponents : optionalFilters) {

                    QueryFilter optionalFilter = new QueryFilter(((String) filterComponents[0]), ((String) filterComponents[1]), filterComponents[2]);

                    queryFilters.add(optionalFilter);

                }

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

                if (refresh) reportCount = featureCollection.getProperties().num_results;

                String countLabel;

                if (reportCount > 0) {

                    fblPostCount.setVisibility(View.VISIBLE);

                    countLabel = String.format("%s %s", reportCount, resources.getQuantityString(R.plurals.post_label, reportCount, reportCount)).toLowerCase();

                } else {

                    countLabel = resources.getString(R.string.zero_posts);

                }

                postCountLabel.setText(countLabel);

                setReportCountState(reportCount);

                if (refresh) {

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

//                    timeLineContainer.setRefreshing(false);

                } catch (NullPointerException e) {

                    finish();

                }

            }

            @Override
            public void onFailure(RetrofitError error) {

                try {

//                    timeLineContainer.setRefreshing(false);

                } catch (NullPointerException e) {

                    finish();

                }

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

    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    public void filterAll() {

        if (timeLine != null) {

            timeLine.setSelection(0);

        }

        tvFilterCategory.setText("");

        mMasterQuery = buildQuery(true, "report", null);

        fetchPosts(5, 1, mMasterQuery, true);

    }

    @Override
    public void filterOnActions() {

        if (timeLine != null) {

            timeLine.setSelection(0);

        }

        tvFilterCategory.setText(resources.getString(R.string.filter_category_actions));

        mMasterQuery = mActionQuery;

        fetchPosts(5, 1, mActionQuery, true);

    }

    @Override
    public void filterOnLikes() {

        if (timeLine != null) {

            timeLine.setSelection(0);

        }

        tvFilterCategory.setText(resources.getString(R.string.filter_category_likes));

        mMasterQuery = mLikeQuery;

        fetchPosts(5, 1, mLikeQuery, true);

    }

    @Override
    public void filterOnComments() {

        if (timeLine != null) {

            timeLine.setSelection(0);

        }

        tvFilterCategory.setText(resources.getString(R.string.filter_category_comments));

        mMasterQuery = mCommentQuery;

        fetchPosts(5, 1, mCommentQuery, true);

    }

    @Override
    public void filterOnStories() {

        if (timeLine != null) {

            timeLine.setSelection(0);

        }

        tvFilterCategory.setText(resources.getString(R.string.filter_category_stories));

        mMasterQuery = mStoryQuery;

        fetchPosts(5, 1, mStoryQuery, true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        ButterKnife.unbind(this);

        // Cancel all pending network requests

        CancelableCallback.cancelAll();

    }

}
