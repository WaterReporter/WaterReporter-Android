package com.viableindustries.waterreporter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.models.notification.NotificationSetting;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.api.models.user.UserProperties;
import com.viableindustries.waterreporter.user_interface.adapters.NotificationSettingAdapter;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ProfileSettingsActivity extends AppCompatActivity {

    @Bind(R.id.edit_profile)
    TextView editProfile;

    @Bind(R.id.manage_groups)
    TextView manageGroups;

    @Bind(R.id.report_bug)
    TextView reportBug;

    @Bind(R.id.log_out)
    TextView logOut;

    @Bind(R.id.notification_settings)
    LinearLayout notificationSettingsContainer;

    private SharedPreferences mSharedPreferences;

    private SharedPreferences coreProfile;

    private SharedPreferences groupMembership;

    private SharedPreferences associatedGroups;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_settings);

        ButterKnife.bind(this);

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

        groupMembership = getSharedPreferences(getString(R.string.group_membership_key), 0);

        associatedGroups = getSharedPreferences(getString(R.string.associated_group_key), 0);

        // Retrieve stored User

        retrieveStoredUser();

    }

    private void retrieveStoredUser() {

        user = ModelStorage.getStoredUser(coreProfile, "auth_user");

        try {

            int userId = user.properties.id;

            refreshAccount(user.id);

        } catch (NullPointerException _e) {

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

    }

    private void onRequestError(RetrofitError error) {

        Response response = error.getResponse();

        if (response != null) {

            int status = response.getStatus();

            if (status == 403) {

                startActivity(new Intent(ProfileSettingsActivity.this, SignInActivity.class));

            }

        }

    }

    private void refreshAccount(int userId) {

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getUserService().getUser(accessToken,
                "application/json",
                userId,
                new Callback<User>() {
                    @Override
                    public void success(User user,
                                        Response response) {

                        // Set flag confirming successful sign-in

                        mSharedPreferences.edit().putBoolean("clean_slate", true).apply();

                        final SharedPreferences coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

                        ModelStorage.storeModel(coreProfile, user, "auth_user");

                        configureNotificationSettings(user);

                    }

                    @Override
                    public void failure(RetrofitError error) {

                        onRequestError(error);

                    }
                });

    }

    private void configureNotificationSettings(User user) {

        String[] notificationFields;

        Map<String, Boolean> userNotificationSettings = user.properties.getNotificationProperties();

        if (user.properties.isAdmin()) {

            notificationFields = user.properties.getAdminNotificationSettingFields();

        } else {

            notificationFields = user.properties.getNotificationSettingFields();

        }

        List<NotificationSetting> currentNotificationSettings = new ArrayList<>();

        for (String field : notificationFields) {

            int settingId = getResources().getIdentifier(field, "string",
                    getPackageName());

            String description = getResources().getString(settingId);

            currentNotificationSettings.add(
                    new NotificationSetting(
                            field, description, userNotificationSettings.get(field)
                    )
            );

        }

        NotificationSettingAdapter notificationSettingAdapter = new NotificationSettingAdapter(ProfileSettingsActivity.this, currentNotificationSettings);

        final int adapterCount = notificationSettingAdapter.getCount();

        for (int i = 0; i < adapterCount; i++) {

            View item = notificationSettingAdapter.getView(i, null, notificationSettingsContainer);

            notificationSettingsContainer.addView(item);

        }

    }

    public void editProfile(View view) {

        startActivity(new Intent(this, EditProfileActivity.class));

    }

    public void manageGroups(View view) {

        startActivity(new Intent(this, GroupActionListActivity.class));

    }

    public void logOut(View view) {

        // Clear stored token and user id values

        mSharedPreferences.edit().clear().apply();

        // Clear stored active user profile

        coreProfile.edit().clear().apply();

        // Clear stored group memberships

        groupMembership.edit().clear().apply();

        // Clear stored group memberships available to report tagging

        associatedGroups.edit().clear().apply();

        Intent logOutIntent = new Intent(this, SignInActivity.class);

        logOutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(logOutIntent);

        finish();

    }

    public void composeEmail(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@waterreporter.org"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Water Reporter for Android");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void readTerms(View view) {

        startActivity(new Intent(this, TermsActivity.class));

    }

    public void readPrivacy(View view) {

        startActivity(new Intent(this, PrivacyActivity.class));

    }

    // Back button should always go to authenticated user's account profile

    @Override
    public void onBackPressed() {

        // Cancel all pending network requests

        //Callback.cancelAll();

        startActivity(new Intent(this, AuthUserActivity.class));

    }

    @Override
    public void onResume() {

        super.onResume();

        // Retrieve stored User

        if (user == null) retrieveStoredUser();

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

