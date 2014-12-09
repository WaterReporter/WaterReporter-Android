package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.GpsLocationProvider;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.UserLocationOverlay;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;
import com.viableindustries.waterreporter.data.CommonsCloudResponse;
import com.viableindustries.waterreporter.data.CommonsCloudService;
import com.viableindustries.waterreporter.data.Geometries;
import com.viableindustries.waterreporter.data.Report;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends ActionBarActivity {
    @InjectView(R.id.mapview) MapView mv;

    private SharedPreferences prefs = null;
    private UserLocationOverlay myLocationOverlay;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private RestAdapter restAdapter = CommonsCloudService.restAdapter;
    private CommonsCloudService service = restAdapter.create(CommonsCloudService.class);

    protected void setMapCenterByUserLocation() {
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                float latitude = (float) location.getLatitude();
                float longitude = (float) location.getLongitude();
                prefs.edit().putFloat("latitude", latitude).putFloat("longitude", longitude).apply();

                mv.setCenter(new LatLng(location));

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                0, 100, locationListener);
    }

    protected void addMarkers(Report report){
        //create the marker's LatLng
        Geometries geometries = report.geometry.geometries.get(0);
        LatLng latLng = geometries.getCoordinates();
        if(report.pollution.get(0).name == null){
            report.pollution.get(0).name = "Unknown issue type";
        }

        //create an intent which will open the detailed view when the marker's info button is clicked
        //include the report id so that the detailed view can get the information for the report
        final Intent intent = new Intent(this, MarkerDetailActivity.class);
        intent.putExtra("REPORT_ID", report.id);

        //only put markers for Point geometries on the map
        if(geometries.type.equals("Point")){
            String issue = report.pollution.get(0).name;
            String color;

            if(report.isPollution){
                color = getString(R.string.waterreporter_green);
            } else {
                color = getString(R.string.waterreporter_orange);
            }

            CustomMarker m =
                    new CustomMarker(mv, issue, report.getFormattedDateString(), latLng);
            m.setIcon(new Icon(this, Icon.Size.LARGE, "", color));
            //get the InfoWindow's view so that you can set a touch listener which will switch
            //to the marker's detailed view when clicked
            final InfoWindow infoWindow = m.getToolTip(mv);
            View view = infoWindow.getView();
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    //make sure to choose action down or up, otherwise the intent will launch twice
                    if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                        startActivity(intent);
                        //close the InfoWindow so it's not still open when coming back to the map
                        infoWindow.close();
                    }
                    return true;
                }
            });
            mv.addMarker(m);
        }
    }

    protected void setUpMap(){
        mv.setMinZoomLevel(mv.getTileProvider().getMinimumZoomLevel());
        mv.setMaxZoomLevel(mv.getTileProvider().getMaximumZoomLevel());
        mv.setZoom(11);
        setMapCenterByUserLocation();

        // Adds an icon that shows location
        myLocationOverlay = new UserLocationOverlay(new GpsLocationProvider(this), mv);
        myLocationOverlay.setDrawAccuracyEnabled(true);
        mv.getOverlays().add(myLocationOverlay);
    }

    protected void requestData(){
        service.getReports(new Callback<CommonsCloudResponse>() {
            @Override
            public void success(CommonsCloudResponse commonsCloudResponse, Response response) {
                List<Report> reports = commonsCloudResponse.featuresResponse.features;
                for(Report report : reports){
                    addMarkers(report);
                }
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        setUpMap();

        requestData();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        String user_email = prefs.getString("user_email", "");

        if(user_email.equals("")){
            startActivity(new Intent(this, SignInActivity.class));
        }

        myLocationOverlay.enableMyLocation();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        myLocationOverlay.disableMyLocation();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ButterKnife.reset(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if(id == R.id.action_submissions){
            startActivity(new Intent(this, SubmissionsActivity.class));
        }
        if(id == R.id.action_report){
            startActivity(new Intent(this, ReportActivity.class));
        }
        return true;
    }
}
