package com.viableindustries.waterreporter;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
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
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerViewManager;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.functions.stops.Stops;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.FeatureCollection;
import com.viableindustries.waterreporter.data.GroupListHolder;
import com.viableindustries.waterreporter.data.HUCFeature;
import com.viableindustries.waterreporter.data.HUCGeometryCollection;
import com.viableindustries.waterreporter.data.HUCGeometryService;
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
import com.viableindustries.waterreporter.data.Territory;
import com.viableindustries.waterreporter.data.TerritoryGroupList;
import com.viableindustries.waterreporter.data.TerritoryHolder;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserCollection;
import com.viableindustries.waterreporter.data.UserOrgPatch;
import com.viableindustries.waterreporter.data.UserService;

import org.w3c.dom.Text;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.mapbox.mapboxsdk.style.functions.Function.property;
import static com.mapbox.mapboxsdk.style.functions.stops.Stop.stop;
import static com.mapbox.mapboxsdk.style.functions.stops.Stops.exponential;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOutlineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static java.lang.Boolean.TRUE;

public class TerritoryActivity extends AppCompatActivity {

//    LinearLayout profileMeta;

    FlexboxLayout profileMeta;

    LinearLayout profileStats;

    LinearLayout reportStat;

    TextView reportCounter;

    TextView reportCountLabel;

    LinearLayout actionStat;

    TextView actionCounter;

    TextView actionCountLabel;

    LinearLayout groupStat;

    TextView groupCounter;

    TextView groupCountLabel;

    TextView territoryName;

    TextView territoryStates;

    LinearLayout sharePrompt;

    FloatingActionButton jumpStart;

    List<LatLng> latLngs = new ArrayList<LatLng>();

    @Bind(R.id.sProfileMeta)
    LinearLayout sProfileMeta;

    @Bind(R.id.sTerritoryName)
    TextView sTerritoryName;

    @Bind(R.id.sStates)
    TextView sStates;

    @Bind(R.id.backArrow)
    RelativeLayout backArrow;

    @Bind(R.id.secondaryMapButton)
    RelativeLayout secondaryMapButton;

//    @Bind(R.id.sReportCount)
//    TextView sReportCount;

    @Bind(R.id.mapview)
    MapView mapView;

    private MapboxMap mMapboxMap;

    //    @Bind(R.id.timeline)
//    SwipeRefreshLayout timeLineContainer;
//
    @Bind(R.id.timeline_items)
    ListView timeLine;
//
//    @Bind(R.id.listTabs)
//    FrameLayout listTabs;

    FloatingActionButton accessMap;

    protected TimelineAdapter timelineAdapter;

    protected List<Report> reportCollection = new ArrayList<Report>();

    private String territoryNameText;

    private String complexQuery;

    private ViewGroup.LayoutParams listViewParams;

    private int territoryId;

    private int actionCount = 0;

    private int reportCount = 99999999;

    private boolean actionFocus = false;

    private boolean hasGroups = false;

    private Context context;

    private Territory territory;

    private SharedPreferences prefs;

    private int socialOptions;

    private Resources resources;

    private EndlessScrollListener scrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here.
        Mapbox.getInstance(this, getString(R.string.mapBoxToken));

        setContentView(R.layout.activity_territory);

        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= 19){

            setStatusBarTranslucent(true);

        }

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        context = this;

        resources = getResources();

        territory = TerritoryHolder.getTerritory();

        // Hide the docked metadata view
//        sProfileMeta.setAlpha(0.0f);
        sTerritoryName.setAlpha(0.0f);
        sStates.setAlpha(0.0f);

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

//        countReports(complexQuery, "state");

        // Count related groups

//        fetchOrganizations(10, 1, buildQuery(false, "group", null));

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
//                Rect myViewRect = new Rect();
//                profileMeta.getGlobalVisibleRect(myViewRect);
//                float x = myViewRect.left;
//                float y = myViewRect.top;
//                Log.v("header-offset", "" + y);

