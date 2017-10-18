package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.gson.Gson;
import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.organization.OrganizationMemberList;
import com.viableindustries.waterreporter.api.models.query.QueryFilter;
import com.viableindustries.waterreporter.api.models.query.QueryParams;
import com.viableindustries.waterreporter.api.models.query.QuerySort;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.api.models.user.UserCollection;
import com.viableindustries.waterreporter.user_interface.adapters.UserListAdapter;
import com.viableindustries.waterreporter.utilities.EndlessScrollListener;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class OrganizationMembersActivity extends AppCompatActivity {

    @Bind(R.id.memberListContainer)
    SwipeRefreshLayout memberListContainer;

    @Bind(R.id.memberList)
    ListView memberList;

    private Organization organization;

    private List<User> memberCollection = new ArrayList<>();

    private UserListAdapter userListAdapter;

    private SharedPreferences mSharedPreferences;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_organization_members);

        ButterKnife.bind(this);

        mContext = this;

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        // Retrieve stored Organization

        retrieveStoredOrganization();

        memberCollection = OrganizationMemberList.getList();

        Collections.sort(memberCollection, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.properties.last_name.compareTo(user2.properties.last_name);
            }
        });

        // Set refresh listener on report feed container

        memberListContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual api-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        fetchOrganizationMembers(50, 1, organization.id, null, true);
                    }
                }
        );

        // Set color of swipe refresh arrow animation

        memberListContainer.setColorSchemeResources(R.color.waterreporter_blue);

        populateUsers(memberCollection);


    }

    private void retrieveStoredOrganization() {

        organization = ModelStorage.getStoredGroup(mSharedPreferences);

        fetchOrganizationMembers(50, 1, organization.id, null, true);

        try {

            int orgId = organization.properties.id;

        } catch (NullPointerException _e) {

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

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

    private void populateUsers(List<User> users) {

        userListAdapter = new UserListAdapter(this, users);

        memberList.setAdapter(userListAdapter);

        attachScrollListener();

    }

    private void attachScrollListener() {

        memberList.setOnScrollListener(new EndlessScrollListener() {

            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new api needs to be appended to the list

                fetchOrganizationMembers(50, page, organization.id, null, false);

                return true; // ONLY if more api is actually being loaded; false otherwise.

            }

        });

    }

    private String buildQuery() {

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<>();

        QuerySort querySort = new QuerySort("last_name", "asc");

        queryOrder.add(querySort);

        // Create filter list and add a filter parameter

        List<Object> queryFilters = new ArrayList<>();

        QueryFilter complexVal = new QueryFilter("id", "eq", organization.id);

        QueryFilter userFilter = new QueryFilter("organization", "any", complexVal);

        queryFilters.add(userFilter);

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(queryFilters, queryOrder);

        return new Gson().toJson(queryParams);

    }

    private void fetchOrganizationMembers(int limit, int page, int organizationId, final String query, final boolean refresh) {

        final SharedPreferences mSharedPreferences =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getOrganizationService().getOrganizationMembers(accessToken, "application/json", organizationId, page, limit, query, new Callback<UserCollection>() {

            @Override
            public void success(UserCollection userCollection, Response response) {

                ArrayList<User> members = userCollection.getFeatures();

                if (!members.isEmpty()) {

                    memberCollection.addAll(members);

                    Collections.sort(memberCollection, new Comparator<User>() {
                        @Override
                        public int compare(User user1, User user2) {
                            return user1.properties.last_name.compareTo(user2.properties.last_name);
                        }
                    });

                    userListAdapter.notifyDataSetChanged();

                }

                if (refresh) {

                    OrganizationMemberList.setList(members);

                    memberCollection = members;

                    Collections.sort(memberCollection, new Comparator<User>() {
                        @Override
                        public int compare(User user1, User user2) {
                            return user1.properties.last_name.compareTo(user2.properties.last_name);
                        }
                    });

                    populateUsers(memberCollection);

                }

                memberListContainer.setRefreshing(false);

            }

            @Override
            public void failure(RetrofitError error) {

                memberListContainer.setRefreshing(false);

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(OrganizationMembersActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    @Override
    public void onResume() {

        super.onResume();

        // Retrieve stored Organization

        retrieveStoredOrganization();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

        // Cancel all pending network requests

        //Callback.cancelAll();

    }

}