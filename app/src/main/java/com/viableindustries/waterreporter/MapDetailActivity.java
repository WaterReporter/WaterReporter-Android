package com.viableindustries.waterreporter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MapDetailActivity extends AppCompatActivity {

    @Bind(R.id.mapview)
    MapView mapView;

    private MapboxMap mMapboxMap;

    private double latitude;
    private double longitude;

    private static int reportId;
    private static String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Mapbox access token only needs to be configured once in your app
        MapboxAccountManager.start(this, getResources().getString(R.string.mapBoxToken));

        setContentView(R.layout.activity_map_detail);

        ButterKnife.bind(this);

        // Retrieve report ID
        reportId = getIntent().getExtras().getInt("REPORT_ID", 0);

        // Retrieve image icon URL
        imageUrl = getIntent().getExtras().getString("IMAGE_URL", "");

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
                        //.bearing(180) // Rotate the camera
                        //.tilt(30) // Set the camera tilt
                        .build(); // Creates a CameraPosition from the builder

                //CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(14).build());

                mapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position), 5000);

                //mapboxMap.moveCamera(cameraUpdate);

                // Create an Icon object for the marker to use
                //IconFactory iconFactory = IconFactory.getInstance(MapDetailActivity.this);
                //Drawable iconDrawable = ContextCompat.getDrawable(MapDetailActivity.this, R.drawable.purple_marker);
                //Icon icon = iconFactory.fromDrawable(iconDrawable);

//                mapboxMap.addMarker(new MarkerOptions()
//                        .position(new LatLng(latitude, longitude)));

                // add custom ViewMarker
                CustomMarkerViewOptions options = new CustomMarkerViewOptions();
                options.position(new LatLng(latitude, longitude));
                options.flat(true);
                options.imageUrl(imageUrl);
                options.reportId(reportId);
                options.anchor(0.5f, 0.5f);
                mapboxMap.addMarker(options);

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

//                        LatLng[] latLngs = latLngBounds.toLatLngs();
//
//                        for (LatLng latLng : latLngs) {
//
//                            Log.d("point", latLng.toString());
//
//                        }

//                        Log.d("northEast", latLngBounds.toLatLngs().toString());

//                        LatLngBounds latLngBounds = new LatLngBounds.Builder()
//                                .include(new LatLng(36.532128, -93.489121)) // Northeast
//                                .include(new LatLng(25.837058, -106.646234)) // Southwest
//                                .build();

                        Log.d("mapPosition", position.toString());

                    }
                });

//                mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
//                    @Override
//                    public boolean onMarkerClick(@NonNull Marker marker) {
//
//                        Intent intent = new Intent(MapDetailActivity.this, MarkerDetailActivity.class);
//
//                        intent.putExtra("REPORT_ID", reportId);
//
//                        startActivity(intent);
//
//                        return true;
//
//                    }
//
//                });
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
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Picasso.with(getContext()).load(marker.getImageUrl()).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(viewHolder.image);

            return convertView;
        }

        @Override
        public boolean onSelect(@NonNull final CustomMarkerView marker, @NonNull final View convertView, boolean reselectionForViewReuse) {

            Log.d("anchor", String.format("anchorU %s", marker.getAnchorU()));
            Log.d("anchor", String.format("anchorV %s", marker.getAnchorV()));

            Context context = getContext();

            Activity activity = (Activity) context;

            Intent intent = new Intent(activity, MarkerDetailActivity.class);

            intent.putExtra("REPORT_ID", marker.getReportId());

            activity.startActivity(intent);

            //activity.finish();

//            context.

            return true;

            //mapboxMap.selectMarker(marker);

//            convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//            ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(convertView, View.ROTATION, 0, 360);
//            rotateAnimator.setDuration(reselectionForViewReuse ? 0 : 350);
//            rotateAnimator.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    super.onAnimationEnd(animation);
//                    convertView.setLayerType(View.LAYER_TYPE_NONE, null);
//                    mapboxMap.selectMarker(marker);
//                }
//            });
//            rotateAnimator.start();

            // false indicates that we are calling selectMarker after our animation ourselves
            // true will let the system call it for you, which will result in showing an InfoWindow instantly
            //return false;
        }

        @Override
        public void onDeselect(@NonNull CustomMarkerView marker, @NonNull final View convertView) {
            convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(convertView, View.ROTATION, 360, 0);
            rotateAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    convertView.setLayerType(View.LAYER_TYPE_NONE, null);
                }
            });
            //rotateAnimator.start();
        }

        private static class ViewHolder {
            ImageView image;
        }
    }

    private void onFetchSuccess(List<Report> reports) {

        for (Report report : reports) {

            Geometry geometry = report.geometry.geometries.get(0);

            CustomMarkerViewOptions options = new CustomMarkerViewOptions();
            options.position(new LatLng(geometry.coordinates.get(1), geometry.coordinates.get(0)));
            options.flat(true);
            options.imageUrl(report.properties.images.get(0).properties.icon_retina);
            options.reportId(report.id);
            options.anchor(0.5f, 0.5f);
            mMapboxMap.addMarker(options);

        }

    }

    private void fetchNearbyReports(String polygon, int reportId) {

        // Retrieve feature IDs from the local database and use them in a

        SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        Log.d("", access_token);

        // Retrieve the user id

        int user_id = prefs.getInt("user_id", 0);

        // Add query filters to retrieve the user's reports
        // Create filters list and add a filter for owner_id

        List<QueryFilter> queryFilters = new ArrayList<QueryFilter>();

        QueryFilter geometryFilter = new QueryFilter("geometry", "intersects", polygon);

        QueryFilter idFilter = new QueryFilter("id", "neq", reportId);

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