//                Rect offsetViewBounds = new Rect();
//                //returns the visible bounds
//                profileMeta.getDrawingRect(offsetViewBounds);
                //calculates the relative coordinates to the parent

                int[] locations = new int[2];
                profileMeta.getLocationOnScreen(locations);
                int x = locations[0];
                int y = locations[1];
                Log.v("header-offset", "" + y);
                if (y <= 24) {

//                    if (sProfileMeta.getAlpha() < 1.0) {
//
//                        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
//                        fadeIn.setDuration(250);
//                        sProfileMeta.setAlpha(1.0f);
//                        sProfileMeta.startAnimation(fadeIn);
//
//                    }

                    if (sTerritoryName.getAlpha() < 1.0) {

//                        sProfileMeta.setBackgroundColor(ContextCompat.getColor(TerritoryActivity.this, R.color.splash_blue));

                        int colorFrom = Color.TRANSPARENT;
                        int colorTo = ContextCompat.getColor(TerritoryActivity.this, R.color.splash_blue);
                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                        colorAnimation.setDuration(250); // milliseconds
                        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                sProfileMeta.setBackgroundColor((int) animator.getAnimatedValue());
                            }

                        });
                        colorAnimation.start();

                        sTerritoryName.setAlpha(1.0f);
                        sStates.setAlpha(0.8f);

                        secondaryMapButton.setVisibility(View.VISIBLE);

                    }

                } else {

//                    sProfileMeta.setBackgroundColor(Color.TRANSPARENT);

                    if (sTerritoryName.getAlpha() > 0.0) {

                        int colorFrom = ContextCompat.getColor(TerritoryActivity.this, R.color.splash_blue);
                        int colorTo = Color.TRANSPARENT;
                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                        colorAnimation.setDuration(200); // milliseconds
                        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                sProfileMeta.setBackgroundColor((int) animator.getAnimatedValue());
                            }

                        });
                        colorAnimation.start();

                        sTerritoryName.setAlpha(0.0f);
                        sStates.setAlpha(0.0f);

                        secondaryMapButton.setVisibility(View.INVISIBLE);

                    }

//                    sProfileMeta.setAlpha(0.0f);

//                    sProfileMeta.setVisibility(View.GONE);
//
//                    AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
//                    fadeOut.setDuration(250);
////                    fadeOut.setFillAfter(true);
//                    sProfileMeta.setAlpha(0.0f);
//                    sProfileMeta.startAnimation(fadeOut);

                }

//                try {
//                    timeLine.offsetDescendantRectToMyCoords(profileMeta, offsetViewBounds);
//                    int relativeTop = offsetViewBounds.top;
//
//                    if (relativeTop <= 16) {
//
//                        sProfileMeta.setVisibility(View.VISIBLE);
//
//                    } else {
//
//                        sProfileMeta.setVisibility(View.GONE);
//
//                    }
//
//                    Log.v("header-offset", "" + relativeTop);
//                } catch (IllegalArgumentException e) {
//                    return;
//                }

            }

        };

        // Add click listener to share button

//        jumpStart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                startPost();
//
//            }
//        });

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {

                mMapboxMap = mapboxMap;

//                mapboxMap.setMaxZoomPreference(7);
//                mapboxMap.setMinZoomPreference(7);

//                mapboxMap.getUiSettings().setAllGesturesEnabled(false);

                fetchGeometry();

//                final MarkerViewManager markerViewManager = mapboxMap.getMarkerViewManager();
//
//                latitude = originalPost.geometry.geometries.get(0).coordinates.get(1);
//                longitude = originalPost.geometry.geometries.get(0).coordinates.get(0);
//
//                CameraPosition position = new CameraPosition.Builder()
//                        .target(new LatLng(latitude, longitude)) // Sets the new camera position
//                        .zoom(14) // Sets the zoom
//                        .build(); // Creates a CameraPosition from the builder

//                mapboxMap.animateCamera(CameraUpdateFactory
//                        .newCameraPosition(position), 4000);

                //
                String code = String.format("%s", territory.properties.huc_8_code);
                if (code.length() == 7) code = String.format("0%s", code);
                String url = String.format("https://huc.waterreporter.org/8/%s", code);

                try {

                    URL geoJsonUrl = new URL(url);
                    GeoJsonSource geoJsonSource = new GeoJsonSource("geojson", geoJsonUrl);
                    mapboxMap.addSource(geoJsonSource);

                    // Create a FillLayer with style properties

                    FillLayer layer = new FillLayer("geojson", "geojson");

                    layer.withProperties(
                            //fillOutlineColor("#FFFFFF"),
                            fillColor("#6b4ab5"),
                            fillOpacity(0.4f)
                    );

                    mapboxMap.addLayer(layer);

                } catch (MalformedURLException e) {

                    Log.d("Malformed URL", e.getMessage());

                }

            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        secondaryMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, TerritoryMapActivity.class));
            }
        });

    }

    protected void addListViewHeader() {

        LayoutInflater inflater = getLayoutInflater();

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.watershed_profile_header, timeLine, false);

