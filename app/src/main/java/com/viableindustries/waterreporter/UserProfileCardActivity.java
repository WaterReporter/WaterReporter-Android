package com.viableindustries.waterreporter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ScrollView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.api.models.snapshot.UserSnapshot;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.user_interface.view_holders.UserProfileHeaderView;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserProfileCardActivity extends AppCompatActivity {

    private int userId;

    private SharedPreferences mSharedPreferences;

    private SharedPreferences mCoreProfile;

    private User mUser;

    private UserSnapshot mUserSnapshot;

    private UserProfileHeaderView mUserProfileHeaderView;

    private Resources mResources;

    @Bind(R.id.scrollView)
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_card);

        ButterKnife.bind(this);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        mCoreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

        mResources = getResources();

        retrieveStoredUser();

    }

    private void retrieveStoredUser() {

        mUser = ModelStorage.getStoredUser(mSharedPreferences, "stored_user");

        try {

            userId = mUser.properties.id;

            Log.d("stored--user--id", userId + "");

            addListViewHeader(mUser);

            retrieveStoredSnapshot();

        } catch (NullPointerException e1) {

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

    }

    private void retrieveStoredSnapshot() {

        mUserSnapshot = ModelStorage.getStoredUserSnapshot(mSharedPreferences);

        try {

            int postCount = mUserSnapshot.posts;

            Log.d("stored--user--posts", postCount + "");

            setSnapshotData(mUserSnapshot);

        } catch (NullPointerException e1) {

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

    }

    private void addListViewHeader(User user) {

        mUserProfileHeaderView = new UserProfileHeaderView();

        LayoutInflater inflater = getLayoutInflater();

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.user_profile_card_full, scrollView, false);

        mUserProfileHeaderView.buildHeader(this, mSharedPreferences, getSupportFragmentManager(), header, user);

        scrollView.addView(header);

    }

    private void setSnapshotData(UserSnapshot userSnapshot) {

        String reportCountText = String.format("%s %s", String.valueOf(userSnapshot.posts),
                mResources.getQuantityString(R.plurals.post_label, userSnapshot.posts, userSnapshot.posts));
        mUserProfileHeaderView.reportCounter.setText(reportCountText);

        String actionCountText = String.format("%s %s", String.valueOf(userSnapshot.actions),
                mResources.getQuantityString(R.plurals.action_label, userSnapshot.actions, userSnapshot.actions));
        mUserProfileHeaderView.actionCounter.setText(actionCountText);

        String groupCountText = String.format("%s %s", String.valueOf(userSnapshot.groups),
                mResources.getQuantityString(R.plurals.group_label, userSnapshot.groups, userSnapshot.groups));
        mUserProfileHeaderView.groupCounter.setText(groupCountText);

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

        Picasso.with(this).cancelRequest(mUserProfileHeaderView.headerCanvas);

        Picasso.with(this).cancelRequest(mUserProfileHeaderView.userAvatar);

        ButterKnife.unbind(this);

    }

}
