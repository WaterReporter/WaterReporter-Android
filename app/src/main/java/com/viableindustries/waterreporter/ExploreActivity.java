package com.viableindustries.waterreporter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.google.gson.Gson;
import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.models.campaign.Campaign;
import com.viableindustries.waterreporter.api.models.campaign.CampaignCollection;
import com.viableindustries.waterreporter.api.models.query.QueryParams;
import com.viableindustries.waterreporter.api.models.query.QuerySort;
import com.viableindustries.waterreporter.user_interface.adapters.CampaignListAdapter;
import com.viableindustries.waterreporter.utilities.EndlessScrollListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ExploreActivity extends AppCompatActivity {

    @Bind(R.id.campaignCollection)
    SwipeRefreshLayout campaignListContainer;

    @Bind(R.id.list)
    ListView listView;

    private ArrayList<Campaign> campaigns;

    private CampaignListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_explore);

        ButterKnife.bind(this);

        // Set color of swipe refresh arrow animation

        campaignListContainer.setColorSchemeResources(R.color.waterreporter_blue);

        // Set refresh listener on report feed container

        campaignListContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual api-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.

                        fetchCampaigns(1, 10, true);

                    }
                }
        );

        fetchCampaigns(1, 10, true);

    }

    private void fetchCampaigns(int page, int limit, final boolean refresh) {

        SharedPreferences mSharedPreferences =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String accessToken = mSharedPreferences.getString("access_token", "");

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<>();

        QuerySort querySort = new QuerySort("id", "desc");

        queryOrder.add(querySort);

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(null, queryOrder);

        String query = new Gson().toJson(queryParams);

        Log.d("URL", query);

        RestClient.getCampaignService().getMany(accessToken, "application/json", page, limit, query, new Callback<CampaignCollection>() {

            @Override
            public void success(CampaignCollection campaignCollection, Response response) {

                ArrayList<Campaign> features = campaignCollection.getFeatures();

                Log.v("list", features.toString());

                if (refresh) {

                    campaigns = features;

                    populateList(campaigns);

                } else {

                    campaigns.addAll(features);

                    try {

                        adapter.notifyDataSetChanged();

                    } catch (NullPointerException ne) {

                        populateList(campaigns);

                    }

                }

                try {

                    campaignListContainer.setRefreshing(false);

                } catch (NullPointerException e) {

                    finish();

                }

            }

            @Override
            public void failure(RetrofitError error) {

                try {

                    campaignListContainer.setRefreshing(false);

                } catch (NullPointerException e) {

                    finish();

                }

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(ExploreActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void attachScrollListener() {

        try {

            listView.setOnScrollListener(new EndlessScrollListener() {
                @Override
                public boolean onLoadMore(int page, int totalItemsCount) {

                    // Triggered only when new api needs to be appended to the list

                    fetchCampaigns(page, 10, false);

                    return true; // ONLY if more api is actually being loaded; false otherwise.

                }
            });

        } catch (NullPointerException e) {

            finish();

        }

    }

    private void populateList(ArrayList<Campaign> campaigns) {

        adapter = new CampaignListAdapter(this, campaigns);

        try {

            listView.setAdapter(adapter);

        } catch (NullPointerException e) {

            finish();

        }

        attachScrollListener();

    }

}