//        sharePrompt = (LinearLayout) header.findViewById(R.id.share_cta);
//
//        jumpStart = (FloatingActionButton) header.findViewById(R.id.jump_start);
//
//        jumpStart.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green_1)));
//
//        // Add click listener to share button
//
//        jumpStart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                startPost();
//
//            }
//        });

        territoryName = (TextView) header.findViewById(R.id.territoryName);

        territoryStates = (TextView) header.findViewById(R.id.states);

        reportCounter = (TextView) header.findViewById(R.id.reportCount);
//
//        actionCounter = (TextView) header.findViewById(R.id.actionCount);
//
//        groupCounter = (TextView) header.findViewById(R.id.groupCount);
//
//        reportCountLabel = (TextView) header.findViewById(R.id.reportCountLabel);
//
//        actionCountLabel = (TextView) header.findViewById(R.id.actionCountLabel);
//
//        groupCountLabel = (TextView) header.findViewById(R.id.groupCountLabel);
//
//        reportStat = (LinearLayout) header.findViewById(R.id.reportStat);
//
//        actionStat = (LinearLayout) header.findViewById(R.id.actionStat);
//
//        groupStat = (LinearLayout) header.findViewById(R.id.groupStat);

        profileMeta = (FlexboxLayout) header.findViewById(R.id.profileMeta);

        accessMap = (FloatingActionButton) header.findViewById(R.id.accessMap);

        accessMap.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.splash_blue)));

        accessMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                accessMap.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.splash_blue_dark)));
                startActivity(new Intent(TerritoryActivity.this, TerritoryMapActivity.class));
            }
        });

