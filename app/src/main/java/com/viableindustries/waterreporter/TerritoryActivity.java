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
import android.support.v4.widget.Space;
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
import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.interfaces.data.territory.TerritoryGeometryCallbacks;
import com.viableindustries.waterreporter.api.interfaces.data.territory.TerritoryHelpers;
import com.viableindustries.waterreporter.api.models.FeatureCollection;
import com.viableindustries.waterreporter.api.models.organization.GroupListHolder;
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.organization.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.query.QueryFilter;
import com.viableindustries.waterreporter.api.models.query.QueryParams;
import com.viableindustries.waterreporter.api.models.query.QuerySort;
import com.viableindustries.waterreporter.api.models.territory.HucFeature;
import com.viableindustries.waterreporter.api.models.territory.Territory;
import com.viableindustries.waterreporter.constants.HucStates;
import com.viableindustries.waterreporter.user_interface.adapters.TimelineAdapter;
import com.viableindustries.waterreporter.user_interface.dialogs.TimelineFilterDialog;
import com.viableindustries.waterreporter.utilities.AttributeTransformUtility;
import com.viableindustries.waterreporter.utilities.EndlessScrollListener;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static java.lang.Boolean.TRUE;

public class TerritoryActivity extends AppCompatActivity implements TimelineFilterDialog.TimelineFilterDialogCallback {

    private FlexboxLayout profileMeta;

    private FlexboxLayout fblPostCount;

    private TextView postCountLabel;

    private FlexboxLayout fblGroupCount;

    private TextView groupCountLabel;

    private TextView mTerritoryName;

    private LinearLayout promptBlock;

    private TextView promptMessage;

    private TextView tvFilterCategory;

    @Bind(R.id.customActionBar)
    LinearLayout customActionBar;

    @Bind(R.id.actionBarTitle)
    TextView actionBarTitle;

    @Bind(R.id.actionBarSubtitle)
    TextView actionBarSubtitle;

    @Bind(R.id.backArrow)
    RelativeLayout backArrow;

    @Bind(R.id.mapView)
    MapView mapView;

    @Bind(R.id.timeline_items)
    ListView timeLine;

    private TimelineAdapter timelineAdapter;

    private final List<Report> reportCollection = new ArrayList<>();

    private String mActionQuery;

    private String mLikeQuery;

    private String mCommentQuery;

    private String mStoryQuery;

    private String mMasterQuery;

    private int reportCount = 99999999;

    private int mapButtonTopOffset;

    private Context mContext;

    private Territory mTerritory;

    private SharedPreferences mSharedPreferences;

    private Resources resources;

    private EndlessScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here.
        Mapbox.getInstance(this, getString(R.string.mapBoxToken));

        setContentView(R.layout.activity_territory);

        ButterKnife.bind(this);

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        mContext = this;

        resources = getResources();

        float scale = getResources().getDisplayMetrics().density;
        mapButtonTopOffset = (int) (296 * scale);

        if (Build.VERSION.SDK_INT >= 19) {

            setStatusBarTranslucent(true);

            int statusBarHeight = AttributeTransformUtility.getStatusBarHeight(resources);

            mapButtonTopOffset -= statusBarHeight;

        }

        // Hide the docked metadata view
        customActionBar.setBackgroundColor(Color.TRANSPARENT);
        actionBarTitle.setAlpha(0.0f);
        actionBarSubtitle.setAlpha(0.0f);

        // Retrieve stored Territory

        retrieveStoredTerritory();

        // Inflate and insert timeline header view

        addListViewHeader();

        // Initialize MapView

        mapView.onCreate(savedInstanceState);

        // With the header layout in place, render the map

        renderMap(mapView);

        // Generate all required query strings

        generateQueries();

        // Count related groups

        fetchOrganizations(10, 1, buildQuery(false, "group", null));

        scrollListener = new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new data should be appended to the list

                fetchPosts(5, page, mMasterQuery, false);

