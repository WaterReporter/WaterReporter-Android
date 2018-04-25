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
import android.widget.Button;
import android.widget.ScrollView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.user_interface.view_holders.UserProfileHeaderView;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WelcomeActivity extends AppCompatActivity {

    private int userId;

    private SharedPreferences mSharedPreferences;

    private SharedPreferences mCoreProfile;

    private User mUser;

    private UserProfileHeaderView mUserProfileHeaderView;

    private Resources mResources;

    @Bind(R.id.scrollView)
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

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

        //
        // Must access the core profile for the authenticated user
        // rather than the generic application preferences.
        //

        mUser = ModelStorage.getStoredUser(mCoreProfile, "auth_user");

        try {

            userId = mUser.properties.id;

            Log.d("stored--user--id", userId + "");

            addListViewHeader(mUser);

        } catch (NullPointerException e1) {

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

    }

    private void addListViewHeader(User user) {

        mUserProfileHeaderView = new UserProfileHeaderView();

        LayoutInflater inflater = getLayoutInflater();

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.user_welcome_header, scrollView, false);

        mUserProfileHeaderView.buildWelcomeHeader(this, mSharedPreferences, getSupportFragmentManager(), header, user);

        scrollView.addView(header);

        Button getStarted = (Button) header.findViewById(R.id.getStarted);

        getStarted.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));

            }

        });

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
