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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
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

import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
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
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.GroupListHolder;
import com.viableindustries.waterreporter.data.HUCFeature;
import com.viableindustries.waterreporter.data.HUCGeometryCollection;
import com.viableindustries.waterreporter.data.HUCGeometryService;
import com.viableindustries.waterreporter.data.MappedReportsHolder;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.OrganizationHolder;
import com.viableindustries.waterreporter.data.OrganizationMemberList;
import com.viableindustries.waterreporter.data.OrganizationService;
import com.viableindustries.waterreporter.data.QueryFilter;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.Territory;
import com.viableindustries.waterreporter.data.TerritoryGroupList;
import com.viableindustries.waterreporter.data.TerritoryHolder;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserCollection;
import com.viableindustries.waterreporter.data.UserOrgPatch;
import com.viableindustries.waterreporter.data.UserService;
import com.viableindustries.waterreporter.mbox.CustomMarkerView;
import com.viableindustries.waterreporter.mbox.CustomMarkerViewOptions;

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

public class TerritoryMapActivity extends AppCompatActivity {

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

    @Bind(R.id.postList)
    RecyclerView postList;

    private MapboxMap mMapboxMap;

    private MappedReportsHolder mappedReportsHolder;

    protected TimelineAdapter timelineAdapter;

    protected List<Report> reportCollection = new ArrayList<Report>();

    private String territoryNameText;

    private Context context;

    private Territory territory;

    private SharedPreferences prefs;

    private Resources resources;

    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here.
        Mapbox.getInstance(this, getString(R.string.mapBoxToken));

