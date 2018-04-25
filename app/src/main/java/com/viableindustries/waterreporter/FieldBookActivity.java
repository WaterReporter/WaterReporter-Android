package com.viableindustries.waterreporter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.models.campaign.CampaignFormField;
import com.viableindustries.waterreporter.api.models.field_book.FieldBook;
import com.viableindustries.waterreporter.api.models.field_book.FieldBookListResponse;
import com.viableindustries.waterreporter.api.models.field_book.FieldBookPatchBody;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.user.User;
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

    @Bind(R.id.saveFieldBookIcon)
    ImageView saveFieldBookIcon;

    private boolean mEditMode = false;

    private FieldBook mFieldBook;

    private Report mPost;

    private List<CampaignFormField> fieldList = new ArrayList<>();

    private SharedPreferences mSharedPreferences;

    private SharedPreferences mFieldBookEntries;

    private SharedPreferences mCoreProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_field_book);

        ButterKnife.bind(this);

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        mFieldBookEntries = getSharedPreferences(getString(R.string.field_book_entries_key), 0);

        mCoreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

        mEditMode = getIntent().getExtras().getBoolean("EDIT_MODE");

        // Clear any stored field book values

        resetFieldBookStorage();

        // Retrieve stored Report

        retrieveStoredPost();

        // Set color filter on save icon

        saveFieldBookIcon.setColorFilter(ContextCompat.getColor(this, R.color.splash_blue), PorterDuff.Mode.SRC_ATOP);

    }

    private boolean authUserOwnsPost() {

        User authUser = ModelStorage.getStoredUser(mCoreProfile, "auth_user");

        Log.v("AUTH_USER", authUser.id + "");

        Log.v("AUTH_USER_COMPARE", mFieldBook.owner.properties.id + "");

        if (authUser.id == mFieldBook.owner.properties.id) {
            
            return true;

        }
        
        return false;

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

        //
        // Populating a LinearLayout here rather than a ListView
        //

        CampaignFormFieldListAdapter mCampaignFormFieldListAdapter;
        
        if (authUserOwnsPost()) {

            mCampaignFormFieldListAdapter = new CampaignFormFieldListAdapter(this, campaignFormFields);

        } else {

            mCampaignFormFieldListAdapter = new CampaignFormFieldListAdapter(this, false, campaignFormFields);
            
        }

        final int adapterCount = mCampaignFormFieldListAdapter.getCount();

        Log.v("FIELD_COUNT", adapterCount + "");

        for (int i = 0; i < adapterCount; i++) {

            View item = mCampaignFormFieldListAdapter.getView(i, null, formFieldContainer);

            formFieldContainer.addView(item);

        }

    }

    private void storeExistingValues(List<CampaignFormField> campaignFormFields) {

        for (CampaignFormField campaignFormField : campaignFormFields) {

            if (campaignFormField.value != null &&
                    !campaignFormField.value.toString().isEmpty()) {

                mFieldBookEntries
                        .edit()
                        .putString(
                                campaignFormField.name,
                                campaignFormField.value.toString()
                        )
                        .apply();

            }

        }

    }

    private void returnToTimeline() {

        //
        // Return to the previous view. If the user
        // arrived here after creating a post,
        // re-direct to the main timeline.
        //

        if (mEditMode) {

            finish();

        } else {

            startActivity(new Intent(this, MainActivity.class));

        }

    }

    private void fetchFieldBookData(int postId) {

        final SharedPreferences mSharedPreferences =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getReportService().getFieldBookData(accessToken, "application/json", postId, new Callback<FieldBookListResponse>() {

            @Override
            public void success(FieldBookListResponse fieldBookListResponse, Response response) {

                // Set value of mFieldBook variable

                mFieldBook = fieldBookListResponse.features.get(0);

                // Set value of form field list

                fieldList = mFieldBook.data;

                // Loop through form fields and place existing values
                // in temporary storage.

                storeExistingValues(fieldList);

                // Check field book feature ownership against the
                // authenticated user to determine write permission.
                
                if (authUserOwnsPost()) {

                    saveFieldBook.setVisibility(View.VISIBLE);

                    saveFieldBook.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            stageFieldBookData();
                        }
                    });

                }

                // Render form UI elements

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

        // Retrieve stored Post

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

        if (mEditMode) {

            finish();

        } else {

            startActivity(new Intent(this, MainActivity.class));

        }

    }

}
