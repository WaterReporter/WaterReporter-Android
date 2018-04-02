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

import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.models.snapshot.SnapshotGroupList;
import com.viableindustries.waterreporter.api.models.snapshot.SnapshotShallowGroup;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.user_interface.adapters.SnapshotGroupListAdapter;
import com.viableindustries.waterreporter.utilities.EndlessScrollListener;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UserGroupsActivity extends AppCompatActivity {

    @Bind(R.id.listViewContainer)
    SwipeRefreshLayout listViewContainer;

    @Bind(R.id.listView)
    ListView listView;

    private User mUser;

    private int mUserId;

    private List<SnapshotShallowGroup> groupList = new ArrayList<>();

    private SnapshotGroupListAdapter mUserGroupListAdapter;

    private SharedPreferences mSharedPreferences;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_groups);

        ButterKnife.bind(this);

        mContext = this;

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        // Retrieve stored User

        retrieveStoredUser();

        // Set refresh listener on report feed container

        listViewContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual api-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        fetchUserGroups(1, mUser.id, true);
                    }
                }
        );

        // Set color of swipe refresh arrow animation

        listViewContainer.setColorSchemeResources(R.color.waterreporter_blue);

    }

    private void retrieveStoredUser() {

        mUser = ModelStorage.getStoredUser(mSharedPreferences, "stored_user");

        try {

            mUserId = mUser.properties.id;

            fetchUserGroups(1, mUserId, true);

        } catch (NullPointerException e1) {

            try {

                Log.d("USER ID ONLY", "proceed to load profile data");

                mUserId = mUser.id;

                fetchUserGroups(1, mUserId, true);

            } catch (NullPointerException e2) {

                Log.v("NO-STORED-USER", e2.toString());

                startActivity(new Intent(this, MainActivity.class));

                finish();

            }

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

    private void populateList(List<SnapshotShallowGroup> campaignGroups) {

        mUserGroupListAdapter = new SnapshotGroupListAdapter(this, campaignGroups, getSupportFragmentManager());

        listView.setAdapter(mUserGroupListAdapter);

        attachScrollListener();

    }

    private void attachScrollListener() {

        listView.setOnScrollListener(new EndlessScrollListener() {

            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new api needs to be appended to the list

                fetchUserGroups(page, mUser.id, false);

                return true; // ONLY if more api is actually being loaded; false otherwise.

            }

        });

    }

    private void fetchUserGroups(int page, int mUserId, final boolean refresh) {

        final SharedPreferences mSharedPreferences =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getSnapshotService().getUserGroups(accessToken, "application/json", page, mUserId, new Callback<SnapshotGroupList>() {

            @Override
            public void success(SnapshotGroupList campaignGroupList, Response response) {

                List<SnapshotShallowGroup> groups = campaignGroupList.groups;

                if (refresh) {

                    groupList = groups;

                    populateList(groupList);

                } else {

                    groupList.addAll(groups);

                    mUserGroupListAdapter.notifyDataSetChanged();

                }

                listViewContainer.setRefreshing(false);

            }

            @Override
            public void failure(RetrofitError error) {

                listViewContainer.setRefreshing(false);

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(mContext, SignInActivity.class));

                    }

                }

            }

        });

    }

    @Override
    public void onResume() {

        super.onResume();

        // Retrieve stored user

        if (mUser == null) {

            retrieveStoredUser();

        }

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

    }

}