//        profileStats = (LinearLayout) header.findViewById(R.id.profileStats);

        try {

            territoryId = territory.id;

        } catch (NullPointerException e) {

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

        territoryNameText = territory.properties.huc_8_name;

        territoryName.setText(territoryNameText);
        sTerritoryName.setText(territoryNameText);

        // Attach click listeners to stat elements

//        reportStat.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                resetStats();
//
//            }
//        });

//        actionStat.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                actionFocus = true;
//
//                actionCounter.setTextColor(ContextCompat.getColor(context, R.color.base_blue));
//                actionCountLabel.setTextColor(ContextCompat.getColor(context, R.color.base_blue));
//
//                reportCounter.setTextColor(ContextCompat.getColor(context, R.color.material_blue_grey950));
//                reportCountLabel.setTextColor(ContextCompat.getColor(context, R.color.material_blue_grey950));
//
//                if (timeLine != null) {
//
//                    timeLine.setSelection(0);
//
//                }
//
//                timeLineContainer.setRefreshing(true);
//
//                fetchReports(5, 1, complexQuery, true);
//
//            }
//        });

//        groupStat.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (hasGroups) {
//
//                    Intent intent = new Intent(context, RelatedGroupsActivity.class);
//
//                    intent.putExtra("GENERIC_USER", TRUE);
//
//                    startActivity(intent);
//
//                }
//
//            }
//        });

        // Add populated header view to report timeline

        timeLine.addHeaderView(header, null, false);

    }

    private void resetStats() {

//        reportCounter.setTextColor(ContextCompat.getColor(TerritoryActivity.this, R.color.base_blue));
//        reportCountLabel.setTextColor(ContextCompat.getColor(TerritoryActivity.this, R.color.base_blue));
//
//        actionCounter.setTextColor(ContextCompat.getColor(TerritoryActivity.this, R.color.material_blue_grey950));
//        actionCountLabel.setTextColor(ContextCompat.getColor(TerritoryActivity.this, R.color.material_blue_grey950));
//
//        actionFocus = false;

//        timeLineContainer.setRefreshing(true);

//        fetchReports(5, 1, buildQuery(true, "report", null), true);

    }

    protected void fetchGeometry() {

        RestAdapter restAdapter = HUCGeometryService.restAdapter;

        HUCGeometryService service = restAdapter.create(HUCGeometryService.class);

        String code = String.format("%s", territory.properties.huc_8_code);
        if (code.length() == 7) code = String.format("0%s", code);

        service.getGeometry("application/json", code, new Callback<HUCGeometryCollection>() {

            @Override
            public void success(HUCGeometryCollection hucGeometryCollection, Response response) {

                HUCFeature hucFeature = hucGeometryCollection.features.get(0);

                Log.v("huc-feature", hucFeature.toString());

//                List<List<Double>> coordinatePairs = hucFeature.geometry.coordinates.get(0);
//
//                for (List<Double> point : coordinatePairs) {
//
//                    Log.v("point", point.toString());
//
//                    LatLng latLng = new LatLng(point.get(1), point.get(0));
//
//                    latLngs.add(latLng);
//
//                }

                LatLng southWest = new LatLng(hucFeature.properties.bounds.get(1), hucFeature.properties.bounds.get(0));
                LatLng northEast = new LatLng(hucFeature.properties.bounds.get(3), hucFeature.properties.bounds.get(2));

                latLngs.add(southWest);
                latLngs.add(northEast);

                territoryStates.setText(hucFeature.properties.states.concat);
                sStates.setText(hucFeature.properties.states.concat);

                // Draw a polygon on the map
//                mMapboxMap.addPolygon(new PolygonOptions()
//                        .addAll(latLngs)
//                        //.strokeColor(Color.parseColor("#FFFFFF"))
//                        .fillColor(Color.parseColor("#806b4ab5")));
//
//                // Draw polyline on the map
//                mMapboxMap.addPolyline(new PolylineOptions()
//                        .addAll(latLngs)
//                        .color(Color.parseColor("#FFFFFF"))
//                        .width(2));

                // Move camera to watershed bounds
                LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(latLngs).build();
                mMapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100, 100, 100, 100), 3000);

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
                        reportCounter.setText(String.format("%s %s", reportCount, resources.getQuantityString(R.plurals.post_label, reportCount, reportCount)).toLowerCase());
//                        sReportCount.setText(String.format("%s %s", reportCount, resources.getQuantityString(R.plurals.post_label, reportCount, reportCount)).toLowerCase());
//                        reportCountLabel.setText(resources.getQuantityString(R.plurals.post_label, reportCount, reportCount));
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

//    protected void fetchOrganizations(int limit, int page, final String query) {
//
//        final String accessToken = prefs.getString("access_token", "");
//
//        Log.d("", accessToken);
//
//        RestAdapter restAdapter = OrganizationService.restAdapter;
//
//        OrganizationService service = restAdapter.create(OrganizationService.class);
//
//        service.getOrganizations(accessToken, "application/json", page, limit, query, new Callback<OrganizationFeatureCollection>() {
//
//            @Override
//            public void success(OrganizationFeatureCollection organizationFeatureCollection, Response response) {
//
//                ArrayList<Organization> organizations = organizationFeatureCollection.getFeatures();
//
//                if (!organizations.isEmpty()) {
//
//                    int groupCount = organizations.size();
//
//                    groupCounter.setText(String.valueOf(groupCount));
//                    groupCountLabel.setText(resources.getQuantityString(R.plurals.group_label, groupCount, groupCount));
//
//                    groupStat.setVisibility(View.VISIBLE);
//
//                    GroupListHolder.setList(organizations);
//
//                    hasGroups = true;
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
//                        startActivity(new Intent(context, SignInActivity.class));
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
                reportCounter.setText(count);

//                if (reportCount > 0) {
//
//                    sharePrompt.setVisibility(View.GONE);
//
//                    reportStat.setVisibility(View.VISIBLE);
//
//                    reportCounter.setText(String.valueOf(reportCount));
//
//                    reportCountLabel.setText(resources.getQuantityString(R.plurals.post_label, reportCount, reportCount));
//
//                } else {
//
//                    try {
//
//                        reportStat.setVisibility(View.GONE);
//
//                        sharePrompt.setVisibility(View.VISIBLE);
//
//                    } catch (NullPointerException e) {
//
//                        finish();
//
//                    }
//
//                }

                if (refresh || reportCollection.isEmpty()) {

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

                        startActivity(new Intent(context, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void populateTimeline(List<Report> list) {

        timelineAdapter = new TimelineAdapter(this, list, false);

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