        setContentView(R.layout.activity_territory_map);

        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= 19) {

            AttributeTransformUtility.setStatusBarTranslucent(getWindow(), true);

        }

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        context = this;

        resources = getResources();

        mappedReportsHolder = new MappedReportsHolder();

        territory = TerritoryHolder.getTerritory();

        territoryNameText = territory.properties.huc_8_name;

        actionBarTitle.setText(territoryNameText);

        // Retrieve first batch of posts

        if (reportCollection.isEmpty()) {

            fetchReports(5, 1, buildQuery(true, "report", null), false);

        }

        // RecyclerView

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        postList.setHasFixedSize(true);

        final SnapHelper snapHelper = new GravitySnapHelper(Gravity.START);
        snapHelper.attachToRecyclerView(postList);

        postList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                CardView cardView = (CardView) snapHelper.findSnapView(mLayoutManager);

                if (cardView != null) {

                    int idx = (int) cardView.getTag();

                    Report r = mappedReportsHolder.getReport(String.format("%s-%s", idx, "r"));

                    Log.v("view-tag", "" + idx);

                    Geometry geometry = r.geometry.geometries.get(0);

                    LatLng postLocation = new LatLng(geometry.coordinates.get(1), geometry.coordinates.get(0));

                    CameraPosition position = new CameraPosition.Builder()
                            .target(new LatLng(postLocation.getLatitude(), postLocation.getLongitude())) // Sets the new camera position
                            .zoom(12) // Sets the zoom
                            .build(); // Creates a CameraPosition from the builder

                    mMapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position), 500);

                }

            }
        });

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        postList.setLayoutManager(mLayoutManager);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {

                mMapboxMap = mapboxMap;

                fetchGeometry();

                final MarkerViewManager markerViewManager = mapboxMap.getMarkerViewManager();

                markerViewManager.addMarkerViewAdapter(new MarkerAdapter(context, postList, mLayoutManager, mapboxMap, mappedReportsHolder));

                fetchReports(50, 1, buildQuery(true, "report", null), false);

            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        customActionBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

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

                LatLng southWest = new LatLng(hucFeature.properties.bounds.get(1), hucFeature.properties.bounds.get(0));
                LatLng northEast = new LatLng(hucFeature.properties.bounds.get(3), hucFeature.properties.bounds.get(2));

                latLngs.add(southWest);
                latLngs.add(northEast);

                actionBarSubtitle.setText(hucFeature.properties.states.concat);

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

    private String buildQuery(boolean order, String collection, String[][] optionalFilters) {

        List<QuerySort> queryOrder = null;

        List<Object> queryFilters = new ArrayList<>();

        // Create order_by list and add a sort parameter

        if (order) {

            queryOrder = new ArrayList<QuerySort>();

            QuerySort querySort = new QuerySort("created", "desc");

            queryOrder.add(querySort);

        }

        QueryFilter complexVal = new QueryFilter("huc_8_name", "eq", territory.properties.huc_8_name);

        QueryFilter territoryFilter = new QueryFilter("territory", "has", complexVal);

        queryFilters.add(territoryFilter);

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

        Log.d("", accessToken);

        Log.d("URL", query);

        RestAdapter restAdapter = ReportService.restAdapter;

        ReportService service = restAdapter.create(ReportService.class);

        service.getReports(accessToken, "application/json", page, limit, query, new Callback<FeatureCollection>() {

            @Override
            public void success(FeatureCollection featureCollection, Response response) {

                List<Report> reports = featureCollection.getFeatures();

                Log.v("list", reports.toString());

                onFetchSuccess(reports);

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

    private void onFetchSuccess(List<Report> reports) {

        // specify an adapter (see also next example)
        mAdapter = new MarkerCardAdapter(this, reports);

        try {

            postList.setAdapter(mAdapter);

        } catch (NullPointerException e) {

            finish();

            return;

        }

        int idx = 0;

        for (Report report : reports) {

            Geometry geometry = report.geometry.geometries.get(0);

            CustomMarkerViewOptions options = new CustomMarkerViewOptions();
            options.position(new LatLng(geometry.coordinates.get(1), geometry.coordinates.get(0)));
            options.anchor(0.5f, 0.5f);
            options.flat(true);
            options.reportId(report.id);
            options.thumbNail(report.properties.images.get(0).properties.icon_retina);
            options.fullImage(report.properties.images.get(0).properties.square_retina);
            options.status(report.properties.state);
            options.inFocus(0);
            options.index(idx);

            idx++;

            latLngs.add(new LatLng(geometry.coordinates.get(1), geometry.coordinates.get(0)));

            mMapboxMap.addMarker(options);

            mappedReportsHolder.addReport(String.format("%s-%s", report.id, "r"), report);

        }

        // Move camera to watershed bounds
        LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(latLngs).build();
        mMapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100, 100, 100, 100), 2000);

    }

    // Adapter for our custom marker view

    private static class MarkerAdapter extends MapboxMap.MarkerViewAdapter<CustomMarkerView> {

        private LayoutInflater inflater;
        private MapboxMap mapboxMap;
        private MappedReportsHolder mappedReportsHolder;
        private Context ctxt;
        private RecyclerView recyclerView;
        private LinearLayoutManager layoutManager;

        public MarkerAdapter(@NonNull Context context, @NonNull RecyclerView recyclerView, @NonNull LinearLayoutManager layoutManager, @NonNull MapboxMap mapboxMap, @NonNull MappedReportsHolder mappedReportsHolder) {
            super(context);
            this.ctxt = context;
            this.inflater = LayoutInflater.from(context);
            this.mapboxMap = mapboxMap;
            this.mappedReportsHolder = mappedReportsHolder;
            this.recyclerView = recyclerView;
            this.layoutManager = layoutManager;
        }

        @Nullable
        @Override
        public View getView(@NonNull CustomMarkerView marker, @Nullable View convertView, @NonNull ViewGroup parent) {

            MarkerAdapter.ViewHolder viewHolder;

            if (convertView == null) {

                viewHolder = new MarkerAdapter.ViewHolder();

                convertView = inflater.inflate(R.layout.view_marker_dot, parent, false);

                convertView.setTag(viewHolder);

            } else {

                viewHolder = (MarkerAdapter.ViewHolder) convertView.getTag();

            }

            return convertView;

        }

        @Override
        public boolean onSelect(@NonNull final CustomMarkerView marker, @NonNull final View convertView, boolean reselectionForViewReuse) {

            Log.d("reportKeyFromMarkerTap", String.format("%s-%s", marker.getReportId(), "r"));

            Report r = mappedReportsHolder.getReport(String.format("%s-%s", marker.getReportId(), "r"));

            ReportHolder.setReport(r);

            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(marker.getPosition().getLatitude(), marker.getPosition().getLongitude())) // Sets the new camera position
                    .zoom(12) // Sets the zoom
                    .build(); // Creates a CameraPosition from the builder

            mapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 500);

            int idx = marker.getIndex();

            layoutManager.scrollToPositionWithOffset(idx, 0);

            // false indicates that we are calling selectMarker ourselves
            // true will let the system call it for you, which will result in showing an InfoWindow instantly

            return false;
        }

        @Override
        public void onDeselect(@NonNull CustomMarkerView marker, @NonNull final View convertView) {

            //

        }

        private static class ViewHolder {
//            FrameLayout markerContainer;
//            ImageView image;
//            ImageView actionBadge;
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
