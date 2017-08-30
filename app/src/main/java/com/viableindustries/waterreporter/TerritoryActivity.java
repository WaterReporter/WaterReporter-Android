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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.viableindustries.waterreporter.data.FeatureCollection;
import com.viableindustries.waterreporter.data.GroupListHolder;
import com.viableindustries.waterreporter.data.HUCFeature;
import com.viableindustries.waterreporter.data.HUCGeometryCollection;
import com.viableindustries.waterreporter.data.HUCGeometryService;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.OrganizationService;
import com.viableindustries.waterreporter.data.QueryFilter;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.Territory;
import com.viableindustries.waterreporter.data.TerritoryHolder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;

public class TerritoryActivity extends AppCompatActivity {

    FlexboxLayout profileMeta;

    LinearLayout profileStats;

    LinearLayout llReportCount;

    TextView postCountLabel;

    LinearLayout llActionCount;

    TextView actionCountLabel;

    LinearLayout llGroupCount;

    TextView groupCountLabel;

    TextView territoryName;

    TextView territoryStates;

    List<LatLng> latLngs = new ArrayList<LatLng>();

    @Bind(R.id.customActionBar)
    LinearLayout customActionBar;

    @Bind(R.id.actionBarTitle)
    TextView actionBarTitle;

    @Bind(R.id.actionBarSubtitle)
    TextView actionBarSubtitle;

    @Bind(R.id.backArrow)
    RelativeLayout backArrow;

    @Bind(R.id.mapview)
    MapView mapView;

    private MapboxMap mMapboxMap;

    @Bind(R.id.timeline_items)
    ListView timeLine;

    FloatingActionButton accessMap;

    protected TimelineAdapter timelineAdapter;

    protected List<Report> reportCollection = new ArrayList<Report>();

    private String territoryNameText;

    private int territoryId;

    private String complexQuery;

    private int actionCount = 0;

    private int reportCount = 99999999;

    private boolean actionFocus = false;

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

        if (Build.VERSION.SDK_INT >= 19) {

            setStatusBarTranslucent(true);

        }

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        mContext = this;

        resources = getResources();

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

        // Count reports with actions

        complexQuery = buildQuery(true, "report", new String[][]{
                {"state", "eq", "closed"}
        });

        countReports(complexQuery, "state");

        // Count related groups

        fetchOrganizations(10, 1, buildQuery(false, "group", null));

        // Retrieve first batch of posts

        if (reportCollection.isEmpty()) {

//            timeLineContainer.setRefreshing(true);

            fetchReports(5, 1, buildQuery(true, "report", null), false);

        }

        scrollListener = new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new data needs to be appended to the list

