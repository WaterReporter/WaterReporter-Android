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
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.snapshot.SnapshotCampaignList;
import com.viableindustries.waterreporter.api.models.snapshot.SnapshotShallowCampaign;
import com.viableindustries.waterreporter.user_interface.adapters.SnapshotShallowCampaignListAdapter;
import com.viableindustries.waterreporter.utilities.EndlessScrollListener;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class OrganizationCampaignsActivity extends AppCompatActivity {

    @Bind(R.id.listViewContainer)
    SwipeRefreshLayout listViewContainer;

    @Bind(R.id.listView)
    ListView listView;

    private Organization mOrganization;

    private List<SnapshotShallowCampaign> campaignList = new ArrayList<>();

    private SnapshotShallowCampaignListAdapter mOrganizationCampaignListAdapter;

    private SharedPreferences mSharedPreferences;

    private Context mContext;

    private int mOrganizationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_organization_campaigns);

        ButterKnife.bind(this);

        mContext = this;

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        // Retrieve stored Organization

        retrieveStoredOrganization();

        // Set refresh listener on report feed container

        listViewContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual api-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        fetchOrganizationCampaigns(1, mOrganization.id, true);
                    }
                }
        );

        // Set color of swipe refresh arrow animation

        listViewContainer.setColorSchemeResources(R.color.waterreporter_blue);

    }

    private void retrieveStoredOrganization() {

        mOrganization = ModelStorage.getStoredOrganization(mSharedPreferences);

        try {

            mOrganizationId = mOrganization.properties.id;

            fetchOrganizationCampaigns(1, mOrganizationId, true);

        } catch (NullPointerException e1) {

            try {

                mOrganizationId = mOrganization.id;

                fetchOrganizationCampaigns(1, mOrganizationId, true);

            } catch (NullPointerException e2) {

                Log.v("NO-STORED-GROUP", e2.toString());

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

    private void populateList(List<SnapshotShallowCampaign> snapshotShallowCampaigns) {

        mOrganizationCampaignListAdapter = new SnapshotShallowCampaignListAdapter(this, snapshotShallowCampaigns, getSupportFragmentManager());

        try {

            listView.setAdapter(mOrganizationCampaignListAdapter);

            attachScrollListener();

        } catch (NullPointerException e) {

            finish();

        }

    }

    private void attachScrollListener() {

        listView.setOnScrollListener(new EndlessScrollListener() {

            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new api needs to be appended to the list

                fetchOrganizationCampaigns(page, mOrganization.id, false);

                return true; // ONLY if more api is actually being loaded; false otherwise.

            }

        });

    }

    private void fetchOrganizationCampaigns(int page, int mOrganizationId, final boolean refresh) {

        final SharedPreferences mSharedPreferences =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getSnapshotService().getOrganizationCampaigns(accessToken, "application/json", page, mOrganizationId, new Callback<SnapshotCampaignList>() {

            @Override
            public void success(SnapshotCampaignList snapshotCampaignList, Response response) {

                List<SnapshotShallowCampaign> snapshotShallowCampaigns = snapshotCampaignList.campaigns;

                if (refresh) {

                    campaignList = snapshotShallowCampaigns;

                    populateList(campaignList);

                } else {

                    campaignList.addAll(snapshotShallowCampaigns);

                    mOrganizationCampaignListAdapter.notifyDataSetChanged();

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

        // Retrieve stored organization

        if (mOrganization == null) {

            retrieveStoredOrganization();

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