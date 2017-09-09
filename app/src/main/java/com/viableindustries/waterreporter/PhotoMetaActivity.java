package com.viableindustries.waterreporter;

import android.Manifest;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.AbbreviatedOrganization;
import com.viableindustries.waterreporter.data.ApiDispatcher;
import com.viableindustries.waterreporter.data.CacheManager;
import com.viableindustries.waterreporter.data.CancelableCallback;
import com.viableindustries.waterreporter.data.CursorPositionTracker;
import com.viableindustries.waterreporter.data.DisplayDecimal;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.GeometryResponse;
import com.viableindustries.waterreporter.data.HashTag;
import com.viableindustries.waterreporter.data.HashtagCollection;
import com.viableindustries.waterreporter.data.ImageService;
import com.viableindustries.waterreporter.data.OpenGraph;
import com.viableindustries.waterreporter.data.OpenGraphProperties;
import com.viableindustries.waterreporter.data.OpenGraphResponse;
import com.viableindustries.waterreporter.data.OpenGraphTask;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.QueryFilter;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportPatchBody;
import com.viableindustries.waterreporter.data.ReportPostBody;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.TagHolder;
import com.viableindustries.waterreporter.data.TagService;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserHolder;
import com.viableindustries.waterreporter.data.UserProperties;

import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Ryan Hamley on 10/28/14.
 * This activity handles all of the report functionality.
 */
