package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.models.campaign.CampaignFormField;
import com.viableindustries.waterreporter.api.models.field_book.FieldBook;
import com.viableindustries.waterreporter.api.models.field_book.FieldBookListResponse;
import com.viableindustries.waterreporter.api.models.field_book.FieldBookPatchBody;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.user_interface.adapters.CampaignFormFieldListAdapter;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class FieldBookActivity extends AppCompatActivity {

    @Bind(R.id.formFieldContainer)
    LinearLayout formFieldContainer;

    @Bind(R.id.saveFieldBook)
    RelativeLayout saveFieldBook;

    private FieldBook mFieldBook;

    private Report mPost;

    private List<CampaignFormField> fieldList = new ArrayList<>();

    private CampaignFormFieldListAdapter mCampaignFormFieldListAdapter;

    private SharedPreferences mSharedPreferences;

    private SharedPreferences mFieldBookEntries;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_campaign_form);

        ButterKnife.bind(this);

        mContext = this;

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        mFieldBookEntries = getSharedPreferences(getString(R.string.field_book_entries_key), 0);

        // Clear any stored field book values

        resetFieldBookStorage();

        // Retrieve stored Report

        retrieveStoredPost();

        // Set click listeners

        saveFieldBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stageFieldBookData();
            }
        });

    }

    private void resetFieldBookStorage() {

        mFieldBookEntries.edit().clear().apply();

    }

    private void retrieveStoredPost() {

        mPost = ModelStorage.getStoredPost(mSharedPreferences);

        try {

            fetchFieldBookData(mPost.properties.id);

        } catch (NullPointerException _e) {

            returnToTimeline();

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

    private void populateFields(List<CampaignFormField> campaignFormFields) {

        // Populating a LinearLayout here rather than a ListView

        mCampaignFormFieldListAdapter = new CampaignFormFieldListAdapter(this, campaignFormFields);

        final int adapterCount = mCampaignFormFieldListAdapter.getCount();

        Log.v("FIELD_COUNT", adapterCount + "");

        for (int i = 0; i < adapterCount; i++) {

            View item = mCampaignFormFieldListAdapter.getView(i, null, formFieldContainer);

            formFieldContainer.addView(item);

        }

    }

    private void returnToTimeline() {

        // Re-direct user to main activity feed, which has the effect of preventing
        // unwanted access to the history stack

        Intent intent = new Intent(FieldBookActivity.this, MainActivity.class);

        startActivity(intent);

        finish();

    }

    private void fetchFieldBookData(int postId) {

        final SharedPreferences mSharedPreferences =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getReportService().getFieldBookData(accessToken, "application/json", postId, new Callback<FieldBookListResponse>() {

            @Override
            public void success(FieldBookListResponse fieldBookListResponse, Response response) {

                fieldList = fieldBookListResponse.features.get(0).data;

                populateFields(fieldList);

            }

            @Override
            public void failure(RetrofitError error) {

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(FieldBookActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void stageFieldBookData() {

        FieldBookPatchBody fieldBookPatchBody = new FieldBookPatchBody();

        for (CampaignFormField campaignFormField : fieldList) {

            String storedValue = mFieldBookEntries.getString(campaignFormField.name, "");

            if (!storedValue.isEmpty()) {

                campaignFormField.value = storedValue;

            } else {

                campaignFormField.value = storedValue;

            }

        }

        fieldBookPatchBody.data = fieldList;

        updateFieldBookEntry(fieldBookPatchBody);

    }

    private void updateFieldBookEntry(FieldBookPatchBody fieldBookPatchBody) {

        String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getFieldBookService().updateSingle(accessToken, "application/json", mFieldBook.id, fieldBookPatchBody,
                new Callback<Response>() {
                    @Override
                    public void success(Response result, Response response) {

                        returnToTimeline();

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        onPostError();
                    }

                });

    }

    private void onPostError() {

        CharSequence text =
                "Error posting field book data. Please try again later.";

        Snackbar.make(formFieldContainer, text,
                Snackbar.LENGTH_SHORT)
                .show();

    }

    @Override
    public void onResume() {

        super.onResume();

        // Retrieve stored Organization

        if (mPost == null) {

            retrieveStoredPost();

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

        resetFieldBookStorage();

    }

    @Override
    public void onBackPressed() {

        resetFieldBookStorage();

        startActivity(new Intent(this, MainActivity.class));

    }

}
