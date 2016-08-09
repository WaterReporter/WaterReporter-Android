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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
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
import com.viableindustries.waterreporter.data.QueryFilter;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.mbox.CustomMarkerView;
import com.viableindustries.waterreporter.mbox.CustomMarkerViewOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Mapbox access token only needs to be configured once in your app
        MapboxAccountManager.start(this, getResources().getString(R.string.mapBoxToken));

        setContentView(R.layout.activity_map_detail);

        ButterKnife.bind(this);

        // Set a reference to SharedPreferences
        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        // Reset stored list of mapped reports
        sharedPreferences.edit().putString("mappedReports", "").apply();

        // Retrieve report attributes
        reportId = getIntent().getExtras().getInt("REPORT_ID", 0);
        reportDescription = getIntent().getExtras().getString("REPORT_DESCRIPTION", "");
        thumbNail = getIntent().getExtras().getString("THUMBNAIL_URL", "");
        fullImage = getIntent().getExtras().getString("FULL_IMAGE_URL", "");
        creationDate = getIntent().getExtras().getString("REPORT_CREATED", "");
        watershedName = getIntent().getExtras().getString("REPORT_WATERSHED", "");
        groupList = getIntent().getExtras().getString("REPORT_GROUPS", "");
        commentCount = getIntent().getExtras().getString("COMMENT_COUNT", "");
        userName = getIntent().getExtras().getString("USER_NAME", "");
        userAvatar = getIntent().getExtras().getString("USER_AVATAR", "");
        status = getIntent().getExtras().getString("STATUS", "");

        // Retrieve location data from intent
        latitude = getIntent().getExtras().getDouble("REPORT_LATITUDE", 38.904722);
        longitude = getIntent().getExtras().getDouble("REPORT_LONGITUDE", -77.016389);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {

                mMapboxMap = mapboxMap;

                final MarkerViewManager markerViewManager = mapboxMap.getMarkerViewManager();

                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(latitude, longitude)) // Sets the new camera position
                        .zoom(14) // Sets the zoom
                        .build(); // Creates a CameraPosition from the builder

                mapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position), 4000);

//                mapboxMap.addMarker(new MarkerOptions()
//                        .position(new LatLng(latitude, longitude)));

                // Create an Icon object for the marker to use
                IconFactory iconFactory = IconFactory.getInstance(MapDetailActivity.this);
                Drawable iconDrawable = ContextCompat.getDrawable(MapDetailActivity.this, R.drawable.anchor_marker);
                Icon icon = iconFactory.fromDrawable(iconDrawable);

                // Add the custom icon marker to the map
//                mapboxMap.addMarker(new MarkerOptions()
//                        .position(new LatLng(latitude, longitude))
//                        .title("Cape Town Harbour")
//                        .snippet("One of the busiest ports in South Africa")
//                        .icon(icon));

                // add custom ViewMarker
                CustomMarkerViewOptions options = new CustomMarkerViewOptions();
                options.position(new LatLng(latitude, longitude));
                options.flat(true);
                options.reportId(reportId);
                options.reportDescription(reportDescription);
                options.thumbNail(thumbNail);
                options.fullImage(fullImage);
                options.creationDate(creationDate);
                options.watershedName(watershedName);
                options.groupList(groupList);
                options.commentCount(commentCount);
                options.userName(userName);
                options.userAvatar(userAvatar);
                options.status(status);
                options.inFocus(1);
                //options.anchor(0.5f, 0.5f);
                mapboxMap.addMarker(options);

                trackId(reportId);

