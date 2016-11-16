package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.NotificationSetting;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserHolder;
import com.viableindustries.waterreporter.data.UserProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

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

    private SharedPreferences prefs;

    private SharedPreferences coreProfile;

    protected SharedPreferences groupPrefs;

    protected SharedPreferences associatedGroups;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_settings);

        ButterKnife.bind(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

        groupPrefs = getSharedPreferences(getString(R.string.group_membership_key), 0);

        associatedGroups = getSharedPreferences(getString(R.string.associated_group_key), 0);

        user = UserHolder.getUser();

        configureNotificationSettings();

    }

    private void setCurrentUser(int userId, SharedPreferences coreProfile) {

        UserProperties userProperties = new UserProperties(userId, coreProfile.getString("description", ""),
                coreProfile.getString("first_name", ""), coreProfile.getString("last_name", ""),
                coreProfile.getString("organization_name", ""), coreProfile.getString("picture", null),
                coreProfile.getString("public_email", ""), coreProfile.getString("title", ""), null, null, null);

        User coreUser = User.createUser(userId, userProperties);

        UserHolder.setUser(coreUser);

    }

    private void configureNotificationSettings() {

        String[] notificationFields;

        if ("admin".equals(coreProfile.getString("role", ""))) {

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
                            field, description, coreProfile.getBoolean(field, false)
                    )
            );

        }

        NotificationSettingAdapter notificationSettingAdapter = new NotificationSettingAdapter(ProfileSettingsActivity.this, currentNotificationSettings, true);

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

        prefs.edit().putString("access_token", "")
                .putInt("user_id", 0).apply();

        // Clear stored active user profile

        coreProfile.edit().clear().apply();

        // Clear stored group memberships

        groupPrefs.edit().clear().apply();

        // Clear stored group memberships available to report tagging

        associatedGroups.edit().clear().apply();

        startActivity(new Intent(this, SignInActivity.class));

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

    // Back button should always go to user profile

    @Override
    public void onBackPressed() {

        startActivity(new Intent(this, UserProfileActivity.class));

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

    }

}

