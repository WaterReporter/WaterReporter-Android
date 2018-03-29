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
import com.viableindustries.waterreporter.api.models.campaign.Campaign;
import com.viableindustries.waterreporter.api.models.campaign.CampaignFormField;
import com.viableindustries.waterreporter.api.models.campaign.CampaignFormResponse;
import com.viableindustries.waterreporter.user_interface.adapters.CampaignFormFieldListAdapter;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CampaignFormActivity extends AppCompatActivity {

    @Bind(R.id.listViewContainer)
    SwipeRefreshLayout listViewContainer;

    @Bind(R.id.listView)
    ListView listView;

    private Campaign mCampaign;

    private List<CampaignFormField> fieldList = new ArrayList<>();

    private CampaignFormFieldListAdapter mCampaignFormFieldListAdapter;

    private SharedPreferences mSharedPreferences;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_campaign_form);

        ButterKnife.bind(this);

        mContext = this;

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        // Retrieve stored Campaign

        retrieveStoredCampaign();

        // Set refresh listener on report feed container

        listViewContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual api-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        fetchCampaignFormFields(1, mCampaign.id, true);
                    }
                }
        );

        // Set color of swipe refresh arrow animation

        listViewContainer.setColorSchemeResources(R.color.waterreporter_blue);

    }

    private void retrieveStoredCampaign() {

        mCampaign = ModelStorage.getStoredCampaign(mSharedPreferences);

        try {

            int campaignId = mCampaign.properties.id;

            fetchCampaignFormFields(1, campaignId, true);

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

    private void populateList(List<CampaignFormField> campaignMembers) {

        mCampaignFormFieldListAdapter = new CampaignFormFieldListAdapter(this, campaignMembers);

        listView.setAdapter(mCampaignFormFieldListAdapter);

//        attachScrollListener();

    }

//    private void attachScrollListener() {
//
//        listView.setOnScrollListener(new EndlessScrollListener() {
//
//            @Override
//            public boolean onLoadMore(int page, int totalItemsCount) {
//
//                // Triggered only when new api needs to be appended to the list
//
//                fetchCampaignFormFields(page, mCampaign.id, false);
//
//                return true; // ONLY if more api is actually being loaded; false otherwise.
//
//            }
//
//        });
//
//    }

    private void fetchCampaignFormFields(int page, int mCampaignId, final boolean refresh) {

        final SharedPreferences mSharedPreferences =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getCampaignService().getCampaignForm(accessToken, "application/json", mCampaignId, new Callback<CampaignFormResponse>() {

            @Override
            public void success(CampaignFormResponse campaignFormResponse, Response response) {

                fieldList = campaignFormResponse.properties.fields;

                populateList(fieldList);

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

                        startActivity(new Intent(CampaignFormActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    @Override
    public void onResume() {

        super.onResume();

        // Retrieve stored Organization

        retrieveStoredCampaign();

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
