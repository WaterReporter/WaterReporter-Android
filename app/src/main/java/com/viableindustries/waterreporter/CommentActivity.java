package com.viableindustries.waterreporter;

import android.Manifest;
import android.app.Activity;
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
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.interfaces.data.comment.DeleteCommentSuccessCallback;
import com.viableindustries.waterreporter.api.models.comment.Comment;
import com.viableindustries.waterreporter.api.models.comment.CommentCollection;
import com.viableindustries.waterreporter.api.models.comment.CommentPost;
import com.viableindustries.waterreporter.api.models.hashtag.HashTag;
import com.viableindustries.waterreporter.api.models.hashtag.HashtagCollection;
import com.viableindustries.waterreporter.api.models.hashtag.TagHolder;
import com.viableindustries.waterreporter.api.models.image.ImageProperties;
import com.viableindustries.waterreporter.api.models.open_graph.OpenGraphProperties;
import com.viableindustries.waterreporter.api.models.open_graph.OpenGraphResponse;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.post.ReportHolder;
import com.viableindustries.waterreporter.api.models.post.ReportStateBody;
import com.viableindustries.waterreporter.api.models.query.QueryFilter;
import com.viableindustries.waterreporter.api.models.query.QueryParams;
import com.viableindustries.waterreporter.api.models.query.QuerySort;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.user_interface.adapters.CommentAdapter;
import com.viableindustries.waterreporter.user_interface.adapters.TagSuggestionAdapter;
import com.viableindustries.waterreporter.user_interface.dialogs.CommentActionDialog;
import com.viableindustries.waterreporter.user_interface.dialogs.CommentActionDialogListener;
import com.viableindustries.waterreporter.user_interface.dialogs.CommentPhotoDialog;
import com.viableindustries.waterreporter.user_interface.dialogs.CommentPhotoDialogListener;
import com.viableindustries.waterreporter.user_interface.dialogs.PhotoPickerDialogFragment;
import com.viableindustries.waterreporter.user_interface.listeners.UserProfileListener;
import com.viableindustries.waterreporter.utilities.AttributeTransformUtility;
import com.viableindustries.waterreporter.utilities.CacheManager;
import com.viableindustries.waterreporter.utilities.CircleTransform;
import com.viableindustries.waterreporter.utilities.CursorPositionTracker;
import com.viableindustries.waterreporter.utilities.FileUtils;
import com.viableindustries.waterreporter.utilities.ModelStorage;
import com.viableindustries.waterreporter.utilities.OpenGraph;
import com.viableindustries.waterreporter.utilities.OpenGraphTask;
import com.viableindustries.waterreporter.utilities.PatternEditableBuilder;

import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URISyntaxException;
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
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class CommentActivity extends AppCompatActivity implements
        CommentPhotoDialogListener,
        CommentActionDialogListener,
        DeleteCommentSuccessCallback,
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

    @Bind(R.id.openGraphProgress)
    ProgressBar openGraphProgress;

    // Open Graph preview

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

    private Report report;

    private String body;

    private CommentAdapter commentAdapter;

    private List<Comment> commentCollectionList = new ArrayList<>();

    private boolean working;

    private String mTempImagePath;

    private String reportState = "open";

    private static final int ACTION_TAKE_PHOTO = 1;

    private static final int ACTION_SELECT_PHOTO = 2;

    private Uri imageUri;

    private static final int RC_ALL_PERMISSIONS = 100;

    private static final int RC_SETTINGS_SCREEN = 125;

    private static final String TAG = "ProfileBasicActivity";

    private SharedPreferences mSharedPreferences;

    private SharedPreferences mCoreProfile;

    private Context mContext;

    private ArrayList<HashTag> baseTagList;

    private Handler handler;

    private Runnable tagSearchRunnable;

    private String query;

    private String tagToken;

    private boolean retrievingOpenGraphData;

    private OpenGraphProperties openGraphProperties;

    private int userId;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comment);

        ButterKnife.bind(this);

        mContext = this;

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        mCoreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

        // Set ProgressBar appearance

        openGraphProgress.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.splash_blue), android.graphics.PorterDuff.Mode.SRC_IN);

        report = ReportHolder.getReport();

        addListViewHeader(report);

        // Check permissions

        verifyPermissions();

        // Load stored data about the authenticated user

        retrieveStoredUser();

        // Initialize empty list to hold hashtags

        baseTagList = new ArrayList<>();

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

                // Retrieve current cursor position

                int pos = commentInput.getSelectionStart();

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

