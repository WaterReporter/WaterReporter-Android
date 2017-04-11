package com.viableindustries.waterreporter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerViewManager;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.FeatureCollection;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.MappedReportsHolder;
import com.viableindustries.waterreporter.data.QueryFilter;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.mbox.CustomMarkerView;
import com.viableindustries.waterreporter.mbox.CustomMarkerViewOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MapDetailActivity extends AppCompatActivity {

    @Bind(R.id.mapview)
    MapView mapView;

    @Bind(R.id.map_detail)
    RelativeLayout mapContainer;

    private MapboxMap mMapboxMap;

    private double latitude;
    private double longitude;

    private static int reportId;
    private static String reportDescription;
    private static String thumbNail;
    private static String fullImage;
    private static String creationDate;
    private static String watershedName;
    private static String groupList;
    private static String commentCount;
    private static String userName;
    private static String userAvatar;
    private static String status;

    private SharedPreferences sharedPreferences;

    private String mappedReports;

    MappedReportsHolder mappedReportsHolder;

    private Report originalPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mappedReportsHolder = new MappedReportsHolder();

        // Mapbox access token is configured here.
        Mapbox.getInstance(this, getString(R.string.mapBoxToken));

        setContentView(R.layout.activity_map_detail);

        ButterKnife.bind(this);

        // Set a reference to SharedPreferences
        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        // Reset stored list of mapped reports
        sharedPreferences.edit().putString("mappedReports", "").apply();

        // Add report to marker index

        originalPost = ReportHolder.getReport();

        mappedReportsHolder.addReport(String.format("%s-%s", originalPost.id, "r"), originalPost);

        Log.d("reportKey", String.format("%s-%s", originalPost.id, "r"));

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {

                mMapboxMap = mapboxMap;

                final MarkerViewManager markerViewManager = mapboxMap.getMarkerViewManager();

                latitude = originalPost.geometry.geometries.get(0).coordinates.get(1);
                longitude = originalPost.geometry.geometries.get(0).coordinates.get(0);

                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(latitude, longitude)) // Sets the new camera position
                        .zoom(14) // Sets the zoom
                        .build(); // Creates a CameraPosition from the builder

                mapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position), 4000);

                // if you want to customise a ViewMarker you need to extend ViewMarker and provide an adapter implementation
                // set adapters for child classes of ViewMarker
                markerViewManager.addMarkerViewAdapter(new MarkerAdapter(MapDetailActivity.this, mapboxMap, mappedReportsHolder));

                mapboxMap.setOnCameraChangeListener(new MapboxMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition position) {

                        Log.d("mapPosition", position.toString());

                        if (position.zoom >= 14) {

                            LatLngBounds latLngBounds = mapboxMap.getProjection().getVisibleRegion().latLngBounds;

                            double north = latLngBounds.getLatNorth();
                            double south = latLngBounds.getLatSouth();
                            double east = latLngBounds.getLonEast();
                            double west = latLngBounds.getLonWest();

                            String polygon = String.format("SRID=4326;POLYGON((%s %s,%s %s,%s %s,%s %s,%s %s))", west, north, east, north, east, south, west, south, west, north);

                            Log.d("polygonString", polygon);

                            fetchNearbyReports(polygon, originalPost.id);

                        }

                        Log.d("mapPosition", position.toString());

                    }
                });
            }
        });

    }

    // Adapter for our custom marker view

    private static class MarkerAdapter extends MapboxMap.MarkerViewAdapter<CustomMarkerView> {

        private LayoutInflater inflater;
        private MapboxMap mapboxMap;
        private MappedReportsHolder mappedReportsHolder;

        public MarkerAdapter(@NonNull Context context, @NonNull MapboxMap mapboxMap, @NonNull MappedReportsHolder mappedReportsHolder) {
            super(context);
            this.inflater = LayoutInflater.from(context);
            this.mapboxMap = mapboxMap;
            this.mappedReportsHolder = mappedReportsHolder;
        }

        @Nullable
        @Override
        public View getView(@NonNull CustomMarkerView marker, @Nullable View convertView, @NonNull ViewGroup parent) {

            ViewHolder viewHolder;

            if (convertView == null) {

                viewHolder = new ViewHolder();

                if (marker.isInFocus() == 1) {

                    convertView = inflater.inflate(R.layout.view_origin_marker, parent, false);

                } else {

                    convertView = inflater.inflate(R.layout.view_custom_marker, parent, false);

                }

                viewHolder.markerContainer = (FrameLayout) convertView.findViewById(R.id.customMarker);
                viewHolder.image = (ImageView) convertView.findViewById(R.id.imageView);
                viewHolder.actionBadge = (ImageView) convertView.findViewById(R.id.actionBadge);

                convertView.setTag(viewHolder);

            } else {

                viewHolder = (ViewHolder) convertView.getTag();

            }

            // Display badge if report is closed
            if (marker.getStatus().equals("closed")) {

                viewHolder.actionBadge.setVisibility(View.VISIBLE);

            } else {

                viewHolder.actionBadge.setVisibility(View.GONE);

            }

            int markerDimension;

            // Display active marker pin if report is "source"
            if (marker.isInFocus() == 1) {

                markerDimension = getContext().getResources().getDimensionPixelSize(R.dimen.origin_marker_size);

            } else {

                markerDimension = getContext().getResources().getDimensionPixelSize(R.dimen.default_marker_size);

            }

            Picasso.with(getContext()).load(marker.getThumbNail()).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(viewHolder.image);

            return convertView;
        }

        @Override
        public boolean onSelect(@NonNull final CustomMarkerView marker, @NonNull final View convertView, boolean reselectionForViewReuse) {

            // Build new MarkerDetailFragment
            MarkerDetailFragment markerDetailFragment = new MarkerDetailFragment();

            Log.d("reportKeyFromMarkerTap", String.format("%s-%s", marker.getReportId(), "r"));

            ReportHolder.setReport(mappedReportsHolder.getReport(String.format("%s-%s", marker.getReportId(), "r")));

            // Check to see if marker detail is already open
            SharedPreferences prefs = getContext().getSharedPreferences(getContext().getPackageName(), MODE_PRIVATE);

            boolean isOpen = prefs.getBoolean("markerDetailOpen", false);

            if (!isOpen) {

                prefs.edit().putBoolean("markerDetailOpen", true).apply();

                Intent markerIntent = new Intent(getContext(), MarkerDetailActivity.class);

                getContext().startActivity(markerIntent);

            }

            return false;

            // false indicates that we are calling selectMarker after our animation ourselves
            // true will let the system call it for you, which will result in showing an InfoWindow instantly
            //return false;
        }

        @Override
        public void onDeselect(@NonNull CustomMarkerView marker, @NonNull final View convertView) {

            //

        }

        private static class ViewHolder {
            FrameLayout markerContainer;
            ImageView image;
            ImageView actionBadge;
        }
    }

    private void trackId(int reportId) {

        mappedReports = sharedPreferences.getString("mappedReports", "");

        List<Integer> trackedIds = new ArrayList<Integer>();

        if (!mappedReports.isEmpty()) {

            String[] array = mappedReports.split(",");

            for (String id : array) {

                trackedIds.add(Integer.parseInt(id));

            }

        }

        trackedIds.add(reportId);

        sharedPreferences.edit().putString("mappedReports", TextUtils.join(",", trackedIds)).apply();

    }

    private boolean checkId(int reportId) {

        mappedReports = sharedPreferences.getString("mappedReports", "");

        return Arrays.asList(mappedReports.split(",")).contains(String.valueOf(reportId));

    }

    private void onFetchSuccess(List<Report> reports) {

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

            mMapboxMap.addMarker(options);

            // Store report id for exclusion from future queries
            trackId(report.id);

            mappedReportsHolder.addReport(String.format("%s-%s", report.id, "r"), report);

            Log.d("reportKey", String.format("%s-%s", report.id, "r"));

        }

        // Add origin marker last to ensure that it overlays any others

        Log.d("tracked", checkId(originalPost.id) + "");

        if (!checkId(originalPost.id)) {

            CustomMarkerViewOptions options = new CustomMarkerViewOptions();
            options.position(new LatLng(latitude, longitude));
            options.anchor(0.5f, 0.5f);
            options.flat(true);
            options.reportId(originalPost.id);
            options.thumbNail(originalPost.properties.images.get(0).properties.icon_retina);
            options.fullImage(originalPost.properties.images.get(0).properties.square_retina);
            options.status(originalPost.properties.state);
            options.inFocus(1);

            mMapboxMap.addMarker(options);

            trackId(originalPost.id);

        }

    }

    private void fetchNearbyReports(String polygon, int reportId) {

        QueryFilter idFilter;

        final String accessToken = sharedPreferences.getString("access_token", "");

        Log.d("", accessToken);

        // Retrieve the user id

        int user_id = sharedPreferences.getInt("user_id", 0);

        // Add query filters to retrieve the user's reports
        // Create filters list and add a filter for owner_id

        List<Object> queryFilters = new ArrayList<>();

        QueryFilter geometryFilter = new QueryFilter("geometry", "intersects", polygon);

        // Exclude mapped reports
        mappedReports = sharedPreferences.getString("mappedReports", "");

        if (!mappedReports.isEmpty()) {

            List<Integer> ids = new ArrayList<Integer>();

            // Add original report id to temporary list first and store it more permanently later once
            // the marker has been added to the map - - I wonder if we can instead permanently place
            // the origin marker above all others using z-index or something. Of course the current solution
            // only becomes a problem when there is a large, tightly clustered set of reports.

            ids.add(reportId);

            mappedReports = mappedReports.replaceAll("[\\p{Z}\\s]+", "");

            String[] array = mappedReports.substring(1, mappedReports.length() - 1).split(",");

            for (String id : array) {

                ids.add(Integer.parseInt(id));

            }

            idFilter = new QueryFilter("id", "not_in", ids);

        } else {

            idFilter = new QueryFilter("id", "neq", reportId);

        }

        queryFilters.add(geometryFilter);
        queryFilters.add(idFilter);

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<QuerySort>();

        QuerySort querySort = new QuerySort("created", "desc");

        queryOrder.add(querySort);

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(queryFilters, queryOrder);

        String query = new Gson().toJson(queryParams);

        Log.d("URL", query);

        ReportService service = ReportService.restAdapter.create(ReportService.class);

        service.getReports(accessToken, "application/json", 1, 25, query, new Callback<FeatureCollection>() {

            @Override
            public void success(FeatureCollection featureCollection, Response response) {

                List<Report> reports = featureCollection.getFeatures();

                Log.v("list", reports.toString());

                onFetchSuccess(reports);

            }

            @Override
            public void failure(RetrofitError error) {

                Response response = error.getResponse();

                RetrofitError.Kind r = error.getKind();

                Log.d("HTTP Error:", response.toString());

                Log.d("HTTP Error:", error.getMessage() + r);

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                int status = response.getStatus();

                if (status == 403) {

                    startActivity(new Intent(MapDetailActivity.this, MainActivity.class));

                }

            }

        });

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
        mappedReportsHolder = null;
    }

}
