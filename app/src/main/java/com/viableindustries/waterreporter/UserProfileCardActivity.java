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

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.user_interface.adapters.TimelineAdapter;
import com.viableindustries.waterreporter.user_interface.view_holders.UserProfileHeaderView;
import com.viableindustries.waterreporter.utilities.EndlessScrollListener;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class UserProfileCardActivity extends AppCompatActivity {

    private int userId;

    private SharedPreferences mSharedPreferences;

    private SharedPreferences mCoreProfile;

    private User mUser;

    private UserProfileHeaderView mUserProfileHeaderView;

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

        retrieveStoredUser();

        retrieveStoredSnapshot();

    }

    private void retrieveStoredUser() {

        mUser = ModelStorage.getStoredUser(mSharedPreferences, "stored_user");

        try {

            userId = mUser.properties.id;

            Log.d("stored--user--id", userId + "");

            setUserData(mUser);

        } catch (NullPointerException e1) {

            try {

                Log.d("USER ID ONLY", "proceed to load profile data");

                userId = mUser.id;

                fetchUser(userId);

            } catch (NullPointerException e2) {

                startActivity(new Intent(this, MainActivity.class));

                finish();

            }

        }

    }

    private void retrieveStoredSnapshot() {

        mUser = ModelStorage.getStoredUser(mSharedPreferences, "stored_user");

        try {

            userId = mUser.properties.id;

            Log.d("stored--user--id", userId + "");

            setUserData(mUser);

        } catch (NullPointerException e1) {

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

    }

    private void addListViewHeader(User user) {

        mUserProfileHeaderView = new UserProfileHeaderView();

        LayoutInflater inflater = getLayoutInflater();

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.user_profile_header, timeLine, false);

        mUserProfileHeaderView.buildHeader(this, mSharedPreferences, getSupportFragmentManager(), header, user);

        timeLine.addHeaderView(header, null, false);

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

        Picasso.with(this).cancelRequest(mUserProfileHeaderView.userAvatar);

        ButterKnife.unbind(this);

//        ModelStorage.removeModel(mSharedPreferences, "stored_user");

    }

}
