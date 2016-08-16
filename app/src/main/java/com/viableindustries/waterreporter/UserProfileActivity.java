package com.viableindustries.waterreporter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.UserService;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UserProfileActivity extends AppCompatActivity {

    @Bind(R.id.userName)
    TextView userName;

    @Bind(R.id.userTitle)
    TextView userTitle;

    @Bind(R.id.userDescription)
    TextView userDescription;

    @Bind(R.id.userAvatar)
    ImageView userAvatar;

    @Bind(R.id.profileViewPager)
    ViewPager viewPager;

    @Bind(R.id.sliding_tabs)
    TabLayout tabLayout;

    // Number of pages in our ViewPager
    private Integer NUM_PAGES = 3;

    // The pager widget, which handles animation and allows swiping horizontally
    //private ViewPager mPager;

    // The pager adapter, which provides the pages to the view pager widget
    private PagerAdapter mPagerAdapter;

    private String userDescriptionText;

    private String userTitleText;

    private String userNameText;

    private String userAvatarUrl;

    private String userOrganization;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile);

        ButterKnife.bind(this);

        userId = getIntent().getExtras().getInt("USER_ID");
        userTitleText = getIntent().getExtras().getString("USER_TITLE");
        userDescriptionText = getIntent().getExtras().getString("USER_DESCRIPTION");
        userNameText = getIntent().getExtras().getString("USER_NAME");
        userOrganization = getIntent().getExtras().getString("USER_ORGANIZATION");
        userAvatarUrl = getIntent().getExtras().getString("USER_AVATAR");

        userName.setText(userNameText);

        userTitle.setText(userTitleText);

        userDescription.setText(userDescriptionText);

        userDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ObjectAnimator animation;

                int duration;

                int maxLines = TextViewCompat.getMaxLines(userDescription);

                if (maxLines == 2) {

                    userDescription.setEllipsize(null);

                    animation = ObjectAnimator.ofInt(
                            userDescription,
                            "maxLines",
                            2,
                            20);

                    duration = 350;

                } else {

                    userDescription.setEllipsize(TextUtils.TruncateAt.END);

                    animation = ObjectAnimator.ofInt(
                            userDescription,
                            "maxLines",
                            20,
                            2);

                    duration = 200;

                }

                animation.setDuration(duration);
                animation.setInterpolator(new LinearOutSlowInInterpolator());
                animation.start();

            }
        });

        Picasso.with(this).load(userAvatarUrl).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(userAvatar);

        // Instantiate a ViewPager and a PagerAdapter.
        //mPager = (ViewPager) findViewById(R.id.profileViewPager);
        //mPager.setLayoutParams(vp);
        mPagerAdapter = new ProfilePagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(mPagerAdapter);
        //Pager.setPageTransformer(true, new DepthPageTransformer());

        // Add tabs to ViewPager
        //TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.base_blue));
        tabLayout.setupWithViewPager(viewPager);

    }

    protected void requestData(int id) {

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        RestAdapter restAdapter = ReportService.restAdapter;

        ReportService service = restAdapter.create(ReportService.class);

        service.getSingleReport(access_token, "application/json", id, new Callback<Report>() {

            @Override
            public void success(Report reportResponse, Response response) {

                final Report report = reportResponse;

                //populateView(report);

            }

            @Override
            public void failure(RetrofitError error) {
            }

        });

    }

    protected void fetchUserGroups(int userId) {

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        Log.d("", access_token);

        // We shouldn't need to retrieve this value again, but we'll deal with that issue later
        //user_id = prefs.getInt("user_id", 0);

        UserService service = UserService.restAdapter.create(UserService.class);

        service.getUserOrganization(access_token, "application/json", userId, new Callback<OrganizationFeatureCollection>() {

            @Override
            public void success(OrganizationFeatureCollection organizationCollectionResponse, Response response) {

                List<Organization> organizations = organizationCollectionResponse.getFeatures();

                //String orgIds = "";

                if (!organizations.isEmpty()) {

                    for (Organization organization : organizations) {

                        //orgIds += String.format(",%s", organization.id);
                        Log.d("orgName", organization.properties.name);

                    }

                }

                // Reset the user's stored group IDs.

                //prefs.edit().putString("user_groups", orgIds).apply();

            }

            @Override
            public void failure(RetrofitError error) {

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(UserProfileActivity.this, SignInActivity.class));

                    }

                }

            }

        });

    }

    // A simple pager adapter that allows users to browse a person's feed and groups.
    private class ProfilePagerAdapter extends FragmentPagerAdapter {

        Context ctxt = null;

        public ProfilePagerAdapter(Context ctxt, FragmentManager fm) {
            super(fm);
            this.ctxt = ctxt;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return UserFeedFragment.newInstance(userId);
                case 1:
                    return UserGroupsFragment.newInstance(userId);
                case 2:
                    return UserGroupsFragment.newInstance(userId);
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public String getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "Reports";
                case 1:
                    return "Actions";
                case 2:
                    return "Groups";
                default:
                    return "Tab";
            }

        }

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

        Picasso.with(this).cancelRequest(userAvatar);

        ButterKnife.unbind(this);

    }

}