                if (actionFocus) {

                    fetchReports(5, page, complexQuery, false);

                } else {

                    fetchReports(5, page, buildQuery(true, "report", null), false);

                }

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

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {

                mMapboxMap = mapboxMap;

                TerritoryHelpers.fetchTerritoryGeometry(mContext, territory, new TerritoryGeometryCallbacks() {

                    @Override
                    public void onSuccess(@NonNull HUCFeature hucFeature) {

                        LatLng southWest = new LatLng(hucFeature.properties.bounds.get(1), hucFeature.properties.bounds.get(0));
                        LatLng northEast = new LatLng(hucFeature.properties.bounds.get(3), hucFeature.properties.bounds.get(2));

                        latLngs.add(southWest);
                        latLngs.add(northEast);

                        territoryStates.setText(hucFeature.properties.states.concat);
                        actionBarSubtitle.setText(hucFeature.properties.states.concat);

                        // Move camera to watershed bounds
                        LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(latLngs).build();
                        mMapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100, 100, 100, 100), 3000);

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

                try

                {

                    URL geoJsonUrl = new URL(url);
                    GeoJsonSource geoJsonSource = new GeoJsonSource("geojson", geoJsonUrl);
                    mapboxMap.addSource(geoJsonSource);

                    // Create a FillLayer with style properties

                    FillLayer layer = new FillLayer("geojson", "geojson");

                    layer.withProperties(
                            fillColor("#4355b8"),
                            fillOpacity(0.4f)
                    );

                    mapboxMap.addLayer(layer);

                } catch (
                        MalformedURLException e)

                {

                    Log.d("Malformed URL", e.getMessage());

                }

            }
        });

        backArrow.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    protected void addListViewHeader() {

        LayoutInflater inflater = getLayoutInflater();

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.watershed_profile_header, timeLine, false);

        territoryName = (TextView) header.findViewById(R.id.territoryName);

        territoryStates = (TextView) header.findViewById(R.id.states);

        llReportCount = (LinearLayout) header.findViewById(R.id.postCount);

        llActionCount = (LinearLayout) header.findViewById(R.id.actionCount);

        llGroupCount = (LinearLayout) header.findViewById(R.id.groupCount);

        postCountLabel = (TextView) header.findViewById(R.id.postCountLabel);

        actionCountLabel = (TextView) header.findViewById(R.id.actionCountLabel);

        groupCountLabel = (TextView) header.findViewById(R.id.groupCountLabel);

        profileMeta = (FlexboxLayout) header.findViewById(R.id.profileMeta);

        profileStats = (LinearLayout) header.findViewById(R.id.profileStats);

        accessMap = (FloatingActionButton) header.findViewById(R.id.accessMap);

        accessMap.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.splash_blue)));

        accessMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TerritoryActivity.this, TerritoryMapActivity.class));
            }
        });

        try {

            territoryId = territory.id;

        } catch (NullPointerException e) {

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

        territoryNameText = territory.properties.huc_8_name;

        territoryName.setText(territoryNameText);
        actionBarTitle.setText(territoryNameText);

        // Add populated header view to report timeline

        timeLine.addHeaderView(header, null, false);

    }

    private void resetStats() {

        postCountLabel.setTextColor(ContextCompat.getColor(TerritoryActivity.this, R.color.base_blue));

        actionCountLabel.setTextColor(ContextCompat.getColor(TerritoryActivity.this, R.color.material_blue_grey950));

        actionFocus = false;

//        timeLineContainer.setRefreshing(true);

        fetchReports(5, 1, buildQuery(true, "report", null), true);

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
                            llActionCount.setVisibility(View.VISIBLE);
                            actionCount = count;
                            actionCountLabel.setText(String.format("%s %s", actionCount, resources.getQuantityString(R.plurals.action_label, actionCount, actionCount)).toLowerCase());
                        }
                        break;
                    default:
                        reportCount = count;
                        postCountLabel.setText(String.format("%s %s", reportCount, resources.getQuantityString(R.plurals.post_label, reportCount, reportCount)).toLowerCase());
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

                        startActivity(new Intent(mContext, SignInActivity.class));

                    }

                }

            }

        });

    }

    protected void fetchOrganizations(int limit, int page, final String query) {

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        RestAdapter restAdapter = OrganizationService.restAdapter;

        OrganizationService service = restAdapter.create(OrganizationService.class);

        service.getOrganizations(accessToken, "application/json", page, limit, query, new Callback<OrganizationFeatureCollection>() {

            @Override
            public void success(OrganizationFeatureCollection organizationFeatureCollection, Response response) {

                ArrayList<Organization> organizations = organizationFeatureCollection.getFeatures();

                if (!organizations.isEmpty()) {

                    int groupCount = organizations.size();

                    llGroupCount.setVisibility(View.VISIBLE);
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

    private String buildQuery(boolean order, String collection, String[][] optionalFilters) {

        List<QuerySort> queryOrder = null;

        List<Object> queryFilters = new ArrayList<>();

        // Create order_by list and add a sort parameter

        if (order) {

            queryOrder = new ArrayList<QuerySort>();

            QuerySort querySort = new QuerySort("created", "desc");

            queryOrder.add(querySort);

        }

        if (collection.equals("group")) {

            QueryFilter territoryFilter = new QueryFilter("huc_8_name", "eq", territory.properties.huc_8_name);

            QueryFilter complexVal = new QueryFilter("territory", "has", territoryFilter);

            QueryFilter complexFilter = new QueryFilter("reports", "any", complexVal);

            queryFilters.add(complexFilter);

        } else {

            QueryFilter complexVal = new QueryFilter("huc_8_name", "eq", territory.properties.huc_8_name);

            QueryFilter territoryFilter = new QueryFilter("territory", "has", complexVal);

            queryFilters.add(territoryFilter);

            if (optionalFilters != null) {

                for (String[] filterComponents : optionalFilters) {

                    QueryFilter optionalFilter = new QueryFilter(filterComponents[0], filterComponents[1], filterComponents[2]);

                    queryFilters.add(optionalFilter);

                }

            }

        }

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(queryFilters, queryOrder);

        return new Gson().toJson(queryParams);

    }

    private void fetchReports(int limit, final int page, String query, final boolean refresh) {

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

                String count = String.format("%s %s", reportCount, resources.getQuantityString(R.plurals.post_label, reportCount, reportCount)).toLowerCase();
                postCountLabel.setText(count);

                if (refresh || reportCollection.isEmpty()) {

                    reportCollection.clear();

                    reportCollection.addAll(reports);

                    scrollListener.resetState();

                    try {

                        timelineAdapter.notifyDataSetChanged();

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

        timelineAdapter = new TimelineAdapter(this, list, false, getSupportFragmentManager());

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
    }

}
