package com.viableindustries.waterreporter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.util.GeoUtils;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;
import com.viableindustries.waterreporter.data.FeatureCollection;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportService;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.mapview)
    MapView mv;

    static final int REGISTRATION_REQUEST = 1;

    static final int LOGIN_REQUEST = 2;

    protected SharedPreferences prefs = null;

    protected int user_id;

    protected RestAdapter restAdapter = ReportService.restAdapter;

    protected ReportService service = restAdapter.create(ReportService.class);

    // Geographic center of contiguous United States
    protected LatLng defaultCenter = new LatLng(39.828175, -98.5795);

    protected List<LatLng> markers = new ArrayList<LatLng>();

    protected void addMarkers(Report report) {

        //create the marker's LatLng
        Geometry geometry = report.geometry.geometries.get(0);

        LatLng latLng = geometry.getCoordinates();

        markers.add(latLng);

        String issue, color;

        View view;

        CustomMarker m;

        //create an intent which will open the detailed view when the marker's info button is clicked
        //include the report id so that the detailed view can get the information for the report
        final Intent intent = new Intent(this, MarkerDetailActivity.class);

        intent.putExtra("REPORT_ID", report.properties.id);

        //only put markers for Point geometry on the map
        if (geometry.type.equals("Point")) {

            color = getString(R.string.base_blue);

            String title = report.properties.owner.properties.first_name + " " +
                    report.properties.owner.properties.last_name + " \u00B7 " +
                    report.properties.getFormattedDateString();

            m = new CustomMarker(mv, title, report.properties.report_description + " ....", latLng);

            m.setIcon(new Icon(this, Icon.Size.LARGE, "", color));
            //get the InfoWindow's view so that you can set a touch listener which will switch
            //to the marker's detailed view when clicked
            final InfoWindow infoWindow = m.getToolTip(mv);
            view = infoWindow.getView();
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    //make sure to choose action down or up, otherwise the intent will launch twice
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        startActivity(intent);
                        //close the InfoWindow so it's not still open when coming back to the map
                        infoWindow.close();
                    }
                    return true;
                }
            });

            mv.addMarker(m);

        }

//        setBounds();

    }

    protected void setUpMap() {

        mv.setCenter(defaultCenter);

        mv.setMinZoomLevel(mv.getTileProvider().getMinimumZoomLevel());

        mv.setMaxZoomLevel(mv.getTileProvider().getMaximumZoomLevel());

        // There are reports from across the nation now, so let's start way out before zooming to
        // bounding box
        mv.setZoom(4);

    }

    protected void setBounds() {

        BoundingBox box = GeoUtils.findBoundingBoxForGivenLocations(markers, 0.5);

        mv.zoomToBoundingBox(box, true, true);

    }

    protected void requestData() {

        SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        Log.d("", access_token);

        // We shouldn't need to retrieve this value again, but we'll deal with that issue later
        user_id = prefs.getInt("user_id", 0);

        String query = "{\"filters\":[{\"name\":\"owner_id\",\"op\":\"eq\",\"val\":" + user_id + "}],\"order_by\":[{\"field\":\"created\",\"direction\":\"desc\"}]}";

        //String query = "{\"filters\":[],\"order_by\":[{\"field\":\"created\",\"direction\":\"desc\"}]}";

        service.getReports(access_token, "application/json", 500, query, new Callback<FeatureCollection>() {

            @Override
            public void success(FeatureCollection featureCollection, Response response) {

                List<Report> reports = featureCollection.getFeatures();

                Log.v("list", reports.toString());

                if (!reports.isEmpty()) {

                    for (Report report : reports) {

                        addMarkers(report);

                    }

                    setBounds();

                }

            }

            @Override
            public void failure(RetrofitError error) {

                Response response = error.getResponse();

                int status = response.getStatus();

                if (status == 403) {

                    startActivity(new Intent(MainActivity.this, SignInActivity.class));

                }

            }

        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        setUpMap();

        // Not sure we should attempt a call unless we pass all checks first
        // Obviously if we're going to redirect then we need to ensure that user id
        // and token are present. If we decide not to redirect or prompt (which we should)
        // then this becomes less of a problem.
        //requestData();

    }

    @Override
    protected void onResume() {
        super.onResume();

        String access_token = prefs.getString("access_token", "");

        user_id = prefs.getInt("user_id", 0);

        if (user_id == 0) {

            startActivityForResult(new Intent(this, RegistrationForkActivity.class), REGISTRATION_REQUEST);

        } else if (access_token.equals("")) {

            startActivityForResult(new Intent(this, SignInActivity.class), LOGIN_REQUEST);

        } else {

            requestData();

        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        // Disable for now
        // myLocationOverlay.disableMyLocation();

        // locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ButterKnife.unbind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_submissions) {

            startActivity(new Intent(this, SubmissionsActivity.class));

        }

        if (id == R.id.action_report) {

            startActivity(new Intent(this, PhotoActivity.class));

        }

        return true;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REGISTRATION_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                // Since the user just registered, direct them to create a new report
                startActivity(new Intent(this, PhotoActivity.class));

            }

        } else if (requestCode == LOGIN_REQUEST) {

            if (resultCode == RESULT_OK) {

                // The user is logged in and may already have reports in the system.
                // Let's attempt to fetch the user's report collection and, if none exist,
                // direct the user to submit their first report.
                requestData();

            }

        }

    }

}
