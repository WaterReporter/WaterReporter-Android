package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.viableindustries.waterreporter.data.Comment;
import com.viableindustries.waterreporter.data.CommentCollection;
import com.viableindustries.waterreporter.data.FeatureCollection;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.OrganizationMemberList;
import com.viableindustries.waterreporter.data.OrganizationService;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserGroupList;
import com.viableindustries.waterreporter.data.UserService;

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

import static com.viableindustries.waterreporter.data.ReportService.restAdapter;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class CommentActivity extends AppCompatActivity {

//    @Bind(R.id.search_box)
//    EditText listFilter;

    @Bind(R.id.list)
    ListView listView;

    private int reportId;

    protected CommentAdapter commentAdapter;

    protected List<Comment> commentCollectionList = new ArrayList<Comment>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comment);

        ButterKnife.bind(this);

//        generic = getIntent().getExtras().getBoolean("GENERIC_USER", TRUE);

        //assert getSupportActionBar() != null;
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        ArrayList<Organization> organizations = UserGroupList.getList();
//
//        Collections.sort(organizations, new Comparator<Organization>() {
//            @Override
//            public int compare(Organization organization1, Organization organization2) {
//                return organization1.properties.name.compareTo(organization2.properties.name);
//            }
//        });

        fetchComments(10, 1);

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

        // We shouldn't need to retrieve this value again, but we'll deal with that issue later
//        user_id = prefs.getInt("user_id", 0);

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

                if (!comments.isEmpty()) {

                    commentCollectionList.addAll(comments);

                    try {

                        commentAdapter.notifyDataSetChanged();

                    } catch (NullPointerException ne) {

                        populateComments(commentCollectionList);

                    }

                }

                commentCollectionList = comments;

//                if (refresh) {
//
//                    reportCollection = reports;
//
//                    populateTimeline(reportCollection);
//
//                }

//                listView.setRefreshing(false);

            }

            @Override
            public void failure(RetrofitError error) {

//                timeline.setRefreshing(false);

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

//        if (!generic) {
//
//            listFilter.setVisibility(View.VISIBLE);
//
//            listFilter.addTextChangedListener(new TextWatcher() {
//
//                public void afterTextChanged(Editable s) {
//                }
//
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                }
//
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                    Log.d("filter", s.toString());
//
//                    adapter.getFilter().filter(s.toString());
//
//                }
//
//            });
//
//            // Enable ListView filtering
//
//            listView.setTextFilterEnabled(true);
//
//        }

    }

}