//                mapboxMap.addMarker(new MarkerOptions()
//                        .position(new LatLng(latitude, longitude)));

                // if you want to customise a ViewMarker you need to extend ViewMarker and provide an adapter implementation
                // set adapters for child classes of ViewMarker
                markerViewManager.addMarkerViewAdapter(new MarkerAdapter(MapDetailActivity.this, mapboxMap));

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

                            fetchNearbyReports(polygon, reportId);

                        }

                        Log.d("mapPosition", position.toString());

                    }
                });
            }
        });

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
    }

    public void getDirections(View view) {

        // Build the intent
        //Uri location = Uri.parse("geo:0,0?q=1600+Amphitheatre+Parkway,+Mountain+View,+California");

        //Uri location = Uri.parse(String.format("geo:%s,%s?z=14", latitude, longitude)); // z param is zoom level

        Uri location = Uri.parse(String.format("google.navigation:q=%s,%s", latitude, longitude));

        //Uri location = Uri.parse("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345"));

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);

        // Verify it resolves
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
        boolean isIntentSafe = activities.size() > 0;

        // Start an activity if it's safe
        if (isIntentSafe) {
            startActivity(mapIntent);
        }

    }

    // Adapter for our custom marker view

    private static class MarkerAdapter extends MapboxMap.MarkerViewAdapter<CustomMarkerView> {

        private LayoutInflater inflater;
        private MapboxMap mapboxMap;

        public MarkerAdapter(@NonNull Context context, @NonNull MapboxMap mapboxMap) {
            super(context);
            this.inflater = LayoutInflater.from(context);
            this.mapboxMap = mapboxMap;
        }

        @Nullable
        @Override
        public View getView(@NonNull CustomMarkerView marker, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.view_custom_marker, parent, false);
                viewHolder.image = (ImageView) convertView.findViewById(R.id.imageView);
                viewHolder.actionBadge = (ImageView) convertView.findViewById(R.id.actionBadge);
                viewHolder.markerPin = (ImageView) convertView.findViewById(R.id.markerPin);
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

            // Display active marker pin if report is "source"
            if (marker.isInFocus() == 1) {

                //viewHolder.actionBadge.setVisibility(View.VISIBLE);
                //viewHolder.markerPin.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.active_marker_pin));

                //viewHolder.markerPin.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.active_marker_pin));

                viewHolder.markerPin.setBackgroundResource(R.drawable.active_marker_pin);

                viewHolder.image.setBackgroundResource(R.drawable.active_marker_border);

            } else {

                //viewHolder.actionBadge.setVisibility(View.GONE);
                //viewHolder.markerPin.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.default_marker_pin));

                viewHolder.markerPin.setBackgroundResource(R.drawable.default_marker_pin);

                viewHolder.image.setBackgroundResource(R.drawable.marker_icon_border);

            }

            Picasso.with(getContext()).load(marker.getThumbNail()).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(viewHolder.image);

            return convertView;
        }

        @Override
        public boolean onSelect(@NonNull final CustomMarkerView marker, @NonNull final View convertView, boolean reselectionForViewReuse) {

            //ImageView image = (ImageView) convertView.findViewById(R.id.imageView);

            //image.setBackgroundResource(R.drawable.active_marker_border);

            Log.d("anchor", String.format("anchorU %s", marker.getAnchorU()));
            Log.d("anchor", String.format("anchorV %s", marker.getAnchorV()));

            CameraPosition position = new CameraPosition.Builder()
                    .target(marker.getPosition()) // Sets the new camera position
                    //.zoom(14) // Sets the zoom
                    .build(); // Creates a CameraPosition from the builder

            mapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 1000);

            // Build new MarkerDetailFragment
            MarkerDetailFragment markerDetailFragment = new MarkerDetailFragment();

            Bundle markerAttrs = new Bundle();

            markerAttrs.putInt("reportId", marker.getReportId());
            markerAttrs.putString("reportDescription", marker.getReportDescription());
            markerAttrs.putString("thumbNail", marker.getThumbNail());
            markerAttrs.putString("fullImage", marker.getFullImage());
            markerAttrs.putString("creationDate", marker.getCreationDate());
            markerAttrs.putString("watershedName", marker.getWatershedName());
            markerAttrs.putString("groupList", marker.getGroupList());
            markerAttrs.putString("commentCount", marker.getCommentCount());
            markerAttrs.putString("userName", marker.getUserName());
            markerAttrs.putString("userAvatar", marker.getUserAvatar());
            markerAttrs.putString("status", marker.getStatus());
            markerAttrs.putDouble("latitude", marker.getPosition().getLatitude());
            markerAttrs.putDouble("longitude", marker.getPosition().getLongitude());

            markerDetailFragment.setArguments(markerAttrs);

            FragmentTransaction fragmentTransaction = ((FragmentActivity)getContext()).getFragmentManager().beginTransaction();

            fragmentTransaction.replace(R.id.map_detail, markerDetailFragment);
            fragmentTransaction.addToBackStack(null);

            fragmentTransaction.commit();

            return false;

            // false indicates that we are calling selectMarker after our animation ourselves
            // true will let the system call it for you, which will result in showing an InfoWindow instantly
            //return false;
        }

        @Override
        public void onDeselect(@NonNull CustomMarkerView marker, @NonNull final View convertView) {

            //ImageView image = (ImageView) convertView.findViewById(R.id.imageView);

            //image.setBackgroundResource(R.drawable.marker_icon_border);

            //convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            //ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(convertView, View.ROTATION, 360, 0);
            //rotateAnimator.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    super.onAnimationEnd(animation);
//                    convertView.setLayerType(View.LAYER_TYPE_NONE, null);
//                }
//            });
            //rotateAnimator.start();
        }

        private static class ViewHolder {
            ImageView image;
            ImageView actionBadge;
            ImageView markerPin;
        }
    }

    private void trackId(int reportId) {

        mappedReports = sharedPreferences.getString("mappedReports", "");

        List<Integer> ids = new ArrayList<Integer>();

        if (!mappedReports.isEmpty()) {

            mappedReports = mappedReports.replaceAll("[\\p{Z}\\s]+", "");

            String[] array = mappedReports.substring(1, mappedReports.length() - 1).split(",");

            for (String id : array) {

                ids.add(Integer.parseInt(id));

            }

        }

        ids.add(reportId);

        sharedPreferences.edit().putString("mappedReports", ids.toString()).apply();

    }

    private void onFetchSuccess(List<Report> reports) {

        for (Report report : reports) {

            Geometry geometry = report.geometry.geometries.get(0);

            CustomMarkerViewOptions options = new CustomMarkerViewOptions();
            options.position(new LatLng(geometry.coordinates.get(1), geometry.coordinates.get(0)));
            options.flat(true);
            options.reportId(report.id);
            options.reportDescription(report.properties.report_description.trim());
            options.thumbNail(report.properties.images.get(0).properties.icon_retina);
            options.fullImage(report.properties.images.get(0).properties.square_retina);
            options.creationDate(AttributeTransformUtility.parseDate(new SimpleDateFormat("MMM dd, yyyy", Locale.US), report.properties.created));
            options.watershedName(AttributeTransformUtility.parseWatershedName(report.properties.territory));
            options.groupList(AttributeTransformUtility.groupListSize(report.properties.groups));
            options.commentCount(AttributeTransformUtility.countComments(report.properties.comments));
            options.userName(String.format("%s %s", report.properties.owner.properties.first_name, report.properties.owner.properties.last_name));
            options.userAvatar(report.properties.owner.properties.picture);
            options.status(report.properties.state);
            options.inFocus(0);
            //options.anchor(0.5f, 0.5f);
            mMapboxMap.addMarker(options);

            // Store report id for exclusion from future queries
            trackId(report.id);

        }

    }

    private void fetchNearbyReports(String polygon, int reportId) {

        QueryFilter idFilter;

        final String access_token = sharedPreferences.getString("access_token", "");

        Log.d("", access_token);

        // Retrieve the user id

        int user_id = sharedPreferences.getInt("user_id", 0);

        // Add query filters to retrieve the user's reports
        // Create filters list and add a filter for owner_id

        List<QueryFilter> queryFilters = new ArrayList<QueryFilter>();

        QueryFilter geometryFilter = new QueryFilter("geometry", "intersects", polygon);

        // Exclude mapped reports
        mappedReports = sharedPreferences.getString("mappedReports", "");

        if (!mappedReports.isEmpty()) {

            List<Integer> ids = new ArrayList<Integer>();

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

        service.getReports(access_token, "application/json", 1, 25, query, new Callback<FeatureCollection>() {

            @Override
            public void success(FeatureCollection featureCollection, Response response) {

                List<Report> reports = featureCollection.getFeatures();

                Log.v("list", reports.toString());

                if (!reports.isEmpty()) {

                    onFetchSuccess(reports);

                }

            }

            @Override
            public void failure(RetrofitError error) {

                Response response = error.getResponse();

                RetrofitError.Kind r = error.getKind();

                Log.d("HTTP Error:", response.toString());

                Log.d("HTTP Error:", error.getMessage() + r);

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (response != null) {

                    int status = response.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(MapDetailActivity.this, MainActivity.class));

                    }

                }

            }

        });

    }

}
