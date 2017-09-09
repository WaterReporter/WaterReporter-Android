package com.viableindustries.waterreporter;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.CacheManager;
import com.viableindustries.waterreporter.data.CancelableCallback;
import com.viableindustries.waterreporter.data.Comment;
import com.viableindustries.waterreporter.data.CommentCollection;
import com.viableindustries.waterreporter.data.CommentPost;
import com.viableindustries.waterreporter.data.CommentService;
import com.viableindustries.waterreporter.data.HashTag;
import com.viableindustries.waterreporter.data.HashtagCollection;
import com.viableindustries.waterreporter.data.ImageProperties;
import com.viableindustries.waterreporter.data.ImageService;
import com.viableindustries.waterreporter.data.QueryFilter;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.ReportStateBody;
import com.viableindustries.waterreporter.data.TagHolder;
import com.viableindustries.waterreporter.data.TagService;
import com.viableindustries.waterreporter.data.UserProfileListener;
import com.viableindustries.waterreporter.dialogs.CommentActionDialog;
import com.viableindustries.waterreporter.dialogs.CommentActionDialogListener;
import com.viableindustries.waterreporter.dialogs.CommentPhotoDialog;
import com.viableindustries.waterreporter.dialogs.CommentPhotoDialogListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class CommentActivity extends AppCompatActivity implements
        CommentPhotoDialogListener,
        CommentActionDialogListener,
        PhotoPickerDialogFragment.PhotoPickerDialogListener,
        EasyPermissions.PermissionCallbacks {

    @Bind(R.id.commentListContainer)
    SwipeRefreshLayout commentListContainer;

    @Bind(R.id.commentList)
    ListView commentList;

    @Bind(R.id.comment_component)
    LinearLayout commentComponent;

    @Bind(R.id.camera_button_container)
    RelativeLayout cameraButtonContainer;

    @Bind(R.id.send_button_container)
    RelativeLayout sendButtonContainer;

    @Bind(R.id.add_comment_image)
    ImageView addImageIcon;

    @Bind(R.id.send)
    ImageView sendComment;

    @Bind(R.id.comment_box)
    RelativeLayout commentBox;

    @Bind(R.id.comment_input)
    EditText commentInput;

    @Bind(R.id.comment_image_preview)
    ImageView mImageView;

    @Bind(R.id.suggestion_separator)
    View suggestionSeparator;

    @Bind(R.id.tag_component)
    HorizontalScrollView tagComponent;

    @Bind(R.id.tag_results)
    LinearLayout tagResults;

    private Report report;

    private String body;

    protected CommentAdapter commentAdapter;

    protected List<Comment> commentCollectionList = new ArrayList<Comment>();

    private boolean working;

    private String mTempImagePath;

    private String reportState = "open";

    private static final int ACTION_TAKE_PHOTO = 1;

    private static final int ACTION_SELECT_PHOTO = 2;

    protected boolean photoCaptured = false;

    final private String FILE_PROVIDER_AUTHORITY = "com.viableindustries.waterreporter.fileprovider";

    private Uri imageUri;

    private static final int RC_ALL_PERMISSIONS = 100;

    private static final int RC_SETTINGS_SCREEN = 125;

    private static final String TAG = "ProfileBasicActivity";

    private SharedPreferences prefs;

    private Context mContext;

    ArrayList<HashTag> baseTagList;

    Handler handler;

    Runnable tagSearchRunnable;

    protected TagSuggestionAdapter tagSuggestionAdapter;

    private String query;

    private String tagToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comment);

        ButterKnife.bind(this);

        mContext = this;

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        report = ReportHolder.getReport();

        addListViewHeader(report);

        // Check permissions

        verifyPermissions();

        // Load comments

        fetchComments(50, 1);

        // Initialize empty list to hold hashtags

        baseTagList = new ArrayList<HashTag>();

        // Add text change listener to comment input.
        // Observe changes and respond accordingly.

        handler = new Handler(Looper.getMainLooper());

        tagSearchRunnable = new Runnable() {
            @Override
            public void run() {

                if (!tagToken.isEmpty() && tagToken.length() > 2 && !tagToken.equals(TagHolder.getCurrent())) {

                    fetchTags(10, 1, buildQuery("tag", "asc", tagToken));

                } else {

                    tagResults.removeAllViews();

                    suggestionSeparator.setVisibility(View.GONE);

                    tagComponent.setVisibility(View.GONE);

                }

            }

        };

        commentInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {

                query = cs.toString();

                if (!query.isEmpty() && query.contains("#")) {

                    tagToken = query.substring(query.lastIndexOf("#")).replace("#", "");

                } else {

                    tagToken = "";

                }

                Log.d("token", tagToken);

            }

            @Override
            public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                // Ensure that the user's cursor is placed at the end of the selection

                commentInput.setSelection(commentInput.getText().length());

                handler.removeCallbacks(tagSearchRunnable);

                handler.postDelayed(tagSearchRunnable, 300 /*delay*/);

            }

        });

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

        final String accessToken = prefs.getString("access_token", "");

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

            suggestionSeparator.setVisibility(View.VISIBLE);

            tagComponent.setVisibility(View.VISIBLE);

            baseTagList.addAll(hashTags);

            tagSuggestionAdapter = new TagSuggestionAdapter(this, baseTagList);

            final int adapterCount = tagSuggestionAdapter.getCount();

            for (int i = 0; i < adapterCount; i++) {

                View item = tagSuggestionAdapter.getView(i, null, tagResults);

                tagResults.addView(item);

            }

        } else {

            suggestionSeparator.setVisibility(View.GONE);

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

    @AfterPermissionGranted(RC_ALL_PERMISSIONS)
    private void completeSetUp() {

        cameraButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPhoto();
            }
        });

        cameraButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mTempImagePath == null) {

                    addPhoto();

                } else {

                    presentPhotoActions();

                }
            }
        });

        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Check for minimum data requirements

                body = (commentInput.getText().length() > 0) ? commentInput.getText().toString() : null;

                if (working || (body == null && mTempImagePath == null)) return;

                final SharedPreferences coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

                if ("admin".equals(coreProfile.getString("role", ""))) {

                    presentAdminActions();

                } else {

                    prepareComment(false);

                }

            }
        });

        // Set refresh listener on report feed container

        commentListContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        fetchComments(50, 1);
                    }
                }
        );

        // Set color of swipe refresh arrow animation

        commentListContainer.setColorSchemeResources(R.color.waterreporter_blue);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.organization_list, menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void addListViewHeader(Report report) {

        Log.d("report", report.toString());

        Log.d("report", report.properties.owner.toString());

        Log.d("report", report.properties.owner.properties.toString());

        LayoutInflater inflater = getLayoutInflater();

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.comment_list_header, commentList, false);

        TextView reportOwner = (TextView) header.findViewById(R.id.post_owner);

        TextView reportDate = (TextView) header.findViewById(R.id.post_date);

        TextView reportCaption = (TextView) header.findViewById(R.id.post_caption);

        ImageView ownerAvatar = (ImageView) header.findViewById(R.id.owner_avatar);

        ImageView actionBanner = (ImageView) header.findViewById(R.id.action_banner);

        String creationDate = (String) AttributeTransformUtility.relativeTime(report.properties.created);

        reportDate.setText(creationDate);

        reportOwner.setText(String.format("%s %s", report.properties.owner.properties.first_name, report.properties.owner.properties.last_name));

        // Load user's avatar

        String avatarUrl = report.properties.owner.properties.picture;

        Picasso.with(this).
                load(avatarUrl)
                .placeholder(R.drawable.user_avatar_placeholder_003)
                .transform(new CircleTransform())
                .into(ownerAvatar);

        // Attach click listener to avatar

        ownerAvatar.setOnClickListener(new UserProfileListener(this, report.properties.owner));

        // Load report caption

        if (report.properties.description != null && (report.properties.description.length() > 0)) {

            reportCaption.setVisibility(View.VISIBLE);

            reportCaption.setText(report.properties.description.trim());

            new PatternEditableBuilder().
                    addPattern(mContext, Pattern.compile("\\#(\\w+)"), ContextCompat.getColor(mContext, R.color.waterreporter_blue),
                            new PatternEditableBuilder.SpannableClickedListener() {
                                @Override
                                public void onSpanClicked(String text) {

                                    Intent intent = new Intent(mContext, TagProfileActivity.class);
                                    intent.putExtra("tag", text);
                                    mContext.startActivity(intent);

                                }
                            }).into(reportCaption);

        } else {

            reportCaption.setVisibility(View.GONE);

        }

        // Display badge if report is closed

        if ("closed".equals(report.properties.state)) {

            actionBanner.setVisibility(View.VISIBLE);

        } else {

            actionBanner.setVisibility(View.GONE);

        }

        // Add populated header view to report timeline

        commentList.addHeaderView(header, null, false);

    }

    private void fetchComments(int limit, int page) {

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<QuerySort>();

        QuerySort querySort = new QuerySort("created", "desc");

        queryOrder.add(querySort);

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(null, queryOrder);

        String query = new Gson().toJson(queryParams);

        Log.d("URL", query);

        ReportService service = ReportService.restAdapter.create(ReportService.class);

        commentListContainer.setRefreshing(true);

        service.getReportComments(accessToken, "application/json", report.id, page, limit, query, new CancelableCallback<CommentCollection>() {

            @Override
            public void onSuccess(CommentCollection commentCollection, Response response) {

                List<Comment> comments = commentCollection.getFeatures();

                Log.v("list", comments.toString());

                commentCollectionList = comments;

                populateComments(commentCollectionList);

                commentListContainer.setRefreshing(false);

            }

            @Override
            public void onFailure(RetrofitError error) {

                commentListContainer.setRefreshing(false);

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(CommentActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void populateComments(List<Comment> comments) {

        commentAdapter = new CommentAdapter(this, comments);

        commentList.setAdapter(commentAdapter);

    }

    public void addPhoto() {

        showPhotoPickerDialog();

    }

    private void prepareComment(boolean changeState) {

        String state = null;

        if (changeState) {

            state = reportState;

            patchReport(report.id, reportState);

        }

        try {

            appendImage(mTempImagePath);

        } catch (NullPointerException ne) {

            CommentPost commentPost = new CommentPost(body, null, report.id, state, "public");

            sendComment(commentPost);

        }

    }

    protected void onPostError(RetrofitError error) {

        commentListContainer.setRefreshing(false);

        // Lift UI lock

        working = false;

        if (error == null) return;

        Response errorResponse = error.getResponse();

        // If we have a valid response object, check the status code and redirect to log in view if necessary

        if (errorResponse != null) {

            int status = errorResponse.getStatus();

            if (status == 403) {

                startActivity(new Intent(CommentActivity.this, SignInActivity.class));

            }

        }

        CharSequence text =
                "Error posting comment. Please try again later.";

        Toast toast = Toast.makeText(getBaseContext(), text,
                Toast.LENGTH_SHORT);

        toast.show();

    }

    private void appendImage(@NonNull final String filePath) {

        // Change UI state

        commentListContainer.setRefreshing(true);

        final ImageService imageService = ImageService.restAdapter.create(ImageService.class);

        final String accessToken = prefs.getString("access_token", "");

        FileNameMap fileNameMap = URLConnection.getFileNameMap();

        Log.d("filepath", filePath);

        File photo = new File(filePath);

        String mimeType = fileNameMap.getContentTypeFor(filePath);

        TypedFile typedPhoto = new TypedFile(mimeType, photo);

        imageService.postImageAsync(accessToken, typedPhoto,
                new CancelableCallback<ImageProperties>() {
                    @Override
                    public void onSuccess(ImageProperties imageProperties,
                                        Response response) {

                        // Immediately delete the cached image file now that we no longer need it

                        File tempFile = new File(filePath);

                        boolean imageDeleted = tempFile.delete();

                        Log.w("Delete Check", "File deleted: " + tempFile + imageDeleted);

                        // Clear the app data cache

                        CacheManager.deleteCache(getBaseContext());

                        mTempImagePath = null;

                        // Retrieve the image id and create a new report

                        final Map<String, Integer> image_id = new HashMap<String, Integer>();

                        image_id.put("id", imageProperties.id);

                        List<Map<String, Integer>> images = new ArrayList<Map<String, Integer>>();

                        images.add(image_id);

                        CommentPost commentPost = new CommentPost(body, images, report.id, reportState, "public");

                        sendComment(commentPost);

                    }

                    @Override
                    public void onFailure(RetrofitError error) {
                        onPostError(error);
                    }

                });

    }

    private void sendComment(CommentPost commentPost) {

        // Change UI state

        commentListContainer.setRefreshing(true);

        working = true;

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        CommentService service = CommentService.restAdapter.create(CommentService.class);

        service.postComment(accessToken, "application/json", commentPost, new CancelableCallback<Comment>() {

            @Override
            public void onSuccess(Comment comment, Response response) {

                // Lift UI lock

                working = false;

                commentListContainer.setRefreshing(false);

                // Clear the comment box

                commentInput.setText("");

                commentCollectionList.add(comment);

                // Remove image preview

                addImageIcon.setVisibility(View.VISIBLE);

                mImageView.setVisibility(View.GONE);

                try {

                    commentAdapter.notifyDataSetChanged();

                } catch (NullPointerException ne) {

                    populateComments(commentCollectionList);

                }

            }

            @Override
            public void onFailure(RetrofitError error) {

                onPostError(error);

            }

        });

    }

    private void patchReport(int reportId, String state) {

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        ReportService service = ReportService.restAdapter.create(ReportService.class);

        ReportStateBody reportStateBody = new ReportStateBody(reportId, state);

        service.setReportState(accessToken, "application/json", reportId, reportStateBody, new CancelableCallback<Report>() {

            @Override
            public void onSuccess(Report report, Response response) {

                // Update current bookmarked report

                ReportHolder.setReport(report);

            }

            @Override
            public void onFailure(RetrofitError error) {

                onPostError(error);

            }

        });

    }

    public void presentPhotoActions() {

        DialogFragment photoActions = new CommentPhotoDialog();

        photoActions.show(getSupportFragmentManager(), "photo_actions");

    }

    private void presentAdminActions() {

        DialogFragment adminActions = new CommentActionDialog();

        adminActions.show(getSupportFragmentManager(), "admin_actions");

    }

    @Override
    public void onReturnValue(int index) {

        if (index == 0) {

            showPhotoPickerDialog();

        } else {

            // Remove the temporary file from the cache

            File tempFile = new File(mTempImagePath);

            boolean imageDeleted = tempFile.delete();

            Log.w("Delete Check", "File deleted: " + tempFile + imageDeleted);

            mTempImagePath = null;

            // Hide and reset the image preview

            mImageView.setVisibility(View.GONE);

            mImageView.setImageResource(android.R.color.transparent);

            // Show the camera button

            addImageIcon.setVisibility(View.VISIBLE);

        }

    }

    @Override
    public void onSelectAction(int index) {

        if (index == 1) {

            Report report = ReportHolder.getReport();

            if ("closed".equals(report.properties.state)) {

                reportState = "open";

            } else {

                reportState = "closed";

            }

            prepareComment(true);

        } else {

            prepareComment(false);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case ACTION_TAKE_PHOTO:

                if (resultCode == RESULT_OK) {

                    this.revokeUriPermission(imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

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

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);

                        fOut.flush();

                        fOut.close();

                        addImageIcon.setVisibility(View.GONE);

                        mImageView.setVisibility(View.VISIBLE);

                        Picasso.with(this)
                                .load(thumb)
                                .placeholder(R.drawable.user_avatar_placeholder_003)
                                .into(mImageView);

                        photoCaptured = true;

                    } catch (Exception e) {

                        e.printStackTrace();

                        Log.d(null, "Save file error!");

                        mTempImagePath = null;

                        // Hide and reset the image preview

                        mImageView.setVisibility(View.GONE);

                        mImageView.setImageResource(android.R.color.transparent);

                        return;

                    }

                }

                break;

            case ACTION_SELECT_PHOTO:

                if (resultCode == RESULT_OK) {

                    if (data != null) {

                        try {

                            File f = FileUtils.createImageFile(this);

                            mTempImagePath = f.getAbsolutePath();

                            Log.d("filepath", mTempImagePath);

                            // Use FileProvider to comply with Android security requirements.
                            // See: https://developer.android.com/training/camera/photobasics.html
                            // https://developer.android.com/reference/android/os/FileUriExposedException.html

                            imageUri = data.getData();

                            InputStream inputStream = getContentResolver().openInputStream(imageUri);

                            Bitmap bitmap = FileUtils.decodeSampledBitmapFromStream(this, imageUri, 1080, 1080);

                            FileOutputStream fOut = new FileOutputStream(f);

                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);

                            fOut.flush();

                            fOut.close();

                            addImageIcon.setVisibility(View.GONE);

                            mImageView.setVisibility(View.VISIBLE);

                            Picasso.with(this)
                                    .load(f)
                                    .placeholder(R.drawable.user_avatar_placeholder_003)
                                    .into(mImageView);

                            photoCaptured = true;

                        } catch (Exception e) {

                            Snackbar.make(commentListContainer, "Unable to read image.",
                                    Snackbar.LENGTH_SHORT)
                                    .show();

                            // Hide and reset the image preview

                            mImageView.setVisibility(View.GONE);

                            mImageView.setImageResource(android.R.color.transparent);

                        }

                    }

                } else {

                    Log.d("image", "no image data");

                }

                break;

        }

    }


    @Override
    public void onDialogPositiveClick(android.app.DialogFragment dialog) {

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
    public void onDialogNegativeClick(android.app.DialogFragment dialog) {

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);

        photoPickerIntent.setType("image/*");

        if (photoPickerIntent.resolveActivity(getPackageManager()) != null) {

            startActivityForResult(photoPickerIntent, ACTION_SELECT_PHOTO);

        }

    }

    protected void showPhotoPickerDialog() {

        android.app.DialogFragment newFragment = new PhotoPickerDialogFragment();

        FragmentManager fragmentManager = getFragmentManager();

        newFragment.show(fragmentManager, "photoPickerDialog");

    }

    protected void verifyPermissions() {

        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        if (EasyPermissions.hasPermissions(this, permissions)) {

            completeSetUp();

        } else {

            // Ask for all permissions since the app is useless without them
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera_files),
                    RC_ALL_PERMISSIONS, permissions);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
    protected void onResume() {

        super.onResume();

        verifyPermissions();

    }

    @Override
    protected void onPause() {

        super.onPause();

        // Cancel all pending network requests

        CancelableCallback.cancelAll();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

        // Cancel all pending network requests

        CancelableCallback.cancelAll();

    }

}