//                String lastWord = query.substring(query.lastIndexOf(" ") + 1);
//
//                if (URLUtil.isValidUrl(lastWord)) {
//
//                    try {
//
//                        OpenGraph.fetchOpenGraphData(
//                                CommentActivity.this,
//                                commentListContainer,
//                                mSharedPreferences.getInt("user_id", 0),
//                                lastWord);
//
//                        if (OpenGraph.openGraphProperties != null) {
//
//                            displayOpenGraphObject(OpenGraph.openGraphProperties, OpenGraph.openGraphProperties.url);
//
//                        }
//
//                    } catch (IOException e) {
//
//                        Snackbar.make(commentListContainer, "Unable to read URL.",
//                                Snackbar.LENGTH_SHORT)
//                                .show();
//
//                    }
//
//                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                // Ensure that the user's cursor is placed at the end of the selection

//                commentInput.setSelection(commentInput.getText().length());

                handler.removeCallbacks(tagSearchRunnable);

                handler.postDelayed(tagSearchRunnable, 300 /*delay*/);

                String lastWord = query.substring(query.lastIndexOf(" ") + 1);

                if (URLUtil.isValidUrl(lastWord)) {

                    if (retrievingOpenGraphData) {

                        return;

                    }

                    try {

                        retrievingOpenGraphData = true;

                        fetchOpenGraphData(
                                CommentActivity.this,
                                commentListContainer,
                                mSharedPreferences.getInt("user_id", 0),
                                lastWord);

                    } catch (IOException e) {

                        Snackbar.make(commentListContainer, "Unable to read URL.",
                                Snackbar.LENGTH_SHORT)
                                .show();

                    }

                }

            }

        });

    }

    private void retrieveStoredUser() {

        user = ModelStorage.getStoredUser(mCoreProfile, "auth_user");

        try {

            userId = user.properties.id;

            // Load comments

            fetchComments(50, 1);

        } catch (NullPointerException _e) {

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

    }

    private String buildQuery(String sortField, String sortDirection, String searchChars) {

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<>();

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

    private void fetchTags(int limit, int page, final String query) {

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getHashTagService().getMany(accessToken, "application/json", page, limit, query, new Callback<HashtagCollection>() {

            @Override
            public void success(HashtagCollection hashtagCollection, Response response) {

                onTagSuccess(hashtagCollection.getFeatures());

            }

            @Override
            public void failure(RetrofitError error) {

                onRequestError(error);

            }

        });

    }

    private void onTagSuccess(ArrayList<HashTag> hashTags) {

        baseTagList.clear();

        tagResults.removeAllViews();

        if (!hashTags.isEmpty()) {

            suggestionSeparator.setVisibility(View.VISIBLE);

            tagComponent.setVisibility(View.VISIBLE);

            baseTagList.addAll(hashTags);

            TagSuggestionAdapter tagSuggestionAdapter = new TagSuggestionAdapter(this, baseTagList);

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

    private void onRequestError(RetrofitError error) {

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

                // Check for minimum api requirements

                body = (commentInput.getText().length() > 0) ? commentInput.getText().toString() : null;

                if (working || (openGraphProperties == null && body == null && mTempImagePath == null))
                    return;

                final SharedPreferences coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

                final User authUser = ModelStorage.getStoredUser(coreProfile, "auth_user");

                if (authUser.properties.isAdmin()) {

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
                        // This method performs the actual api-refresh operation.
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

    private void addListViewHeader(Report report) {

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

        final String accessToken = mSharedPreferences.getString("access_token", "");

        Log.d("", accessToken);

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<>();

        QuerySort querySort = new QuerySort("created", "desc");

        queryOrder.add(querySort);

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(null, queryOrder);

        String query = new Gson().toJson(queryParams);

        commentListContainer.setRefreshing(true);

        RestClient.getReportService().getReportComments(accessToken, "application/json", report.id, page, limit, query, new Callback<CommentCollection>() {

            @Override
            public void success(CommentCollection commentCollection, Response response) {

                List<Comment> comments = commentCollection.getFeatures();

                Log.v("list", comments.toString());

                commentCollectionList = comments;

                populateComments(commentCollectionList);

                commentListContainer.setRefreshing(false);

            }

            @Override
            public void failure(RetrofitError error) {

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

        commentAdapter = new CommentAdapter(this, mSharedPreferences, commentListContainer, comments);

        commentList.setAdapter(commentAdapter);

    }

    private void addPhoto() {

        if (openGraphProperties == null) {

            showPhotoPickerDialog();

        }

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

            List<OpenGraphProperties> social = new ArrayList<>();

            if (openGraphProperties != null) {

                social.add(openGraphProperties);

            }

            CommentPost commentPost = new CommentPost(
                    body,
                    null,
                    report.id,
                    state,
                    social,
                    "public");

            sendComment(commentPost);

        }

    }

    private void onPostError(RetrofitError error) {

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

        final String accessToken = mSharedPreferences.getString("access_token", "");

        FileNameMap fileNameMap = URLConnection.getFileNameMap();

        Log.d("filepath", filePath);

        File photo = new File(filePath);

        String mimeType = fileNameMap.getContentTypeFor(filePath);

        TypedFile typedPhoto = new TypedFile(mimeType, photo);

        RestClient.getImageService().postImageAsync(accessToken, typedPhoto,
                new Callback<ImageProperties>() {
                    @Override
                    public void success(ImageProperties imageProperties,
                                        Response response) {

                        // Immediately delete the cached image file now that we no longer need it

                        File tempFile = new File(filePath);

                        boolean imageDeleted = tempFile.delete();

                        Log.w("Delete Check", "File deleted: " + tempFile + imageDeleted);

                        // Clear the app api cache

                        CacheManager.deleteCache(getBaseContext());

                        mTempImagePath = null;

                        // Retrieve the image id and create a new report

                        final Map<String, Integer> image_id = new HashMap<>();

                        image_id.put("id", imageProperties.id);

                        List<Map<String, Integer>> images = new ArrayList<>();

                        images.add(image_id);

                        CommentPost commentPost = new CommentPost(
                                body,
                                images,
                                report.id,
                                reportState,
                                null,
                                "public");

                        sendComment(commentPost);

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onPostError(error);
                    }

                });

    }

    private void sendComment(CommentPost commentPost) {

        // Change UI state

//        commentListContainer.setRefreshing(true);

        working = true;

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getCommentService().postComment(accessToken, "application/json", commentPost, new Callback<Comment>() {

            @Override
            public void success(Comment comment, Response response) {

                // Clear Open Graph data and UI components

                openGraphProperties = null;

                ogData.setVisibility(View.GONE);

                // Lift UI lock

                working = false;

                commentListContainer.setRefreshing(false);

                // Show comment list

                commentListContainer.setVisibility(View.VISIBLE);

                // Clear the comment box

                commentInput.setText("");

                commentCollectionList.add(comment);

                // Remove image preview

                addImageIcon.setVisibility(View.VISIBLE);

                mImageView.setVisibility(View.GONE);

                fetchComments(50, 1);

//                try {
//
//                    commentAdapter.notifyDataSetChanged();
//
//                } catch (NullPointerException ne) {

//                    populateComments(commentCollectionList);

//                }

            }

            @Override
            public void failure(RetrofitError error) {

                onPostError(error);

            }

        });

    }

    private void patchReport(int reportId, String state) {

        final String accessToken = mSharedPreferences.getString("access_token", "");

        ReportStateBody reportStateBody = new ReportStateBody(reportId, state);

        RestClient.getReportService().setReportState(accessToken, "application/json", reportId, reportStateBody, new Callback<Report>() {

            @Override
            public void success(Report report, Response response) {

                // Update current bookmarked report

                ReportHolder.setReport(report);

            }

            @Override
            public void failure(RetrofitError error) {

                onPostError(error);

            }

        });

    }

    private void presentPhotoActions() {

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

                boolean photoCaptured = false;
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

                    Log.d("image", "no image api");

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

            String FILE_PROVIDER_AUTHORITY = "com.viableindustries.waterreporter.fileprovider";
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

    private void showPhotoPickerDialog() {

        android.app.DialogFragment newFragment = new PhotoPickerDialogFragment();

        FragmentManager fragmentManager = getFragmentManager();

        newFragment.show(fragmentManager, "photoPickerDialog");

    }

    private void verifyPermissions() {

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

    private void fetchOpenGraphData(
            final Activity activity,
            final View parentLayout,
            final int userId,
            final String url) throws IOException {

        final String[] ogTags = new String[]{
                "og:url",
                "og:title",
                "og:description",
                "og:image"
        };

        final Map<String, String> ogIdx = new HashMap<>();

        openGraphProgress.setVisibility(View.VISIBLE);

        OpenGraphTask openGraphTask = new OpenGraphTask(new OpenGraphResponse() {

            @Override
            public void processFinish(Document output) {
                //Here you will receive the result fired from async class
                //of onPostExecute(result) method.
                try {

                    for (String tag : ogTags) {
                        String tagContent = OpenGraph.parseTag(output, tag);
                        Log.v(tag, tagContent);
                        ogIdx.put(tag.replace(":", "_"), tagContent);
                    }

                    openGraphProperties = OpenGraph.buildOpenGraphObject(ogIdx, userId);

                    if (!openGraphProperties.title.isEmpty() && !openGraphProperties.url.isEmpty()) {

                        displayOpenGraphObject(openGraphProperties, openGraphProperties.url);

                    } else {

                        openGraphProperties = null;

                    }

                    retrievingOpenGraphData = false;

                    openGraphProgress.setVisibility(View.GONE);

                } catch (NullPointerException e) {

                    try {

                        openGraphProgress.setVisibility(View.VISIBLE);

                        Snackbar.make(parentLayout, "Unable to read URL.",
                                Snackbar.LENGTH_SHORT)
                                .show();

                    } catch (IllegalArgumentException i) {

                        // Open Graph retrieval task finished in background
                        // but layout references are unbound.

                        activity.finish();

                    }

                }

            }

        });

        openGraphTask.execute(url);

    }

    private void displayOpenGraphObject(OpenGraphProperties openGraphProperties, String url) {

        // Water Reporter accepts a post image OR Open Graph data
        // but not both. After a successful Open Graph retrieval,
        // clear any existing file references and reverse the
        // visibility of the camera icon.

        imageUri = null;

        mTempImagePath = null;

        addImageIcon.setVisibility(View.VISIBLE);

        mImageView.setVisibility(View.GONE);

        // Hide comment list

        commentListContainer.setVisibility(View.GONE);

        // Render Open Graph preview

        ogData.setVisibility(View.VISIBLE);

        String imageUrl = openGraphProperties.imageUrl;

        Log.v("Display Open Graph img", imageUrl);

        Picasso.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.open_graph_placeholder)
                .error(R.drawable.open_graph_placeholder)
                .into(ogImage);

        ogTitle.setText(openGraphProperties.title);
        ogDescription.setText(openGraphProperties.description);

        String ogDomain;

        try {

            ogDomain = OpenGraph.getDomainName(openGraphProperties.url);

        } catch (URISyntaxException e) {

            ogDomain = "";

        }

        ogUrl.setText(ogDomain);

        // Remove URL from comment text

        if (query != null) {

            try {

                String trimmedInput = query.substring(0, query.indexOf(url)).trim();

                commentInput.setText(trimmedInput);

            } catch (IndexOutOfBoundsException e) {

                openGraphProperties = null;

            }

        }

    }

    @Override
    public void onCommentDelete(Comment comment) {

        commentListContainer.setRefreshing(true);

        fetchComments(50, 1);

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

        //Callback.cancelAll();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

        // Cancel all pending network requests

        //Callback.cancelAll();

    }

}