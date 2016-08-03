package com.viableindustries.waterreporter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
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
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.mbox.CustomMarkerView;
import com.viableindustries.waterreporter.mbox.CustomMarkerViewOptions;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MapDetailActivity extends AppCompatActivity {

    @Bind(R.id.mapview)
    MapView mapView;

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
            public void onMapReady(MapboxMap mapboxMap) {

                final MarkerViewManager markerViewManager = mapboxMap.getMarkerViewManager();

                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(14).build());

                mapboxMap.moveCamera(cameraUpdate);

                // Create an Icon object for the marker to use
                //IconFactory iconFactory = IconFactory.getInstance(MapDetailActivity.this);
                //Drawable iconDrawable = ContextCompat.getDrawable(MapDetailActivity.this, R.drawable.purple_marker);
                //Icon icon = iconFactory.fromDrawable(iconDrawable);

//                mapboxMap.addMarker(new MarkerOptions()
//                        .position(new LatLng(latitude, longitude)));

//                float anchorU = 0;
//                float anchorV = 0;

                // add custom ViewMarker
                CustomMarkerViewOptions options = new CustomMarkerViewOptions();
                options.position(new LatLng(latitude, longitude));
                options.flat(true);
                options.imageUrl(imageUrl);
                options.anchor(0.5f, 0.5f);
                mapboxMap.addMarker(options);

                // if you want to customise a ViewMarker you need to extend ViewMarker and provide an adapter implementation
                // set adapters for child classes of ViewMarker
                markerViewManager.addMarkerViewAdapter(new MarkerAdapter(MapDetailActivity.this, mapboxMap));

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

            Intent intent = new Intent(context, MarkerDetailActivity.class);

            intent.putExtra("REPORT_ID", reportId);

            context.startActivity(intent);

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

}
