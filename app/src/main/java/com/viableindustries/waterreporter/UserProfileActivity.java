package com.viableindustries.waterreporter;

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
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
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

    // Number of pages in our ViewPager
    private Integer NUM_PAGES = 3;

    // The pager widget, which handles animation and allows swiping horizontally
    private ViewPager mPager;

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

        Picasso.with(this).load(userAvatarUrl).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(userAvatar);

        //requestData(userId);

        //fetchUserGroups(userId);

//        DisplayMetrics metrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(metrics);
//
//        int height = metrics.heightPixels;
//
//        LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, height);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.profileViewPager);
        //mPager.setLayoutParams(vp);
        mPagerAdapter = new AttachmentPagerAdapter(this, getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        //Pager.setPageTransformer(true, new DepthPageTransformer());

        // Add tabs to ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.base_blue));
        tabLayout.setupWithViewPager(mPager);

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

    // A simple pager adapter that represents 4 PhotoFragment objects in sequence.
    private class AttachmentPagerAdapter extends FragmentPagerAdapter {

        Context ctxt = null;

        public AttachmentPagerAdapter(Context ctxt, FragmentManager fm) {
            super(fm);
            this.ctxt = ctxt;
        }

        @Override
        public Fragment getItem(int position) {

            return UserGroupsFragment.newInstance(userId);

//            switch (position) {
//                case 0: // Downstream photo
//                    return UserGroupsFragment.newInstance(userId);
//                case 1: // Upstream photo
//                    //return PhotoFragment.newInstance(1, getResources().getString(R.string.upstream_prompt), photoPreviewKey);
//                case 2: // First extra photo
//                    //return PhotoFragment.newInstance(2, getResources().getString(R.string.extra_prompt), photoPreviewKey);
//                //case 3: // Second extra photo
//                    //return PhotoFragment.newInstance(3, getResources().getString(R.string.extra_prompt), photoPreviewKey);
//                default:
//                    return UserGroupsFragment.newInstance(userId);
//            }

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
