package com.viableindustries.waterreporter;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.GeometryResponse;
import com.viableindustries.waterreporter.data.ImageProperties;
import com.viableindustries.waterreporter.data.ImageService;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportPostBody;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.Submission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

/**
 * Created by Ryan Hamley on 10/28/14.
 * This activity handles all of the report functionality.
 */
public class PhotoMetaActivity extends AppCompatActivity {

    @Bind(R.id.save_message)
    RelativeLayout saveMessage;

    @Bind(R.id.save_status)
    TextView saveStatus;

    @Bind(R.id.loading_spinner)
    ProgressBar progressBar;

    @Bind(R.id.date)
    EditText dateField;

    @Bind(R.id.comments)
    EditText commentsField;

    @Bind(R.id.preview)
    ImageView mImageView;

    @Bind(R.id.static_map)
    ImageView staticMap;

    @Bind(R.id.location_button)
    Button locationButton;

    private static final int ACTION_SET_LOCATION = 0;

    private String mGalleryPath;

    private String mTempImagePath;

    private int mImageId;

    private LatLng location;

    private String dateText;

    private String commentsText;

    protected double latitude;

    protected double longitude;

    // Check for a data connection!

    protected boolean connectionActive() {

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_metadata);

        ButterKnife.bind(this);

        progressBar.getIndeterminateDrawable().setColorFilter(
                getResources().getColor(R.color.base_blue),
                android.graphics.PorterDuff.Mode.SRC_IN);

        if (!connectionActive()) {

            CharSequence text = "Looks like you're not connected to the internet, so we couldn't submit your report. Please connect to a network and try again.";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(getBaseContext(), text, duration);
            toast.show();

            startActivity(new Intent(this, MainActivity.class));

            return;

        }

        initializeDateField();

        Bundle extras = getIntent().getExtras();

        if (extras != null) {

            mTempImagePath = extras.getString("image_path", "");

            mGalleryPath = extras.getString("gallery_path", "");

            Log.d("galleryPath", mGalleryPath);

            mImageId = extras.getInt("image_id", 0);

            Log.d("image_path", mTempImagePath);

            Log.d("image_id", mImageId + "");

            Picasso.with(this)
                    .load(new File(mTempImagePath))
                    .placeholder(R.drawable.square_placeholder)
                    .into(mImageView);

            mImageView.setVisibility(View.VISIBLE);

        }

        if (savedInstanceState != null) {

            mTempImagePath = savedInstanceState.getString("image_path", "");

            mGalleryPath = savedInstanceState.getString("gallery_path", "");

            Log.d("galleryPath", mGalleryPath);

            mImageId = savedInstanceState.getInt("image_id", 0);

            Log.d("image_path", mTempImagePath);

            Log.d("image_id", mImageId + "");

            Picasso.with(this)
                    .load(new File(mTempImagePath))
                    .placeholder(R.drawable.square_placeholder)
                    .into(mImageView);

            mImageView.setVisibility(View.VISIBLE);

            dateField.setText(savedInstanceState.getString("report_date"));

            commentsField.setText(savedInstanceState.getString("report_description"));

            latitude = savedInstanceState.getDouble("latitude", 0);

            longitude = savedInstanceState.getDouble("longitude", 0);

            if (longitude > 0 && latitude > 0) {

                staticMap.setVisibility(View.VISIBLE);

                String url = "http://api.tiles.mapbox.com/v4/bmcintyre.ibo4mn2f/" + "pin-m+0094d6(" + longitude + "," + latitude + ",14)/" + longitude + ',' + latitude + ",14/640x640@2x.png?access_token=pk.eyJ1IjoiYm1jaW50eXJlIiwiYSI6IjdST3dWNVEifQ.ACCd6caINa_d4EdEZB_dJw";

                Picasso.with(this)
                        .load(url)
                                //.placeholder(R.drawable.square_placeholder)
                        .fit()
                        .centerCrop()
                        .into(staticMap);

                locationButton.setText("Edit location");

            }

            mImageId = savedInstanceState.getInt("image_id", 0);

        }

        // Add click listener to date field (EditText)

        dateField.setInputType(InputType.TYPE_NULL);

        dateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });

        dateField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDatePickerDialog(v);
                }
            }
        });

    }

    protected void initializeDateField() {

        UtilityMethods utilityMethods = new UtilityMethods();

        int day = utilityMethods.getCurrentDay();
        int month = utilityMethods.getCurrentMonth();
        int year = utilityMethods.getCurrentYear();

        dateField.setText(utilityMethods.getDateString(month, day, year));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case ACTION_SET_LOCATION:

                if (resultCode == RESULT_OK) {

                    Bundle bundle = data.getParcelableExtra("bundle");

                    location = bundle.getParcelable("latLng");

                    if (location != null) {

                        Log.d("location", location.getLatitude() + "");

                        staticMap.setVisibility(View.VISIBLE);

                        longitude = location.getLongitude();

                        latitude = location.getLatitude();

                        Log.d("position", longitude + latitude + "");

                        String url = "http://api.tiles.mapbox.com/v4/bmcintyre.ibo4mn2f/" + "pin-m+0094d6(" + longitude + "," + latitude + ",14)/" + longitude + ',' + latitude + ",14/640x640@2x.png?access_token=pk.eyJ1IjoiYm1jaW50eXJlIiwiYSI6IjdST3dWNVEifQ.ACCd6caINa_d4EdEZB_dJw";

                        Log.d("url", url);

                        Picasso.with(this)
                                .load(url)
                                        //.placeholder(R.drawable.square_placeholder)
                                .fit()
                                .centerCrop()
                                .into(staticMap);

                        locationButton.setText("Edit location");

                        CharSequence text = "Location saved successfully";

                        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);

                        toast.show();

                    }

                }

                break;

        }

    }

    /**
     * onClick event to launch a map from Report view
     **/
    public void updateLocation(View v) {

        startActivityForResult(new Intent(this, LocationActivity.class), ACTION_SET_LOCATION);

    }

    /**
     * onClick event to launch a date picker from Report view
     **/
    public void showDatePickerDialog(View v) {

        DialogFragment newFragment = new DatePickerFragment();

        FragmentManager fragmentManager = getFragmentManager();

        newFragment.show(fragmentManager, "datePicker");

    }

    protected void onPostError() {

        saveMessage.setVisibility(View.GONE);

        CharSequence text =
                "Error posting report. Please try again later.";

        Toast toast = Toast.makeText(getBaseContext(), text,
                Toast.LENGTH_SHORT);

        toast.show();

    }

    // Send POST request

    protected void submitReport() {

        if (!connectionActive()) {

            CharSequence text = "Looks like you're not connected to the internet, so we couldn't capture your report. Please connect to a network and try again.";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(getBaseContext(), text, duration);
            toast.show();

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

        saveMessage.setVisibility(View.VISIBLE);

        final ReportService reportService = ReportService.restAdapter.create(ReportService.class);

        final ImageService imageService = ImageService.restAdapter.create(ImageService.class);

        SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        ArrayList<Float> coordinates = new ArrayList<Float>(2);

        String point = "Point";

        String type = "GeometryCollection";

        List<Geometry> geometryList = new ArrayList<Geometry>(1);

        FileNameMap fileNameMap = URLConnection.getFileNameMap();

        coordinates.clear();
        geometryList.clear();

        coordinates.add((float) longitude);
        coordinates.add((float) latitude);
        Geometry geometry = new Geometry(coordinates, point);
        geometryList.add(geometry);

        final GeometryResponse geometryResponse = new GeometryResponse(geometryList, type);

        Log.d("filepath", mTempImagePath);

        File photo = new File(mTempImagePath);

        String mimeType = fileNameMap.getContentTypeFor(mTempImagePath);

        TypedFile typedPhoto = new TypedFile(mimeType, photo);

        imageService.postImage(access_token, typedPhoto,
                new Callback<ImageProperties>() {
                    @Override
                    public void success(ImageProperties imageProperties,
                                        Response response) {

                        // Retrieve the image id and create a new report

                        final Map<String, Integer> image_id = new HashMap<String, Integer>();

                        image_id.put("id", imageProperties.id);

                        List<Map<String, Integer>> images = new ArrayList<Map<String, Integer>>();

                        images.add(image_id);

                        ReportPostBody reportPostBody = new ReportPostBody(geometryResponse,
                                images, true, dateText, commentsText, "public");

                        reportService.postReport(access_token, "application/json", reportPostBody,
                                new Callback<Report>() {
                                    @Override
                                    public void success(Report report,
                                                        Response response) {

                                        // Immediately delete the cached image file now that we no longer need it

                                        File tempFile = new File(mTempImagePath);

                                        boolean imageDeleted = tempFile.delete();

                                        Log.w("Delete Check", "File deleted: " + tempFile + imageDeleted);

                                        progressBar.setVisibility(View.GONE);

                                        saveStatus.setText(getResources().getString(R.string.report_received));

                                        final Handler handler = new Handler();

                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                startActivity(new Intent(PhotoMetaActivity.this, SubmissionsActivity.class));

                                            }

                                        }, 2000);

                                    }

                                    @Override
                                    public void failure(RetrofitError error) {
                                        onPostError();
                                    }

                                });

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onPostError();
                    }

                });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("report_date", dateText);

        outState.putString("report_description", commentsText);

        outState.putDouble("latitude", latitude);

        outState.putDouble("latitude", longitude);

        outState.putString("image_path", mTempImagePath);

        outState.putString("gallery_path", mGalleryPath);

        outState.putInt("image_id", mImageId);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_save) {

            dateText = String.valueOf(dateField.getText());

            commentsText = String.valueOf(commentsField.getText());

            // Step through comment and location checks and warn the user if anything's missing.

            if (commentsText.isEmpty()) {

                CharSequence text = "Please add a comment.";
                Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
                toast.show();

                return false;

            }

            if (location != null) {

                latitude = location.getLatitude();

                longitude = location.getLongitude();

                if (latitude == 0 || longitude == 0) {

                    CharSequence text = "Please verify your location.";
                    Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
                    toast.show();

                    return false;

                }

            } else {

                SharedPreferences prefs =
                        getSharedPreferences(getPackageName(), MODE_PRIVATE);

                latitude = prefs.getFloat("latitude", 0);

                longitude = prefs.getFloat("longitude", 0);

                if (latitude == 0 || longitude == 0) {

                    CharSequence text = "Please verify your location.";
                    Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
                    toast.show();

                    return false;

                }

            }

            submitReport();

            return true;

        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onResume() {

        super.onResume();

        // Check for a data connection!

        if (!connectionActive()) {

            CharSequence text = "Looks like you're not connected to the internet, so we couldn't capture your report. Please connect to a network and try again.";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(getBaseContext(), text, duration);
            toast.show();

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

    }

    @Override
    protected void onPause() {

        super.onPause();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

    }

}