public class PhotoMetaActivity extends AppCompatActivity
        implements PhotoPickerDialogFragment.PhotoPickerDialogListener,
        EasyPermissions.PermissionCallbacks {

    @Bind(R.id.scrollView)
    ScrollView parentLayout;

    @Bind(R.id.comment_input)
    EditText commentsField;

    @Bind(R.id.tag_component)
    HorizontalScrollView tagComponent;

    @Bind(R.id.tag_results)
    LinearLayout tagResults;

    @Bind(R.id.post_image_preview)
    ImageView mImageView;

    @Bind(R.id.groups)
    LinearLayout groupList;

    @Bind(R.id.comment_component)
    LinearLayout commentComponent;

    @Bind(R.id.camera_button_container)
    RelativeLayout cameraButtonContainer;

    @Bind(R.id.add_post_image)
    ImageView addImageIcon;

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

    @Bind(R.id.post_report)
    FloatingActionButton postReport;

    @Bind(R.id.post_success)
    FloatingActionButton postSuccess;

    @Bind(R.id.progress_bar)
    ProgressBar progressBar;

    @Bind(R.id.ogData)
    CardView ogData;

    @Bind(R.id.ogImage)
    ImageView ogImage;

    @Bind(R.id.ogTitle)
    TextView ogTitle;

    @Bind(R.id.ogDescription)
    TextView ogDescription;

    @Bind(R.id.ogUrl)
    TextView ogUrl;

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

    private Context mContext;

    private ReportService reportService;

    private ImageService imageService;

    private SharedPreferences mSharedPreferences;

    private SharedPreferences groupMemberships;

    protected SharedPreferences associatedGroups;

    private String accessToken;

    private GeometryResponse geometryResponse;

    private Report report;

    private String reportState;

    private static final int ACTION_TAKE_PHOTO = 1;
    private static final int ACTION_SELECT_PHOTO = 2;

    protected boolean photoCaptured = false;

    final private String FILE_PROVIDER_AUTHORITY = "com.viableindustries.waterreporter.fileprovider";

    private Uri imageUri;

    private static final int RC_ALL_PERMISSIONS = 100;

    private static final int RC_SETTINGS_SCREEN = 125;

    private static final String TAG = "PhotoMetaActivity";

    ArrayList<HashTag> baseTagList;

    Handler handler;

    Runnable tagSearchRunnable;

    protected TagSuggestionAdapter tagSuggestionAdapter;

    private String query;

    private String tagToken;

    private OpenGraphProperties openGraphProperties;

    List<Map<String, Integer>> images = new ArrayList<Map<String, Integer>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_metadata);

        ButterKnife.bind(this);

        mContext = this;

        // Set ProgressBar appearance

        progressBar.setIndeterminateDrawable(ContextCompat.getDrawable(this, R.drawable.custom_progress_compat));

        // Set FloatingActionButton colors

        postReport.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.base_blue)));

        postSuccess.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue_green)));

        postReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stageRequest(view);
            }
        });

        // Instantiate SharedPreference references

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        groupMemberships = getSharedPreferences(getString(R.string.group_membership_key), 0);

        associatedGroups = getSharedPreferences(getString(R.string.associated_group_key), 0);

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

        Bundle extras = getIntent().getExtras();

        if (extras != null) {

            // Check for presence of pre-populated hashtag

            String autoTag = extras.getString("autoTag", "");

            if (autoTag.length() > 0) {

                commentsField.setText(autoTag);

            } else {

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

            commentsField.setText(savedInstanceState.getString("post_description"));

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

            if (report.properties.description != null && (report.properties.description.length() > 0)) {

                commentsField.setText(report.properties.description);

            }

            // Set location text

            Geometry geometry = report.geometry.geometries.get(0);

            Log.d("geometry", geometry.toString());

            latitude = geometry.coordinates.get(1);

            longitude = geometry.coordinates.get(0);

            locationOutput.setText(DisplayDecimal.formatDecimals("#.###", "%s, %s", longitude, latitude));

            // Load image

            addImageIcon.setVisibility(View.GONE);

            mImageView.setVisibility(View.VISIBLE);

            Picasso.with(this).load(report.properties.images.get(0).properties.square_retina).placeholder(R.drawable.user_avatar_placeholder).into(mImageView);

        }

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

        // Initialize empty list to hold hashtags

        baseTagList = new ArrayList<HashTag>();

        // Add text change listener to comment input.
        // Observe changes and respond accordingly.

        handler = new Handler(Looper.getMainLooper());

        tagSearchRunnable = new Runnable() {
            @Override
            public void run() {

                if (!tagToken.isEmpty() && !tagToken.equals(TagHolder.getCurrent())) {

                    fetchTags(10, 1, buildQuery("tag", "asc", tagToken));

                } else {

                    tagResults.removeAllViews();

                    tagComponent.setVisibility(View.GONE);

                }

            }

        };

        commentsField.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {

                // Retrieve current cursor position

                int pos = commentsField.getSelectionStart();

                CursorPositionTracker.setPosition(pos);

                query = cs.toString();

                if (!query.isEmpty()) {

                    try {

                        // Identify previous character and handle octothorpes

                        String previousCharacter = query.substring(pos - 1, pos);

                        Log.d("selectionEdge", previousCharacter);

                        if (previousCharacter.equals(" ")) {

                            // Spaces are not allowed in tags,
                            // therefore reset octothorpe index
                            // to the default value

                            CursorPositionTracker.resetHashIndex();

                        }

                        if (previousCharacter.equals("#") && CursorPositionTracker.getHashIndex() == 9999) {

                            CursorPositionTracker.setHashIndex(pos - 1);

                        }

                        if (CursorPositionTracker.getHashIndex() < 9999) {

                            // Extract substring bounded by octothorpe position
                            // and current cursor position

                            tagToken = query.substring(CursorPositionTracker.getHashIndex() + 1, pos);

                            Log.d("selectionEdgeToken", tagToken);

                        } else {

                            // Previous character not an octothorpe and
                            // none is already present

                            tagToken = "";

                        }

                    } catch (StringIndexOutOfBoundsException e) {

                        tagToken = "";

                    }

                } else {

                    tagToken = "";

                }

                Log.d("token", tagToken);

                String lastWord = query.substring(query.lastIndexOf(" ") + 1);

                if (URLUtil.isValidUrl(lastWord)) {

                    try {
                        fetchTags(lastWord);
                    } catch (IOException e) {
                        Snackbar.make(parentLayout, "Unable to read URL.",
                                Snackbar.LENGTH_SHORT)
                                .show();
                    }

                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                // Ensure that the user's cursor is placed at the end of the selection

                //commentsField.setSelection(commentsField.getText().length());

                handler.removeCallbacks(tagSearchRunnable);

                handler.postDelayed(tagSearchRunnable, 300 /*delay*/);

            }

        });

    }

    private void displayOpenGraphObject(Map<String, String> openGraphObject, String url) {

        ogData.setVisibility(View.VISIBLE);

        String imageUrl = openGraphObject.get("og:image");

        if (imageUrl.length() > 0) {

            Picasso.with(this)
                    .load(openGraphObject.get("og:image"))
                    .placeholder(R.drawable.open_graph_image_placeholder)
                    .into(ogImage);

        }

        ogTitle.setText(openGraphObject.get("og:title"));
        ogDescription.setText(openGraphObject.get("og:description"));

        String ogDomain;

        try {

            ogDomain = OpenGraph.getDomainName(openGraphObject.get("og:url"));

        } catch (URISyntaxException e) {

            ogDomain = "";

        }

        ogUrl.setText(ogDomain);

        // Remove URL from comment text

        String trimmedInput = query.substring(0, query.indexOf(url)).trim();

        commentsField.setText(trimmedInput);

    }

    private void fetchTags(final String url) throws IOException {

        final String[] ogTags = new String[]{
                "og:url",
                "og:title",
                "og:description",
                "og:image"
        };

        final Map<String, String> ogIdx = new HashMap<>();

        OpenGraphTask openGraphTask = new OpenGraphTask(new OpenGraphResponse() {

            @Override
            public void processFinish(Document output) {
                //Here you will receive the result fired from async class
                //of onPostExecute(result) method.
                try {

                    for (String tag : ogTags) {
                        String tagContent = OpenGraph.parseTag(output, tag);
                        Log.v(tag, tagContent);
                        ogIdx.put(tag, tagContent);
                    }

                    if (ogIdx.get("og:url").length() > 0) {

                        displayOpenGraphObject(ogIdx, url);

                    }

//                    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Calendar.getInstance().getTime());

                    openGraphProperties = new OpenGraphProperties(
                            ogIdx.get("og:image"),
                            ogIdx.get("og:description"),
                            ogIdx.get("og:title"),
                            ogIdx.get("og:url"),
                            mSharedPreferences.getInt("user_id", 0));

                } catch (NullPointerException e) {

                    Snackbar.make(parentLayout, "Unable to read URL.",
                            Snackbar.LENGTH_SHORT)
                            .show();

                }

            }

        });

        openGraphTask.execute(url);

    }

    private String buildQuery(String sortField, String sortDirection, String searchChars) {

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<QuerySort>();

        QuerySort querySort = new QuerySort(sortField, sortDirection);

        queryOrder.add(querySort);

        // Create filter list and add a filter parameter

        List<Object> queryFilters = new ArrayList<>();

        if (searchChars != null) {

            // Set tag filter to match from beginning of token

            QueryFilter tagNameFilter = new QueryFilter("tag", "ilike", String.format("%s%s", searchChars, "%"));

            queryFilters.add(tagNameFilter);

        }

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(queryFilters, queryOrder);

        return new Gson().toJson(queryParams);

    }

    protected void fetchTags(int limit, int page, final String query) {

        final String accessToken = mSharedPreferences.getString("access_token", "");

        Log.d("", accessToken);

        RestAdapter restAdapter = TagService.restAdapter;

        TagService service = restAdapter.create(TagService.class);

        service.getMany(accessToken, "application/json", page, limit, query, new CancelableCallback<HashtagCollection>() {

            @Override
            public void onSuccess(HashtagCollection hashtagCollection, Response response) {

                onTagSuccess(hashtagCollection.getFeatures());

            }

            @Override
            public void onFailure(RetrofitError error) {

                onRequestError(error);

            }

        });

    }

    protected void onTagSuccess(ArrayList<HashTag> hashTags) {

        baseTagList.clear();

        tagResults.removeAllViews();

        if (!hashTags.isEmpty()) {

            tagComponent.setVisibility(View.VISIBLE);

            baseTagList.addAll(hashTags);

            tagSuggestionAdapter = new TagSuggestionAdapter(this, baseTagList);

            final int adapterCount = tagSuggestionAdapter.getCount();

            for (int i = 0; i < adapterCount; i++) {

                View item = tagSuggestionAdapter.getView(i, null, tagResults);

                tagResults.addView(item);

            }

        } else {

            tagComponent.setVisibility(View.GONE);

        }

    }

    protected void onRequestError(RetrofitError error) {

        if (error == null) return;

        Response errorResponse = error.getResponse();

        // If we have a valid response object, check the status code and redirect to log in view if necessary

        if (errorResponse != null) {

            int status = errorResponse.getStatus();

            if (status == 403) {

                startActivity(new Intent(this, SignInActivity.class));

            }

        }

    }

    protected void showPhotoPickerDialog() {

        DialogFragment newFragment = new PhotoPickerDialogFragment();

        FragmentManager fragmentManager = getFragmentManager();

        newFragment.show(fragmentManager, "photoPickerDialog");

    }

    protected String initializeDateField() {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return format.format(calendar.getTime());

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

            case ACTION_TAKE_PHOTO:

                if (resultCode == RESULT_OK) {

                    FileUtils.galleryAddPic(this, mTempImagePath);

                    Log.d("path", mTempImagePath);

                    // Display thumbnail

                    try {

                        File f = new File(mTempImagePath);

                        String thumbName = String.format("%s-%s.jpg", Math.random(), new Date());

                        File thumb = File.createTempFile(thumbName, null, this.getCacheDir());

                        Log.d("taken path", f.getAbsolutePath());
                        Log.d("taken path", f.toString());
                        Log.d("taken path", f.toURI().toString());

                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

                        Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), bmOptions);

                        bitmap = ThumbnailUtils.extractThumbnail(bitmap, 96, 96);

                        FileOutputStream fOut = new FileOutputStream(thumb);

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

                        fOut.flush();

                        fOut.close();

                        addImageIcon.setVisibility(View.GONE);

                        mImageView.setVisibility(View.VISIBLE);

                        Log.d("thumb", thumb.toString());
                        Log.d("thumb", thumb.toURI().toString());
                        Log.d("thumb", thumb.getAbsolutePath());
                        Log.d("thumb", thumb.getPath());

                        Picasso.with(this)
                                .load(thumb)
                                .placeholder(R.drawable.user_avatar_placeholder)
                                .into(mImageView);

                        photoCaptured = true;

                    } catch (Exception e) {

                        e.printStackTrace();

                        Log.d("path error", "Save file error!");

                        mTempImagePath = null;

                        addImageIcon.setVisibility(View.VISIBLE);

                        mImageView.setVisibility(View.GONE);

                        photoCaptured = false;

                        return;

                    }

                }

                break;

            case ACTION_SELECT_PHOTO:

                if (resultCode == RESULT_OK) {

                    if (data != null) {

                        try {

                            imageUri = data.getData();

                            String thumbName = String.format("%s-%s.jpg", Math.random(), new Date());

                            File thumb = File.createTempFile(thumbName, null, this.getCacheDir());

                            try {

                                ParcelFileDescriptor parcelFileDescriptor =
                                        getContentResolver().openFileDescriptor(imageUri, "r");

                                if (parcelFileDescriptor != null) {

                                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                                    Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

                                    parcelFileDescriptor.close();

                                    image = ThumbnailUtils.extractThumbnail(image, 96, 96);

                                    FileOutputStream fOut = new FileOutputStream(thumb);

                                    image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

                                    fOut.flush();

                                    fOut.close();

                                    Log.d("thumb", thumb.toString());
                                    Log.d("thumb", thumb.toURI().toString());
                                    Log.d("thumb", thumb.getAbsolutePath());
                                    Log.d("thumb", thumb.getPath());

                                    Log.d("thumb bitmap", image.toString());

                                    addImageIcon.setVisibility(View.GONE);

                                    mImageView.setVisibility(View.VISIBLE);

                                    Picasso.with(this)
                                            .load(thumb)
                                            .placeholder(R.drawable.user_avatar_placeholder)
                                            .into(mImageView);

                                    photoCaptured = true;

                                }

                            } catch (IOException e) {

                                e.printStackTrace();

                            }

                        } catch (Exception e) {

                            Snackbar.make(parentLayout, "Unable to read image.",
                                    Snackbar.LENGTH_SHORT)
                                    .show();

                            addImageIcon.setVisibility(View.VISIBLE);

                            mImageView.setVisibility(View.GONE);

                            mTempImagePath = null;

                            photoCaptured = false;

                        }

                    }

                } else {

                    Log.d("image", "no image data");

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

        showPhotoPickerDialog();

    }

    protected void onPostSuccess() {

        // Hide ProgressBar

        progressBar.setVisibility(View.INVISIBLE);

        // Show success indicator

        postSuccess.setVisibility(View.VISIBLE);

        // Clear the app data cache

        CacheManager.deleteCache(getBaseContext());

        // Clear any stored group associations

        associatedGroups.edit().clear().apply();

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

    protected void onPostError() {

        progressBar.setVisibility(View.INVISIBLE);

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

        accessToken = mSharedPreferences.getString("access_token", "");

        dateText = initializeDateField();

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

        // Abort with message if neither comments nor Open Graph data are present

        if ((commentsText.isEmpty() && openGraphProperties == null) && !editMode) {

            CharSequence text = "Please add a photo, write a comment, or paste a link to share.";
            Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            toast.show();

            return;

        }

        // Need an explicit location check so the user is reminded to confirm

        if (location != null) {

            latitude = location.getLatitude();

            longitude = location.getLongitude();

            if ((latitude == 0 || longitude == 0) && !editMode) {

                CharSequence text = "Please verify your location.";
                Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
                toast.show();

                return;

            }

        } else {

            latitude = mSharedPreferences.getFloat("latitude", 0);

            longitude = mSharedPreferences.getFloat("longitude", 0);

            if ((latitude == 0 || longitude == 0) && !editMode) {

                CharSequence text = "Please verify your location.";
                Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
                toast.show();

                return;

            }

        }

        // Execute the correct action and set the value of the `reportState` string
        // to the appropriate value. This should not be set universally because
        // existing reports may be edited at any time ("closing" a report doesn't lock
        // its state). A user may edit a closed report, therefore passing "open" as
        // the default value of `reportState` would not be correct.

        if (editMode) {

            reportState = report.properties.state;

            patchReport();

        } else {

            reportState = "open";

            postReport();

        }

    }

    // POST new report

    private void postReport() {

        progressBar.setVisibility(View.VISIBLE);

        CharSequence text = "Sending post...";

        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);

        toast.show();

        String filePath = mTempImagePath;

        if (imageUri == null) {

            sendFullPost(buildPostBody());

            return;

        }

        if (filePath == null) {

            filePath = FileUtils.getPathFromUri(this, imageUri);

        }

        if (filePath != null) {

            ReportPostBody reportPostBody = buildPostBody();

            String storedPost = new Gson().toJson(reportPostBody);

            String storedPostKey = String.format("stored_post_%s", UUID.randomUUID().toString().replaceAll("-", ""));

            mSharedPreferences.edit().putString(storedPostKey, storedPost).apply();

            mSharedPreferences.edit().putString("PENDING_POST_BODY", storedPost).apply();

            Intent postImageIntent = new Intent(mContext, ImagePostService.class);
            postImageIntent.putExtra("file_path", filePath);
            postImageIntent.putExtra("access_token", accessToken);

            // Send the serialized post body with the intent

            postImageIntent.putExtra("stored_post", storedPost);

            mContext.startService(postImageIntent);

//            mSharedPreferences.edit().putInt("PENDING_IMAGE_ID", 0).apply();

            ApiDispatcher.setTransmissionActive(mSharedPreferences, true);

            onPostSuccess();

        }

    }

    private ReportPostBody buildPostBody() {

        List<Map<String, Integer>> groups = new ArrayList<Map<String, Integer>>();

        try {

            Map<String, ?> groupKeys = associatedGroups.getAll();

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

        List<OpenGraphProperties> social = new ArrayList<OpenGraphProperties>();

        if (openGraphProperties != null) {

            social.add(openGraphProperties);

        }

        return new ReportPostBody(geometryResponse, groups,
                images, true, dateText, commentsText, reportState, social);

    }

    // POST report data

    private void sendFullPost(ReportPostBody reportPostBody) {

        ApiDispatcher.sendFullPost(accessToken, reportPostBody, new SendPostCallbacks() {
            @Override
            public void onSuccess(@NonNull Report post) {
                onPostSuccess();
            }

            @Override
            public void onError(@NonNull RetrofitError error) {
                onPostError();
            }
        });

    }

    // PATCH existing report

    private void patchReport() {

        progressBar.setVisibility(View.VISIBLE);

        CharSequence text = "Updating report...";

        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);

        toast.show();

        List<Map<String, Integer>> groups = new ArrayList<Map<String, Integer>>();

        try {

            Map<String, ?> groupKeys = associatedGroups.getAll();

            Log.d("associated groups", associatedGroups.getAll().toString());

            for (Map.Entry<String, ?> entry : groupKeys.entrySet()) {

                Log.d("associated groups", entry.getValue().toString() + entry.getKey());

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

        ReportPatchBody reportPatchBody = new ReportPatchBody(geometryResponse, groups, dateText, commentsText, reportState);

        Log.d("groups", groups.toString());

        reportService.updateReport(accessToken, "application/json", report.id, reportPatchBody,
                new CancelableCallback<Report>() {
                    @Override
                    public void onSuccess(Report report, Response response) {

                        // Hide ProgressBar

                        progressBar.setVisibility(View.INVISIBLE);

                        // Show success indicator

                        postSuccess.setVisibility(View.VISIBLE);

                        // Clear any stored group associations

                        associatedGroups.edit().clear().apply();

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
                    public void onFailure(RetrofitError error) {
                        onPostError();
                    }

                });

    }

    protected void fetchUserGroups() {

        ArrayList<AbbreviatedOrganization> abbreviatedOrganizations = new ArrayList<>();

        Map<String, ?> keys = groupMemberships.getAll();

        for (Map.Entry<String, ?> entry : keys.entrySet()) {

            int status = Integer.valueOf(entry.getValue().toString());

            if (status > 0) {

                abbreviatedOrganizations.add(new AbbreviatedOrganization(status, entry.getKey()));

            }

        }

        // If editing an existing report, we need to iterate the array of organizations
        // associated with the report and persist references in preferences

        if (editMode) {

            for (Organization organization : report.properties.groups) {

                int selected = groupMemberships.getInt(organization.properties.name, 0);

                if (selected == 0) {

                    abbreviatedOrganizations.add(new AbbreviatedOrganization(organization.properties.id, organization.properties.name));

                }

                // Track entry in associated groups preference.
                // IMPORTANT: This is NOT the preference that records group memberships!

                associatedGroups.edit().putInt(organization.properties.name, organization.properties.id).apply();

            }

        }

        Log.d("associated groups", associatedGroups.getAll().toString());

        populateOrganizations(abbreviatedOrganizations);

    }

    private void populateOrganizations(ArrayList<AbbreviatedOrganization> orgs) {

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
    public void onDialogPositiveClick(DialogFragment dialog) {

        // For compatibility with Android 6.0 (Marshmallow, API 23), we need to check permissions before
        // dispatching takePictureIntent, otherwise the app will crash.

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {

            File f = FileUtils.createImageFile(this);

            mTempImagePath = f.getAbsolutePath();

            Log.d("filepath", mTempImagePath);

            // Use FileProvider to comply with Android security requirements.
            // See: https://developer.android.com/training/camera/photobasics.html
            // https://developer.android.com/reference/android/os/FileUriExposedException.html

            imageUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, f);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            // Using v4 Support Library FileProvider and Camera intent on pre-Marshmallow devices
            // requires granting FileUri permissions at runtime

            List<ResolveInfo> resolvedIntentActivities = this.getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {

                String packageName = resolvedIntentInfo.activityInfo.packageName;

                this.grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            }

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO);

            }

        } catch (IOException e) {

            e.printStackTrace();

            mTempImagePath = null;

        }

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);

        photoPickerIntent.setType("image/*");

        if (photoPickerIntent.resolveActivity(getPackageManager()) != null) {

            startActivityForResult(photoPickerIntent, ACTION_SELECT_PHOTO);

        }

    }

    protected boolean verifyPermissions() {

        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        if (EasyPermissions.hasPermissions(this, permissions)) {

            return true;

        } else {

            // Ask for all permissions since the app is useless without them
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_all_permissions),
                    RC_ALL_PERMISSIONS, permissions);

            return false;

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this, getString(R.string.rationale_ask_again))
                    .setTitle(getString(R.string.title_settings_dialog))
                    .setPositiveButton(getString(R.string.setting))
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            // If the user really doesn't want to play ball, at least them browse reports

                            startActivity(new Intent(getBaseContext(), MainActivity.class));

                        }
                    })
                    .setRequestCode(RC_SETTINGS_SCREEN)
                    .build()
                    .show();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("post_date", dateText);

        outState.putString("post_description", commentsText);

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

                latitude = mSharedPreferences.getFloat("latitude", 0);

                longitude = mSharedPreferences.getFloat("longitude", 0);

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

        } else {

            verifyPermissions();

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

        // Cancel all pending network requests

        CancelableCallback.cancelAll();

    }

    @Override
    public void onBackPressed() {

        // Cancel all pending network requests

        CancelableCallback.cancelAll();

        finish();

        this.overridePendingTransition(R.anim.animation_enter_left,
                R.anim.animation_exit_right);

//        Intent a = new Intent(Intent.ACTION_MAIN);
//        a.addCategory(Intent.CATEGORY_HOME);
//        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(a);

    }

}