                return true; // ONLY if more data are actually being loaded; false otherwise.

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

    private void retrieveStoredTerritory() {

        mTerritory = ModelStorage.getStoredTerritory(mSharedPreferences);

        try {

            int mTerritoryId = mTerritory.properties.id;

            // Retrieve first batch of posts

            if (reportCollection.isEmpty()) {

                mMasterQuery = buildQuery(true, "report", null);

                fetchPosts(5, 1, mMasterQuery, true);

            }

        } catch (NullPointerException _e) {

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

    }

    private void generateQueries() {

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

    private void renderMap(final MapView mapView) {

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

                        TerritoryHelpers.fetchTerritoryGeometry(mContext, mTerritory, new TerritoryGeometryCallbacks() {

                            @Override
                            public void onSuccess(@NonNull HucFeature hucFeature) {

                                List<LatLng> latLngs = new ArrayList<>();

                                LatLng southWest = new LatLng(hucFeature.properties.bounds.get(1), hucFeature.properties.bounds.get(0));
                                LatLng northEast = new LatLng(hucFeature.properties.bounds.get(3), hucFeature.properties.bounds.get(2));

                                latLngs.add(southWest);
                                latLngs.add(northEast);

//                                mTerritoryStates.setText(hucFeature.properties.states.concat);
//                                actionBarSubtitle.setText(hucFeature.properties.states.concat);

                                // Move camera to watershed bounds
                                LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(latLngs).build();
                                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100), 500);

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

                        String code = AttributeTransformUtility.getTerritoryCode(mTerritory);
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

    private void setReportCountState(int count) {

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

    private void launchMap() {

        /*
        Remove any temporary stored post data since the map
        doesn't need to focus on a particular location. Then
        store watershed (territory) data for retrieval in the
        TerritoryMapActivity.
        */

        ModelStorage.removeModel(mSharedPreferences, "stored_post");

        ModelStorage.storeModel(mSharedPreferences, mTerritory, "stored_territory");

        startActivity(new Intent(TerritoryActivity.this, TerritoryMapActivity.class));

    }

    private void addListViewHeader() {

        LayoutInflater inflater = getLayoutInflater();

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.watershed_profile_header, timeLine, false);

//        mapView = (MapView) header.findViewById(R.id.mapView);

//        Space mapOffsetSpace = (Space) header.findViewById(R.id.mapOffsetSpace);

        RelativeLayout mapOffsetSpace = (RelativeLayout) header.findViewById(R.id.mapOffsetSpace);

        mapOffsetSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                launchMap();

            }
        });

        FloatingActionButton accessMap = (FloatingActionButton) header.findViewById(R.id.accessMap);

//        accessMap.animate().y(mapButtonTopOffset).setDuration(1000).start();

        accessMap.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.splash_blue)));

        accessMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                launchMap();

            }
        });

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

        mTerritoryName = (TextView) header.findViewById(R.id.territoryName);

        TextView mTerritoryStates = (TextView) header.findViewById(R.id.states);

        fblPostCount = (FlexboxLayout) header.findViewById(R.id.postCount);

        fblGroupCount = (FlexboxLayout) header.findViewById(R.id.groupCount);

        postCountLabel = (TextView) header.findViewById(R.id.postCountLabel);

        TextView actionCountLabel = (TextView) header.findViewById(R.id.actionCountLabel);

        groupCountLabel = (TextView) header.findViewById(R.id.groupCountLabel);

        profileMeta = (FlexboxLayout) header.findViewById(R.id.profileMeta);

        LinearLayout profileStats = (LinearLayout) header.findViewById(R.id.profileStats);

        FlexboxLayout fblFilterGroup = (FlexboxLayout) header.findViewById(R.id.filterGroup);

        tvFilterCategory = (TextView) header.findViewById(R.id.filterCategory);

        fblFilterGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TimelineFilterDialog timelineFilterDialog = new TimelineFilterDialog();

                timelineFilterDialog.show(getSupportFragmentManager(), "timeline-filter-dialog");

            }
        });

        try {

            int mTerritoryId = mTerritory.id;

        } catch (NullPointerException e) {

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

        String mTerritoryNameText = AttributeTransformUtility.parseWatershedName(mTerritory, false);

        mTerritoryName.setText(mTerritoryNameText);
        actionBarTitle.setText(mTerritoryNameText);

        mTerritoryStates.setText(HucStates.STATES.get(mTerritory.properties.huc_8_code));
        actionBarSubtitle.setText(HucStates.STATES.get(mTerritory.properties.huc_8_code));

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

    private void fetchOrganizations(int limit, int page, final String query) {

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getOrganizationService().getOrganizations(accessToken, "application/json", page, limit, query, new Callback<OrganizationFeatureCollection>() {

            @Override
            public void success(OrganizationFeatureCollection organizationFeatureCollection, Response response) {

                ArrayList<Organization> organizations = organizationFeatureCollection.getFeatures();

                if (!organizations.isEmpty()) {

                    int groupCount = organizations.size();

                    fblGroupCount.setVisibility(View.VISIBLE);
                    groupCountLabel.setText(String.format("%s %s", groupCount, resources.getQuantityString(R.plurals.group_label, groupCount, groupCount)).toLowerCase());

                    GroupListHolder.setList(organizations);

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

    private String buildQuery(boolean order, String collection, Object[][] optionalFilters) {

        List<QuerySort> queryOrder = null;

        List<Object> queryFilters = new ArrayList<>();

        // Create order_by list and add a sort parameter

        if (order) {

            queryOrder = new ArrayList<>();

            QuerySort querySort = new QuerySort("created", "desc");

            queryOrder.add(querySort);

        }

        if (collection.equals("group")) {

            QueryFilter territoryFilter = new QueryFilter("huc_8_code", "eq", mTerritory.properties.huc_8_code);

            QueryFilter complexVal = new QueryFilter("territory", "has", territoryFilter);

            QueryFilter complexFilter = new QueryFilter("reports", "any", complexVal);

            queryFilters.add(complexFilter);

        } else {

            QueryFilter complexVal = new QueryFilter("huc_8_code", "eq", mTerritory.properties.huc_8_code);

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

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getReportService().getReports(accessToken, "application/json", page, limit, query, new Callback<FeatureCollection>() {

            @Override
            public void success(FeatureCollection featureCollection, Response response) {

                List<Report> reports = featureCollection.getFeatures();

                Log.v("list", reports.toString());

                if (refresh) reportCount = featureCollection.getProperties().num_results;

                setReportCountState(reportCount);

                String countLabel;

                if (reportCount > 0) {

                    fblPostCount.setVisibility(View.VISIBLE);

                    countLabel = String.format("%s %s", reportCount, resources.getQuantityString(R.plurals.post_label, reportCount, reportCount)).toLowerCase();

                } else {

                    countLabel = resources.getString(R.string.zero_posts);

                }

                postCountLabel.setText(countLabel);

                if (refresh) {

                    reportCollection.clear();

                    reportCollection.addAll(reports);

                    scrollListener.resetState();

                    try {

                        timelineAdapter.notifyDataSetChanged();

//                        timeLine.smoothScrollToPosition(0);

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
            public void failure(RetrofitError error) {

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

        intent.putExtra("autoTag", String.format("\u0023%s", mTerritoryName.getText().toString().replaceAll("[^a-zA-Z0-9]+", "")));

        startActivity(intent);

        this.overridePendingTransition(R.anim.animation_enter_right,
                R.anim.animation_exit_left);

    }

    private void setStatusBarTranslucent(boolean makeTranslucent) {
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

        // Retrieve stored Territory

        if (mTerritory == null) retrieveStoredTerritory();

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

        ModelStorage.removeModel(mSharedPreferences, "stored_territory");

        // Cancel all pending network requests

        //Callback.cancelAll();

    }

}