package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
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
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserGroupList;
import com.viableindustries.waterreporter.data.UserHolder;
import com.viableindustries.waterreporter.data.UserProperties;
import com.viableindustries.waterreporter.data.UserService;

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

public class CommentActivity extends AppCompatActivity {

    @Bind(R.id.list)
    ListView listView;

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
    EditText commentBox;

    @Bind(R.id.preview)
    ImageView mImageView;

    private int reportId;

    private String body;

    protected CommentAdapter commentAdapter;

    protected List<Comment> commentCollectionList = new ArrayList<Comment>();

    private boolean working;

    private String mTempImagePath;

    private static final int ACTION_ADD_PHOTO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comment);

        ButterKnife.bind(this);

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

        reportId = ReportHolder.getReport().id;

        service.getReportComments(access_token, "application/json", reportId, page, limit, query, new Callback<CommentCollection>() {

            @Override
            public void success(CommentCollection commentCollection, Response response) {

                List<Comment> comments = commentCollection.getFeatures();

                Log.v("list", comments.toString());

                commentCollectionList.addAll(comments);

                try {

                    commentAdapter.notifyDataSetChanged();

                } catch (NullPointerException ne) {

                    populateComments(commentCollectionList);

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

                        startActivity(new Intent(CommentActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void populateComments(List<Comment> comments) {

        commentAdapter = new CommentAdapter(this, comments, true);

        listView.setAdapter(commentAdapter);

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

    public void addPhoto(View view) {

        startActivityForResult(new Intent(this, PhotoActivity.class), ACTION_ADD_PHOTO);

    }

    public void prepareComment(View view) {

        body = (commentBox.getText().length() > 0) ? commentBox.getText().toString() : null;

        reportId = ReportHolder.getReport().id;

        Log.d("body", body);

        if (working || body.isEmpty()) return;

        try {

            appendImage(mTempImagePath);

        } catch (NullPointerException ne) {

            CommentPost commentPost = new CommentPost(body, null, reportId, null, "public");

            sendComment(commentPost);

        }

    }

    protected void onPostError() {

//        saveMessage.setVisibility(View.GONE);

        CharSequence text =
                "Error posting comment. Please try again later.";

        Toast toast = Toast.makeText(getBaseContext(), text,
                Toast.LENGTH_SHORT);

        toast.show();

    }

    private void appendImage(@NonNull final String filePath) {

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

                        CommentPost commentPost = new CommentPost(body, images, reportId, null, "public");

                        sendComment(commentPost);

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onPostError();
                    }

                });

    }

    private void sendComment(CommentPost commentPost) {

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

                // Clear the comment box

                commentBox.setText("");

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

            }

        });

    }

    @Override
    protected void onResume() {

        super.onResume();

        // Check for a data connection!

//        if (!connectionActive()) {
//
//            CharSequence text = "Looks like you're not connected to the internet, so we couldn't capture your report. Please connect to a network and try again.";
//            int duration = Toast.LENGTH_LONG;
//
//            Toast toast = Toast.makeText(getBaseContext(), text, duration);
//            toast.show();
//
//            startActivity(new Intent(this, MainActivity.class));
//
//            finish();
//
//        }

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
