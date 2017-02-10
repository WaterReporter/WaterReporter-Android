package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.UserService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RestAdapter;

public class SearchActivity extends FragmentActivity {

//    @Bind(R.id.search_categories)
//    SlidingTabLayout mSlidingTabLayout;

    @Bind(R.id.search_categories)
    TabLayout tabLayout;

    @Bind(R.id.search_results)
    ViewPager mPager;

    SharedPreferences prefs;

    Intent intent;

    Context context;

//    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;

    private static final String[] TITLES = {
            "People",
            //"Watersheds",
            "Organizations",
            "Tags"
    };

    public static final int NUM_TITLES = TITLES.length;

    private class PagerAdapter extends FragmentPagerAdapter {

        Context ctxt = null;

        public PagerAdapter(Context ctxt, FragmentManager fm) {
            super(fm);
            this.ctxt = ctxt;
        }

        @Override
        public Fragment getItem(int position) {

            Log.d("Tab position", String.valueOf(position));

//            final String accessToken = prefs.getString("access_token", "");
//
//            if (accessToken.isEmpty()) {
//
//                startActivity(new Intent(SearchActivity.this, MainActivity.class));
//
//                finish();
//
//            }
//
//            RestAdapter restAdapter;
//
//            UserService service;
//
//            Map<String, Object> params = new HashMap<>();

            switch (position) {

                case 0:

//                    restAdapter = UserService.restAdapter;
//
//                    service = restAdapter.create(UserService.class);
//
//                    params.put("collection", "user");
//
//                    params.put("service", service);

                    return UserSearchFragment.newInstance();

                case 1:

//                    restAdapter = UserService.restAdapter;
//
//                    service = restAdapter.create(UserService.class);
//
//                    params.put("collection", "user");
//
//                    params.put("service", service);

                    return OrganizationSearchFragment.newInstance();

                default:

                    return null;

            }

        }

        @Override
        public int getCount() {
            return NUM_TITLES;
        }

        @Override
        public String getPageTitle(int position) {

            return TITLES[position % NUM_TITLES].toUpperCase();

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        ButterKnife.bind(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        // Instantiate a ViewPager and a PagerAdapter.
//        mPager = (ViewPager) findViewById(R.id.search_results);
        mPagerAdapter = new PagerAdapter(this, getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageTransformer(true, new DepthPageTransformer());

        // Add tabs for navigating the ViewPager
//        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.search_categories);
//        mSlidingTabLayout.setDistributeEvenly(true);
//        mSlidingTabLayout.setViewPager(mPager);
        tabLayout.setupWithViewPager(mPager);

    }

}
