package com.viableindustries.waterreporter;

import android.os.Bundle;
import android.app.Activity;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import butterknife.Bind;

public class MapDetailActivity extends Activity {

    @Bind(R.id.mapview)
    MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_detail);

        // Mapbox access token only needs to be configured once in your app
        MapboxAccountManager.start(this, getResources().getString(R.string.mapBoxToken));

        // Retrieve location data from intent
//        int reportId = getIntent().getExtras().getInt("REPORT_ID");

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(42.358056, -71.063611))
                        .title("Hello World!")
                        .snippet("Welcome to my marker."));
            }
        });

    }

}
