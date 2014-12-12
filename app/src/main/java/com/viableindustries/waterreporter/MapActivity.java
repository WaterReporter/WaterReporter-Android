package com.viableindustries.waterreporter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Ryan Hamley on 10/14/14.
 * Creates a map for use in selecting a location for your report
 */
public class MapActivity extends ActionBarActivity {
    @InjectView(R.id.mapview) MapView mv;

    private UserLocationOverlay myLocationOverlay;
    private LatLng location;

    protected void setUpMap(){
        SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);
        float latitude = prefs.getFloat("latitude", 0);
        float longitude = prefs.getFloat("longitude", 0);
        LatLng center = new LatLng(latitude, longitude);

        mv.setMinZoomLevel(mv.getTileProvider().getMinimumZoomLevel());
        mv.setMaxZoomLevel(mv.getTileProvider().getMaximumZoomLevel());
        mv.setCenter(center);
        mv.setZoom(14);

        // Adds an icon that shows location
        myLocationOverlay = new UserLocationOverlay(new GpsLocationProvider(this), mv);
        myLocationOverlay.setDrawAccuracyEnabled(true);
        mv.getOverlays().add(myLocationOverlay);

        MapViewListener listener = new MapViewListener() {
            @Override
            public void onShowMarker(MapView mapView, Marker marker) {

            }

            @Override
            public void onHideMarker(MapView mapView, Marker marker) {

            }

            @Override
            public void onTapMarker(MapView mapView, Marker marker) {

            }

            @Override
            public void onLongPressMarker(MapView mapView, Marker marker) {

            }

            @Override
            public void onTapMap(MapView mapView, ILatLng iLatLng) {
                String title = "Report Location";
                String description = "My report was taken at this location";
                location = (LatLng) iLatLng;
                Marker marker = new Marker(mapView, title, description, location);
                marker.setIcon(new Icon(getBaseContext(), Icon.Size.LARGE, "",
                        getString(R.string.waterreporter_green)));
                mapView.clear();
                mapView.addMarker(marker);
            }

            @Override
            public void onLongPressMap(MapView mapView, ILatLng iLatLng) {

            }
        };

        mv.setMapViewListener(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        ButterKnife.inject(this);

        setUpMap();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        myLocationOverlay.enableMyLocation();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        myLocationOverlay.disableMyLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.map, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_save_location){
            Bundle args = new Bundle();
            args.putParcelable("latLng", location);
            Intent intent = new Intent();
            intent.putExtra("bundle", args);
            setResult(RESULT_OK, intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
