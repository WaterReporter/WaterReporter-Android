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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.DisplayDecimal;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.GeometryResponse;
import com.viableindustries.waterreporter.data.GroupNameComparator;
import com.viableindustries.waterreporter.data.ImageProperties;
import com.viableindustries.waterreporter.data.ImageService;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportPatchBody;
import com.viableindustries.waterreporter.data.ReportPostBody;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.Submission;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserHolder;
import com.viableindustries.waterreporter.data.UserProperties;
import com.viableindustries.waterreporter.data.UserService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    @Bind(R.id.scrollView)
    ScrollView scrollView;

    @Bind(R.id.date_input)
    EditText dateField;

    @Bind(R.id.comment_input)
    EditText commentsField;

    @Bind(R.id.preview)
    ImageView mImageView;

    @Bind(R.id.groups)
    LinearLayout groupList;

    @Bind(R.id.comment_component)
    LinearLayout commentComponent;

    @Bind(R.id.camera_button_container)
    RelativeLayout cameraButtonContainer;

    @Bind(R.id.date_component)
    LinearLayout dateComponent;

    @Bind(R.id.date_button_container)
    RelativeLayout dateButtonContainer;

    @Bind(R.id.location_component)
    LinearLayout locationComponent;

    @Bind(R.id.location_button_container)
    RelativeLayout locationButtonContainer;

    @Bind(R.id.add_location)
    ImageView addLocationIcon;

    @Bind(R.id.edit_location)
    ImageView editLocationIcon;

    @Bind(R.id.location_output)
    TextView locationOutput;

    private static final int ACTION_SET_LOCATION = 0;

    private static final int ACTION_ADD_PHOTO = 1;

    private String mGalleryPath;

    private String mTempImagePath;

    private int mImageId;

    private LatLng location;

    private String dateText;

    private String commentsText;

    protected double latitude;

    protected double longitude;

    private boolean editMode;

    protected Map<String, Integer> groupMap = new HashMap<>();

    private ReportService reportService;

    private ImageService imageService;

    private SharedPreferences prefs;

    private SharedPreferences groupPrefs;

    private String access_token;

    private GeometryResponse geometryResponse;

    private Report report;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_metadata);

        ButterKnife.bind(this);

        // Instantiate SharedPreference references

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        groupPrefs = getSharedPreferences(getString(R.string.associated_group_key), MODE_PRIVATE);

        // Instantiate API services

        reportService = ReportService.restAdapter.create(ReportService.class);

        imageService = ImageService.restAdapter.create(ImageService.class);

        if (!ConnectionUtility.connectionActive(this)) {

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

            editMode = extras.getBoolean("EDIT_MODE", false);

            mTempImagePath = extras.getString("image_path", "");

            mGalleryPath = extras.getString("gallery_path", "");

            Log.d("galleryPath", mGalleryPath);

            mImageId = extras.getInt("image_id", 0);

            Log.d("image_path", mTempImagePath);

            Log.d("image_id", mImageId + "");

            try {

                Picasso.with(this)
                        .load(new File(mTempImagePath))
                        .placeholder(R.drawable.square_placeholder)
                        .into(mImageView);

                mImageView.setVisibility(View.VISIBLE);

            } catch (OutOfMemoryError om) {

                //
                Log.d("memory", "too heavy");

            }

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

                addLocationIcon.setVisibility(View.GONE);

                editLocationIcon.setVisibility(View.VISIBLE);

                locationOutput.setText(String.format("%s, %s", longitude, latitude));

            }

            mImageId = savedInstanceState.getInt("image_id", 0);

        }

        // If edit mode is active, set the values of the relevant fields

        if (editMode) {

            report = ReportHolder.getReport();

            // Set comment text

            if (report.properties.report_description != null && (report.properties.report_description.length() > 0)) {

                commentsField.setText(report.properties.report_description);

            }

            // Set location text

            Geometry geometry = report.geometry.geometries.get(0);

            Log.d("geometry", geometry.toString());

            latitude = geometry.coordinates.get(1);

            longitude = geometry.coordinates.get(0);

            locationOutput.setText(DisplayDecimal.formatDecimals("#.###", "%s, %s", longitude, latitude));

            // Load image

            Picasso.with(this).load(report.properties.images.get(0).properties.square_retina).placeholder(R.drawable.user_avatar_placeholder).into(mImageView);

            // Set date text

            dateField.setText(AttributeTransformUtility.parseDate(new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()), report.properties.report_date));

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

        // Add click listener to photo element

        if (!editMode) {

            cameraButtonContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addImage(view);
                }
            });

        }

        // Add click listener to location element

        locationButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateLocation(view);
            }
        });

        locationOutput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateLocation(view);
            }
        });

        // Retrieve the user's group collection

        fetchUserGroups();

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

                        longitude = location.getLongitude();

                        latitude = location.getLatitude();

                        Log.d("position", longitude + latitude + "");

                        locationOutput.setText(DisplayDecimal.formatDecimals("#.###", "%s, %s", longitude, latitude));

                        addLocationIcon.setVisibility(View.GONE);

                        editLocationIcon.setVisibility(View.VISIBLE);

                        CharSequence text = "Location captured successfully";

                        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);

                        toast.show();

                    }

                }

                break;

            case ACTION_ADD_PHOTO:

                if (resultCode == RESULT_OK) {

                    mTempImagePath = data.getStringExtra("file_path");

                    if (mTempImagePath != null) {

                        mImageView.setVisibility(View.VISIBLE);

                        File photo = new File(mTempImagePath);

                        Picasso.with(this).load(photo).placeholder(R.drawable.user_avatar_placeholder).into(mImageView);

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

    public void addImage(View v) {

        startActivityForResult(new Intent(this, PhotoActivity.class), ACTION_ADD_PHOTO);

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

        CharSequence text =
                "Error posting report. Please try again later.";

        Toast toast = Toast.makeText(getBaseContext(), text,
                Toast.LENGTH_SHORT);

        toast.show();

    }

    // Extract input values and prepare request (POST or PATCH)

    public void stageRequest(View view) {

        if (!ConnectionUtility.connectionActive(this)) {

            CharSequence text = "Looks like you're not connected to the internet, so we couldn't capture your report. Please connect to a network and try again.";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(getBaseContext(), text, duration);
            toast.show();

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

        scrollView.smoothScrollTo(0, 0);

        access_token = prefs.getString("access_token", "");

        dateText = String.valueOf(dateField.getText());

        commentsText = commentsField.getText().toString();

        Log.d("comments", commentsText);

        ArrayList<Double> coordinates = new ArrayList<Double>(2);

        String point = "Point";

        String type = "GeometryCollection";

        List<Geometry> geometryList = new ArrayList<Geometry>(1);

        coordinates.clear();
        geometryList.clear();

        coordinates.add((double) longitude);
        coordinates.add((double) latitude);
        Geometry geometry = new Geometry(coordinates, point);
        geometryList.add(geometry);

        geometryResponse = new GeometryResponse(geometryList, type);

        if (commentsText.isEmpty() || mTempImagePath == null) {

            CharSequence text = "Please add a photo and comment.";
            Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            toast.show();

            return;

        }

        if (editMode) {

            patchReport();

        } else {

            postReport();

        }

    }

    // POST new report

    private void postReport() {

        CharSequence text = "Posting report...";

        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);

        toast.show();

        Log.d("filepath", mTempImagePath);

        File photo = new File(mTempImagePath);

        FileNameMap fileNameMap = URLConnection.getFileNameMap();

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

                        List<Map<String, Integer>> groups = new ArrayList<Map<String, Integer>>();

                        try {

                            Map<String, ?> groupKeys = groupPrefs.getAll();

                            for (Map.Entry<String, ?> entry : groupKeys.entrySet()) {

                                Integer value = (Integer) entry.getValue();

                                if (value > 0) {

                                    Map<String, Integer> groupId = new HashMap<String, Integer>();

                                    groupId.put("id", value);

                                    groups.add(groupId);

                                }

                            }

                        } catch (NullPointerException ne) {

                            Log.d("groups", "No groups selected.");

                        }

                        ReportPostBody reportPostBody = new ReportPostBody(geometryResponse, groups,
                                images, true, dateText, commentsText, "public");

                        Log.d("groups", groups.toString());

                        reportService.postReport(access_token, "application/json", reportPostBody,
                                new Callback<Report>() {
                                    @Override
                                    public void success(Report report,
                                                        Response response) {

                                        // Immediately delete the cached image file now that we no longer need it

                                        File tempFile = new File(mTempImagePath);

                                        boolean imageDeleted = tempFile.delete();

                                        Log.w("Delete Check", "File deleted: " + tempFile + imageDeleted);

                                        // Clear any stored group associations

                                        groupPrefs.edit().clear().apply();

                                        final Handler handler = new Handler();

                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                final SharedPreferences coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

                                                int coreId = coreProfile.getInt("id", 0);

                                                UserProperties userProperties = new UserProperties(coreId, coreProfile.getString("description", ""),
                                                        coreProfile.getString("first_name", ""), coreProfile.getString("last_name", ""),
                                                        coreProfile.getString("organization_name", ""), coreProfile.getString("picture", null),
                                                        coreProfile.getString("public_email", ""), coreProfile.getString("title", ""), null, null, null);

                                                User coreUser = User.createUser(coreId, userProperties);

                                                UserHolder.setUser(coreUser);

                                                // Re-direct user to main activity feed, which has the effect of preventing
                                                // unwanted access to the history stack

                                                Intent intent = new Intent(PhotoMetaActivity.this, MainActivity.class);

                                                startActivity(intent);

                                                finish();

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

    // PATCH existing report

    private void patchReport() {

        CharSequence text = "Updating report...";

        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);

        toast.show();

        List<Map<String, Integer>> groups = new ArrayList<Map<String, Integer>>();

        try {

            Map<String, ?> groupKeys = groupPrefs.getAll();

            for (Map.Entry<String, ?> entry : groupKeys.entrySet()) {

                Integer value = (Integer) entry.getValue();

                if (value > 0) {

                    Map<String, Integer> groupId = new HashMap<String, Integer>();

                    groupId.put("id", value);

                    groups.add(groupId);

                }

            }

        } catch (NullPointerException ne) {

            Log.d("groups", "No groups selected.");

        }

        ReportPatchBody reportPatchBody = new ReportPatchBody(geometryResponse, groups, dateText, commentsText, "public");

        Log.d("groups", groups.toString());

        reportService.updateReport(access_token, "application/json", report.id, reportPatchBody,
                new Callback<Report>() {
                    @Override
                    public void success(Report report, Response response) {

                        // Clear any stored group associations

                        groupPrefs.edit().clear().apply();

                        final Handler handler = new Handler();

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                final SharedPreferences coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

                                int coreId = coreProfile.getInt("id", 0);

                                UserProperties userProperties = new UserProperties(coreId, coreProfile.getString("description", ""),
                                        coreProfile.getString("first_name", ""), coreProfile.getString("last_name", ""),
                                        coreProfile.getString("organization_name", ""), coreProfile.getString("picture", null),
                                        coreProfile.getString("public_email", ""), coreProfile.getString("title", ""), null, null, null);

                                User coreUser = User.createUser(coreId, userProperties);

                                UserHolder.setUser(coreUser);

                                // Re-direct user to main activity feed, which has the effect of preventing
                                // unwanted access to the history stack

                                Intent intent = new Intent(PhotoMetaActivity.this, MainActivity.class);

                                startActivity(intent);

                                finish();

                            }

                        }, 2000);

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onPostError();
                    }

                });

    }

    protected void fetchUserGroups() {

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        Log.d("", access_token);

        // We shouldn't need to retrieve this value again, but we'll deal with that issue later
        int user_id = prefs.getInt("user_id", 0);

        UserService service = UserService.restAdapter.create(UserService.class);

        service.getUserOrganization(access_token, "application/json", user_id, new Callback<OrganizationFeatureCollection>() {

            @Override
            public void success(OrganizationFeatureCollection organizationCollectionResponse, Response response) {

                ArrayList<Organization> organizations = organizationCollectionResponse.getFeatures();

                if (organizations.size() > 0) {

                    Collections.sort(organizations, new GroupNameComparator());

                    populateOrganizations(organizations);

                }

            }

            @Override
            public void failure(RetrofitError error) {

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(PhotoMetaActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void populateOrganizations(ArrayList<Organization> orgs) {

        if (!orgs.isEmpty()) {

            groupList.setVisibility(View.VISIBLE);

            // Populating a LinearLayout here rather than a ListView

            final OrganizationCheckListAdapter adapter = new OrganizationCheckListAdapter(this, orgs);

            final int adapterCount = adapter.getCount();

            for (int i = 0; i < adapterCount; i++) {

                View item = adapter.getView(i, null, null);

                groupList.addView(item);

            }

        }

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

            commentsText = String.valueOf(commentsField.getText());

            // Step through comment and location checks and warn the user if anything's missing.

            if (commentsText.isEmpty() || mTempImagePath == null) {

                CharSequence text = "Please add a photo and comment.";
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

            return true;

        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onResume() {

        super.onResume();

        // Check for a data connection!

        if (!ConnectionUtility.connectionActive(this)) {

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