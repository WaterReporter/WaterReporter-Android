package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.OrganizationService;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.Report;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GroupActionListActivity extends AppCompatActivity {

    @Bind(R.id.organizationListContainer)
    SwipeRefreshLayout organizationListContainer;

    @Bind(R.id.skipAhead)
    ImageButton skipAhead;

    @Bind(R.id.search_box)
    EditText listFilter;

    @Bind(R.id.organizationList)
    ListView listView;

    ArrayList<Organization> organizations;

    GroupActionListAdapter adapter;

    private boolean isRegistrationFlow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_action_list);

        ButterKnife.bind(this);

        // Determine if we need "forward" navigation

        Bundle extras = getIntent().getExtras();

        if (extras != null && extras.getBoolean("POST_REGISTER", false)) {

            skipAhead.setVisibility(View.VISIBLE);

        }

        // Initialize empty list to hold organizations

        organizations = new ArrayList<Organization>();

        organizationListContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        buildList(1, 20, true);

                    }
                }
        );

        // Set color of swipe refresh arrow animation

        organizationListContainer.setColorSchemeResources(R.color.waterreporter_blue);

        // Fetch organizations

        organizationListContainer.setRefreshing(true);

        buildList(1, 20, true);

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

    private void attachScrollListener() {

        try {

            listView.setOnScrollListener(new EndlessScrollListener() {
                @Override
                public boolean onLoadMore(int page, int totalItemsCount) {

                    // Triggered only when new data needs to be appended to the list

                    buildList(page, 20, false);

                    return true; // ONLY if more data is actually being loaded; false otherwise.

                }
            });

        } catch (NullPointerException e) {

            finish();

        }

    }

    private void buildList(int page, int limit, final boolean refresh) {

        SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String accessToken = prefs.getString("access_token", "");

        OrganizationService service = OrganizationService.restAdapter.create(OrganizationService.class);

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<QuerySort>();

        QuerySort querySort = new QuerySort("name", "asc");

        queryOrder.add(querySort);

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(null, queryOrder);

        String query = new Gson().toJson(queryParams);

        Log.d("URL", query);

        service.getOrganizations(accessToken, "application/json", page, limit, query, new Callback<OrganizationFeatureCollection>() {

            @Override
            public void success(OrganizationFeatureCollection organizationCollectionResponse, Response response) {

                ArrayList<Organization> features = organizationCollectionResponse.getFeatures();

                Log.v("list", features.toString());

                if (refresh) {

                    organizations = features;

                    populateOrganizations(organizations);

                } else {

                    organizations.addAll(features);

                    try {

                        adapter.notifyDataSetChanged();

                    } catch (NullPointerException ne) {

                        populateOrganizations(organizations);

                    }

                }

                try {

                    organizationListContainer.setRefreshing(false);

                } catch (NullPointerException e) {

                    finish();

                }

            }

            @Override
            public void failure(RetrofitError error) {

                try {

                    organizationListContainer.setRefreshing(false);

                } catch (NullPointerException e) {

                    finish();

                }

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(GroupActionListActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void populateOrganizations(ArrayList<Organization> orgs) {

        adapter = new GroupActionListAdapter(this, orgs, true);

        try {

            listFilter.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    Log.d("filter", s.toString());

                    adapter.getFilter().filter(s.toString());

                }

            });

            listView.setAdapter(adapter);

            // Enable ListView filtering

            listView.setTextFilterEnabled(true);

        } catch (NullPointerException e) {

            finish();

        }

        attachScrollListener();

    }

    public void toFeed(View view) {

        startActivity(new Intent(this, MainActivity.class));

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
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

    }

}
