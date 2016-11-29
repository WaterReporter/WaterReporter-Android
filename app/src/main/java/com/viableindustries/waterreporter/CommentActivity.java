package com.viableindustries.waterreporter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.internal.Streams;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Comment;
import com.viableindustries.waterreporter.data.CommentCollection;
import com.viableindustries.waterreporter.data.CommentPost;
import com.viableindustries.waterreporter.data.CommentService;
import com.viableindustries.waterreporter.data.FeatureCollection;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.GeometryResponse;
import com.viableindustries.waterreporter.data.ImageProperties;
import com.viableindustries.waterreporter.data.ImageService;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.OrganizationMemberList;
import com.viableindustries.waterreporter.data.OrganizationService;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportPostBody;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.ReportStateBody;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserGroupList;
import com.viableindustries.waterreporter.data.UserHolder;
import com.viableindustries.waterreporter.data.UserProfileListener;
import com.viableindustries.waterreporter.data.UserProperties;
import com.viableindustries.waterreporter.data.UserService;
import com.viableindustries.waterreporter.dialogs.CommentActionDialog;
import com.viableindustries.waterreporter.dialogs.CommentActionDialogListener;
import com.viableindustries.waterreporter.dialogs.CommentPhotoDialog;
import com.viableindustries.waterreporter.dialogs.CommentPhotoDialogListener;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

import static com.viableindustries.waterreporter.data.ReportService.restAdapter;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class CommentActivity extends AppCompatActivity implements CommentPhotoDialogListener, CommentActionDialogListener {

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

    @Bind(R.id.add_image)
    ImageView addImage;

    @Bind(R.id.send)
    ImageView sendComment;

    @Bind(R.id.comment_box)
    RelativeLayout commentBox;

    @Bind(R.id.comment_input)
    EditText commentInput;

    @Bind(R.id.preview)
    ImageView mImageView;

    private Report report;

    private String body;

    protected CommentAdapter commentAdapter;

    protected List<Comment> commentCollectionList = new ArrayList<Comment>();

    private boolean working;

    private String mTempImagePath;

    private String reportState = "open";

    private static final int ACTION_ADD_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comment);

        ButterKnife.bind(this);

        report = ReportHolder.getReport();

        addListViewHeader(report);

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

        fetchComments(50, 1);

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

        TextView reportOwner = (TextView) header.findViewById(R.id.report_owner);

        TextView reportDate = (TextView) header.findViewById(R.id.report_date);

        TextView reportCaption = (TextView) header.findViewById(R.id.report_caption);

        ImageView ownerAvatar = (ImageView) header.findViewById(R.id.owner_avatar);

        ImageView actionBanner = (ImageView) header.findViewById(R.id.action_banner);

        String creationDate = (String) AttributeTransformUtility.relativeTime(report.properties.created);

        reportDate.setText(creationDate);

        reportOwner.setText(String.format("%s %s", report.properties.owner.properties.first_name, report.properties.owner.properties.last_name));

        // Load user's avatar

        String avatarUrl = report.properties.owner.properties.picture;

        Picasso.with(this).
                load(avatarUrl)
                .placeholder(R.drawable.user_avatar_placeholder)
                .transform(new CircleTransform())
                .into(ownerAvatar);

        // Attach click listener to avatar

        ownerAvatar.setOnClickListener(new UserProfileListener(this, report.properties.owner));

        // Load report caption

        if (report.properties.report_description != null && (report.properties.report_description.length() > 0)) {

            reportCaption.setVisibility(View.VISIBLE);

            reportCaption.setText(report.properties.report_description.trim());

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

        SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        Log.d("", access_token);

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

        service.getReportComments(access_token, "application/json", report.id, page, limit, query, new Callback<CommentCollection>() {

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

        commentAdapter = new CommentAdapter(this, comments, true);

        commentList.setAdapter(commentAdapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case ACTION_ADD_PHOTO:

                if (resultCode == RESULT_OK) {

                    mTempImagePath = data.getStringExtra("file_path");

                    if (mTempImagePath != null) {

                        // Remove image preview

                        addImage.setVisibility(View.GONE);

                        mImageView.setVisibility(View.VISIBLE);

                        File photo = new File(mTempImagePath);

                        Picasso.with(this).load(photo).placeholder(R.drawable.user_avatar_placeholder).into(mImageView);

                    }

                }

                break;

        }

    }

    public void addPhoto() {

//        startActivityForResult(new Intent(this, PhotoActivity.class), ACTION_ADD_PHOTO);

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

        SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        FileNameMap fileNameMap = URLConnection.getFileNameMap();

        Log.d("filepath", filePath);

        File photo = new File(filePath);

        String mimeType = fileNameMap.getContentTypeFor(filePath);

        TypedFile typedPhoto = new TypedFile(mimeType, photo);

        imageService.postImage(access_token, typedPhoto,
                new Callback<ImageProperties>() {
                    @Override
                    public void success(ImageProperties imageProperties,
                                        Response response) {

                        // Immediately delete the cached image file now that we no longer need it

                        File tempFile = new File(filePath);

                        boolean imageDeleted = tempFile.delete();

                        Log.w("Delete Check", "File deleted: " + tempFile + imageDeleted);

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
                    public void failure(RetrofitError error) {
                        onPostError(error);
                    }

                });

    }

    private void sendComment(CommentPost commentPost) {

        // Change UI state

        commentListContainer.setRefreshing(true);

        working = true;

        SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        Log.d("", access_token);

        CommentService service = CommentService.restAdapter.create(CommentService.class);

        service.postComment(access_token, "application/json", commentPost, new Callback<Comment>() {

            @Override
            public void success(Comment comment, Response response) {

                // Lift UI lock

                working = false;

                commentListContainer.setRefreshing(false);

                // Clear the comment box

                commentInput.setText("");

                commentCollectionList.add(comment);

                // Remove image preview

                addImage.setVisibility(View.VISIBLE);

                mImageView.setVisibility(View.GONE);

                try {

                    commentAdapter.notifyDataSetChanged();

                } catch (NullPointerException ne) {

                    populateComments(commentCollectionList);

                }

            }

            @Override
            public void failure(RetrofitError error) {

                onPostError(error);

            }

        });

    }

    private void patchReport(int reportId, String state) {

        SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        Log.d("", access_token);

        ReportService service = ReportService.restAdapter.create(ReportService.class);

        ReportStateBody reportStateBody = new ReportStateBody(reportId, state);

        service.setReportState(access_token, "application/json", reportId, reportStateBody, new Callback<Report>() {

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

//            startActivityForResult(new Intent(this, PhotoActivity.class), ACTION_ADD_PHOTO);

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

            addImage.setVisibility(View.VISIBLE);

        }

    }

    @Override
    public void onSelectAction(int index) {

        if (index == 1) {

            Report report = ReportHolder.getReport();

            if ("open".equals(report.properties.state) || "public".equals(report.properties.state)) {

                reportState = "closed";

            } else {

                reportState = "open";

            }

            prepareComment(true);

        } else {

            prepareComment(false);

        }

    }

    @Override
    protected void onResume() {

        super.onResume();

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
