package com.viableindustries.waterreporter;

import android.content.Context;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.models.campaign.Campaign;
import com.viableindustries.waterreporter.api.models.campaign.CampaignFormField;
import com.viableindustries.waterreporter.api.models.campaign.CampaignFormResponse;
import com.viableindustries.waterreporter.api.models.field_book.FieldBookPostBody;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.user_interface.adapters.CampaignFormFieldListAdapter;
import com.viableindustries.waterreporter.utilities.CircleTransform;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CampaignFormActivity extends AppCompatActivity {

    @Bind(R.id.formFieldContainer)
    LinearLayout formFieldContainer;

    @Bind(R.id.saveFieldBook)
    RelativeLayout saveFieldBook;

    @Bind(R.id.saveFieldBookIcon)
    ImageView saveFieldBookIcon;

    @Bind(R.id.formSuccessMessage)
    RelativeLayout formSuccessMessage;

    @Bind(R.id.userAvatar)
    ImageView userAvatar;

    @Bind(R.id.userName)
    TextView userName;

    @Bind(R.id.organizationName)
    TextView organizationName;

    @Bind(R.id.thankYouMessage)
    TextView thankYouMessage;

    @Bind(R.id.dismissMessage)
    Button dismissMessage;

    private Campaign mCampaign;

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

        // Retrieve stored Campaign

        retrieveStoredCampaign();

        // Retrieve stored Report

        retrieveStoredPost();

        // Set click listeners

        saveFieldBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stageFieldBookData();
            }
        });

        dismissMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToTimeline();
            }
        });

        // Set color filter on save icon

        saveFieldBookIcon.setColorFilter(ContextCompat.getColor(this, R.color.splash_blue), PorterDuff.Mode.SRC_ATOP);

    }

    private void populateThankYouMessage() {

        Random randomizer = new Random();

        List<User> organizers = mCampaign.properties.organizers;

        User organizer = organizers.get(randomizer.nextInt(organizers.size()));

        // Load group leader image

        Picasso.with(this)
                .load(organizer.properties.picture)
                .placeholder(R.drawable.user_avatar_placeholder_003)
                .transform(new CircleTransform()).into(userAvatar);

        // Set group leader name

        String userNameText = String.format("%s %s",
                organizer.properties.first_name,
                organizer.properties.last_name);

        userName.setText(userNameText);

        // Set organization name

        String organizationNameText = mCampaign.properties.organizations.get(0).properties.name;

        organizationName.setText(organizationNameText);

        // Set thank you text

        final SharedPreferences coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

        User authUser = ModelStorage.getStoredUser(coreProfile, "auth_user");

        thankYouMessage.setText(getString(R.string.default_form_thank_you_message, authUser.properties.first_name.trim()));

    }

    private void resetFieldBookStorage() {

        mFieldBookEntries.edit().clear().apply();

    }

    private void retrieveStoredPost() {

        mPost = ModelStorage.getStoredPost(mSharedPreferences);

        try {

            String postProperties = mPost.properties.toString();

        } catch (NullPointerException _e) {

            returnToTimeline();

        }

    }

    private void retrieveStoredCampaign() {

        mCampaign = ModelStorage.getStoredCampaign(mSharedPreferences);

        try {

            int campaignId = mCampaign.properties.id;

            populateThankYouMessage();

            fetchCampaignFormFields(1, campaignId, true);

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

    private void presentThankYou() {

        formFieldContainer.removeAllViews();

        formSuccessMessage.requestLayout();

        formSuccessMessage.setVisibility(View.VISIBLE);

    }

    private void returnToTimeline() {

        // Re-direct user to main activity feed, which has the effect of preventing
        // unwanted access to the history stack

        Intent intent = new Intent(CampaignFormActivity.this, MainActivity.class);

        startActivity(intent);

        finish();

    }

    private void fetchCampaignFormFields(int page, int mCampaignId, final boolean refresh) {

        final SharedPreferences mSharedPreferences =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getCampaignService().getCampaignForm(accessToken, "application/json", mCampaignId, new Callback<CampaignFormResponse>() {

            @Override
            public void success(CampaignFormResponse campaignFormResponse, Response response) {

                fieldList = campaignFormResponse.properties.fields;

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

                        startActivity(new Intent(CampaignFormActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void stageFieldBookData() {

        FieldBookPostBody fieldBookPostBody = new FieldBookPostBody();

        fieldBookPostBody.report_id = mPost.properties.id;

        for (CampaignFormField campaignFormField : fieldList) {

            String storedValue = mFieldBookEntries.getString(campaignFormField.name, "");

            if (!storedValue.isEmpty()) {

                campaignFormField.value = storedValue;

            } else {

                campaignFormField.value = storedValue;

            }

        }

        fieldBookPostBody.data = fieldList;

        saveFieldBookEntry(fieldBookPostBody);

    }

    private void saveFieldBookEntry(FieldBookPostBody fieldBookPostBody) {

        String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getFieldBookService().postFieldBook(accessToken, "application/json", fieldBookPostBody,
                new Callback<Response>() {
                    @Override
                    public void success(Response result, Response response) {

                        presentThankYou();

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

        if (mCampaign == null) {

            retrieveStoredCampaign();

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
