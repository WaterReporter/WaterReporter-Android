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
import com.viableindustries.waterreporter.api.models.snapshot.SnapshotMemberList;
import com.viableindustries.waterreporter.api.models.snapshot.SnapshotShallowUser;
import com.viableindustries.waterreporter.api.models.territory.Territory;
import com.viableindustries.waterreporter.user_interface.adapters.SnapshotMemberListAdapter;
import com.viableindustries.waterreporter.utilities.EndlessScrollListener;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class WatershedUsersActivity extends AppCompatActivity {

    @Bind(R.id.listViewContainer)
    SwipeRefreshLayout listViewContainer;

    @Bind(R.id.listView)
    ListView listView;

    private Territory mTerritory;

    private List<SnapshotShallowUser> memberList = new ArrayList<>();

    private SnapshotMemberListAdapter mSnapshotShallowUserListAdapter;

    private SharedPreferences mSharedPreferences;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_watershed_users);

        ButterKnife.bind(this);

        mContext = this;

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        // Retrieve stored Territory

        retrieveStoredTerritory();

        // Set refresh listener on report feed container

        listViewContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual api-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        fetchTerritoryUsers(1, mTerritory.id, true);
                    }
                }
        );

        // Set color of swipe refresh arrow animation

        listViewContainer.setColorSchemeResources(R.color.waterreporter_blue);

    }

    private void retrieveStoredTerritory() {

        mTerritory = ModelStorage.getStoredTerritory(mSharedPreferences);

        try {

            fetchTerritoryUsers(1, mTerritory.properties.huc_8_code, true);

        } catch (NullPointerException e1) {

            try {

                Log.d("ID ONLY", "proceed to load profile data");

                fetchTerritoryUsers(1, mTerritory.id, true);

            } catch (NullPointerException e2) {

                Log.v("NO-STORED-TERRITORY", e2.toString());

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

    private void populateList(List<SnapshotShallowUser> territoryMembers) {

        mSnapshotShallowUserListAdapter = new SnapshotMemberListAdapter(this, territoryMembers, getSupportFragmentManager());

        listView.setAdapter(mSnapshotShallowUserListAdapter);

        attachScrollListener();

    }

    private void attachScrollListener() {

        listView.setOnScrollListener(new EndlessScrollListener() {

            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new api needs to be appended to the list

                fetchTerritoryUsers(page, mTerritory.id, false);

                return true; // ONLY if more api is actually being loaded; false otherwise.

            }

        });

    }

    private void fetchTerritoryUsers(int page, int territoryId, final boolean refresh) {

        final SharedPreferences mSharedPreferences =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getSnapshotService().getWatershedUsers(accessToken, "application/json", page, territoryId, new Callback<SnapshotMemberList>() {

            @Override
            public void success(SnapshotMemberList territoryMemberList, Response response) {

                List<SnapshotShallowUser> groups = territoryMemberList.members;

                if (refresh) {

                    memberList = groups;

                    populateList(memberList);

                } else {

                    memberList.addAll(groups);

                    mSnapshotShallowUserListAdapter.notifyDataSetChanged();

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

        // Retrieve stored Territory

        if (mTerritory == null) {

            retrieveStoredTerritory();

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
