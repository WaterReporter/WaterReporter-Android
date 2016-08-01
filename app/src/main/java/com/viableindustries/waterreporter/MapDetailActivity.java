package com.viableindustries.waterreporter;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.viableindustries.waterreporter.data.Geometry;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MapDetailActivity extends Activity {

    @Bind(R.id.mapview)
    MapView mapView;

    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Mapbox access token only needs to be configured once in your app
        MapboxAccountManager.start(this, getResources().getString(R.string.mapBoxToken));

        setContentView(R.layout.activity_map_detail);

        ButterKnife.bind(this);

        // Retrieve report ID
        final int reportId = getIntent().getExtras().getInt("REPORT_ID", 0);

        // Retrieve location data from intent
        latitude = getIntent().getExtras().getDouble("REPORT_LATITUDE", 38.904722);
        longitude = getIntent().getExtras().getDouble("REPORT_LONGITUDE", -77.016389);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(14).build());

                mapboxMap.moveCamera(cameraUpdate);

                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title("Hello World!")
                        .snippet("Welcome to my marker."));

                mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {

                        Intent intent = new Intent(MapDetailActivity.this, MarkerDetailActivity.class);

                        intent.putExtra("REPORT_ID", reportId);

                        startActivity(intent);

                        return true;

                    }

                });
            }
        });

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
////        getMenuInflater().inflate(R.menu.menu_map_detail, menu);
//
//        return true;
//
//    }

    public void getDirections(View view) {

        // Build the intent
        //Uri location = Uri.parse("geo:0,0?q=1600+Amphitheatre+Parkway,+Mountain+View,+California");

        Uri location = Uri.parse(String.format("geo:%s,%s?z=14", latitude, longitude)); // z param is zoom level
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

}
