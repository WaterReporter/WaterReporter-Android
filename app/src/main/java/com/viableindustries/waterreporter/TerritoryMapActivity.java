package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.google.gson.Gson;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerViewManager;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.models.FeatureCollection;
import com.viableindustries.waterreporter.api.models.geometry.Geometry;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.post.ReportHolder;
import com.viableindustries.waterreporter.api.models.query.QueryFilter;
import com.viableindustries.waterreporter.api.models.query.QueryParams;
import com.viableindustries.waterreporter.api.models.query.QuerySort;
import com.viableindustries.waterreporter.api.models.territory.Territory;
import com.viableindustries.waterreporter.api.models.territory.TerritoryHolder;
import com.viableindustries.waterreporter.constants.HucStates;
import com.viableindustries.waterreporter.map_box.CustomMarkerView;
import com.viableindustries.waterreporter.map_box.CustomMarkerViewOptions;
import com.viableindustries.waterreporter.map_box.MappedReportsHolder;
import com.viableindustries.waterreporter.user_interface.adapters.MarkerCardAdapter;
import com.viableindustries.waterreporter.utilities.AttributeTransformUtility;
import com.viableindustries.waterreporter.utilities.CancelableCallback;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TerritoryMapActivity extends AppCompatActivity {

    private final List<LatLng> latLngs = new ArrayList<>();

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

    @Bind(R.id.locationIconView)
    ImageView locationIcon;

    private MapboxMap mMapboxMap;

    private MappedReportsHolder mappedReportsHolder;

    private Context mContext;

    private Territory mTerritory;

    private Report mPost;

    private SharedPreferences mSharedPreferences;

    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here.
        Mapbox.getInstance(this, getString(R.string.mapBoxToken));

        setContentView(R.layout.activity_territory_map);

        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= 19) {

            AttributeTransformUtility.setStatusBarTranslucent(getWindow());

        }

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        mContext = this;

        Resources resources = getResources();

        mappedReportsHolder = new MappedReportsHolder();

        // Retrieve stored Territory

        retrieveStoredTerritory();

        // Retrieve stored Post

        retrieveStoredPost();

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
                            .zoom(10) // Sets the zoom
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

                final MarkerViewManager markerViewManager = mapboxMap.getMarkerViewManager();

                markerViewManager.addMarkerViewAdapter(new MarkerAdapter(mContext, postList, mLayoutManager, mapboxMap, mappedReportsHolder));

                try {

                    setWatershedComponents();

                } catch (NullPointerException e) {

                    nullWatershedFallback();

                }

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

    private void retrieveStoredTerritory(){

        mTerritory = TerritoryHolder.getTerritory();

        try {

            int territoryId = mTerritory.properties.id;

        } catch (NullPointerException e) {

            mTerritory = ModelStorage.getStoredTerritory(mSharedPreferences);

            try {

                int territoryId = mTerritory.properties.id;

            } catch (NullPointerException _e) {

                startActivity(new Intent(this, MainActivity.class));

                finish();

            }

        }

    }

    private void retrieveStoredPost(){

        mPost = ReportHolder.getReport();

        try {

            int postId = mPost.properties.id;

        } catch (NullPointerException e) {

            mPost = ModelStorage.getStoredPost(mSharedPreferences);

            try {

                int postId = mPost.properties.id;

            } catch (NullPointerException _e) {

                startActivity(new Intent(this, MainActivity.class));

                finish();

            }

        }

    }

    private void setActionBarTitle() {

        String territoryNameText = AttributeTransformUtility.parseWatershedName(mTerritory, false);

        actionBarTitle.setText(territoryNameText);

        actionBarSubtitle.setText(HucStates.STATES.get(mTerritory.properties.huc_8_code));

    }

    private void nullWatershedFallback() {

        setActionBarTitle();

        try {

            List<Report> posts = new ArrayList<>();

            posts.add(mPost);

            onFetchSuccess(posts);

        } catch (NullPointerException e) {

            finish();

        }

    }

    private void setWatershedComponents() {

        // Load other posts in the watershed
        fetchPosts(50, 1, buildQuery(true, null));

        // Set action bar title
        setActionBarTitle();

    }

    private String buildQuery(boolean order, String[][] optionalFilters) {

        List<QuerySort> queryOrder = null;

        List<Object> queryFilters = new ArrayList<>();

        // Create order_by list and add a sort parameter

        if (order) {

            queryOrder = new ArrayList<>();

            QuerySort querySort = new QuerySort("created", "desc");

            queryOrder.add(querySort);

        }

        QueryFilter complexVal = new QueryFilter("huc_8_name", "eq", mTerritory.properties.huc_8_name);

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

    private void fetchPosts(int limit, final int page, String query) {

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getReportService().getReports(accessToken, "application/json", page, limit, query, new CancelableCallback<FeatureCollection>() {

            @Override
            public void onSuccess(FeatureCollection featureCollection, Response response) {

                List<Report> reports = featureCollection.getFeatures();

                Log.v("list", reports.toString());

                onFetchSuccess(reports);

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

    private int getIndexById(List<Report> posts, int postId) {

        for (Report _post : posts) {

            if (_post.id == postId) return posts.indexOf(_post);
        }

        return -1;

    }

    private void onFetchSuccess(List<Report> posts) {

        // specify an adapter (see also next example)
        RecyclerView.Adapter mAdapter = new MarkerCardAdapter(this, posts);

        try {

            postList.setAdapter(mAdapter);

        } catch (NullPointerException e) {

            finish();

            return;

        }

        int idx = 0;

        if (posts.size() > 1) {

            locationIcon.setVisibility(View.VISIBLE);

            locationIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.orange_red_bias_orange), PorterDuff.Mode.SRC_ATOP);

        }

        for (Report report : posts) {

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
        if (latLngs.size() > 1 && mPost == null) {

            LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(latLngs).build();

            mMapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100, 100, 100, 100), 2000);

        } else {

            LatLng latLng = latLngs.get(0);

            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(latLng.getLatitude(), latLng.getLongitude())) // Sets the new camera position
                    .zoom(10) // Sets the zoom
                    .build(); // Creates a CameraPosition from the builder

            mMapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 500);

        }

        if (mPost != null) {

            int targetPosition = getIndexById(posts, mPost.id);

            mLayoutManager.scrollToPositionWithOffset(targetPosition, 0);

        }

    }

    // Adapter for our custom marker view

    private static class MarkerAdapter extends MapboxMap.MarkerViewAdapter<CustomMarkerView> {

        private final LayoutInflater inflater;
        private final MapboxMap mapboxMap;
        private final MappedReportsHolder mappedReportsHolder;
        private final Context ctxt;
        private final RecyclerView recyclerView;
        private final LinearLayoutManager layoutManager;

        public MarkerAdapter(@NonNull Context aContext, @NonNull RecyclerView recyclerView, @NonNull LinearLayoutManager layoutManager, @NonNull MapboxMap mapboxMap, @NonNull MappedReportsHolder mappedReportsHolder) {
            super(aContext);
            this.ctxt = aContext;
            this.inflater = LayoutInflater.from(aContext);
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

        // Retrieve stored Territory

        retrieveStoredTerritory();

        // Retrieve stored Post

        retrieveStoredPost();

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
