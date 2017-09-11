package com.viableindustries.waterreporter;

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
import com.viableindustries.waterreporter.data.CancelableCallback;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationHolder;
import com.viableindustries.waterreporter.data.OrganizationMemberList;
import com.viableindustries.waterreporter.data.OrganizationService;
import com.viableindustries.waterreporter.data.QueryFilter;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class OrganizationMembersActivity extends AppCompatActivity {

    @Bind(R.id.memberListContainer)
    private final
    SwipeRefreshLayout memberListContainer;

    @Bind(R.id.memberList)
    private final
    ListView memberList;

    private Organization organization;

    private List<User> memberCollection = new ArrayList<>();

    private UserListAdapter userListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_organization_members);

        ButterKnife.bind(this);

        organization = OrganizationHolder.getOrganization();

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
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        fetchOrganizationMembers(50, 1, organization.id, null, true);
                    }
                }
        );

        // Set color of swipe refresh arrow animation

        memberListContainer.setColorSchemeResources(R.color.waterreporter_blue);

        populateUsers(memberCollection);


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

        userListAdapter = new UserListAdapter(this, users, true);

        memberList.setAdapter(userListAdapter);

        attachScrollListener();

    }

    private void attachScrollListener() {

        memberList.setOnScrollListener(new EndlessScrollListener() {

            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new data needs to be appended to the list

                fetchOrganizationMembers(50, page, organization.id, null, false);

                return true; // ONLY if more data is actually being loaded; false otherwise.

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

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        OrganizationService service = OrganizationService.restAdapter.create(OrganizationService.class);

        service.getOrganizationMembers(accessToken, "application/json", organizationId, page, limit, query, new CancelableCallback<UserCollection>() {

            @Override
            public void onSuccess(UserCollection userCollection, Response response) {

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
            public void onFailure(RetrofitError error) {

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

        CancelableCallback.cancelAll();

    }

}
