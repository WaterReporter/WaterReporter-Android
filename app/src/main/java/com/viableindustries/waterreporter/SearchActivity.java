package com.viableindustries.waterreporter;

import android.animation.ObjectAnimator;
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
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.TextViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.OrganizationService;
import com.viableindustries.waterreporter.data.QueryFilter;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserCollection;
import com.viableindustries.waterreporter.data.UserService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SearchActivity extends FragmentActivity {

    @Bind(R.id.search_box)
    EditText searchBox;

    @Bind(R.id.search_people)
    Button searchPeople;

    @Bind(R.id.search_organizations)
    Button searchOrgs;

    @Bind(R.id.search_results)
    ListView searchResults;

    SharedPreferences prefs;

    Intent intent;

    Context context;

    RestAdapter restAdapter;

    OrganizationService service;

    protected OrganizationListAdapter orgListAdapter;

    protected UserListAdapter userListAdapter;

//    ListView resultList;

    private int SEARCH_ACTION = 0;

    private String query;

//    private ViewPager mPager;

//    private PagerAdapter mPagerAdapter;

    private static final String[] TITLES = {
            "People",
            //"Watersheds",
            "Organizations",
            "Tags"
    };

    public static final int NUM_TITLES = TITLES.length;

//    private class PagerAdapter extends FragmentPagerAdapter {
//
//        Context ctxt = null;
//
//        public PagerAdapter(Context ctxt, FragmentManager fm) {
//            super(fm);
//            this.ctxt = ctxt;
//        }

//        @Override
//        public Fragment getItem(int position) {
//
//            Log.d("Tab position", String.valueOf(position));
//
////            final String accessToken = prefs.getString("access_token", "");
////
////            if (accessToken.isEmpty()) {
////
////                startActivity(new Intent(SearchActivity.this, MainActivity.class));
////
////                finish();
////
////            }
////
////            RestAdapter restAdapter;
////
////            UserService service;
////
////            Map<String, Object> params = new HashMap<>();
//
//            switch (position) {
//
//                case 0:
//
////                    restAdapter = UserService.restAdapter;
////
////                    service = restAdapter.create(UserService.class);
////
////                    params.put("collection", "user");
////
////                    params.put("service", service);
//
//                    return UserSearchFragment.newInstance();
//
//                case 1:
//
////                    restAdapter = UserService.restAdapter;
////
////                    service = restAdapter.create(UserService.class);
////
////                    params.put("collection", "user");
////
////                    params.put("service", service);
//
//                    return OrganizationSearchFragment.newInstance();
//
//                case 2:
//
////                    restAdapter = UserService.restAdapter;
////
////                    service = restAdapter.create(UserService.class);
////
////                    params.put("collection", "user");
////
////                    params.put("service", service);
//
//                    return OrganizationSearchFragment.newInstance();
//
//                default:
//
//                    return null;
//
//            }
//
//        }
//
//        @Override
//        public int getCount() {
//            return NUM_TITLES;
//        }
//
//        @Override
//        public String getPageTitle(int position) {
//
//            return TITLES[position % NUM_TITLES].toUpperCase();
//
//        }
//
//    }

    private String buildQuery(String sortField, String sortDirection, Map filterArgs) {

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<QuerySort>();

        QuerySort querySort = new QuerySort(sortField, sortDirection);

        queryOrder.add(querySort);

        // Create filter list and add a filter parameter

        List<QueryFilter> queryFilters = new ArrayList<QueryFilter>();

//        QueryFilter complexVal = new QueryFilter("id", "eq", organization.id);
//
//        QueryFilter userFilter = new QueryFilter("organization", "any", complexVal);
//
//        queryFilters.add(userFilter);

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(queryFilters, queryOrder);

        return new Gson().toJson(queryParams);

    }

    protected void fetchOrganizations(int limit, int page, final String query) {

//        final SharedPreferences prefs =
//                context.getSharedPreferences(context.getPackageName(), MODE_PRIVATE);

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        RestAdapter restAdapter = OrganizationService.restAdapter;

        OrganizationService service = restAdapter.create(OrganizationService.class);

        service.getOrganizations(accessToken, "application/json", page, limit, query, new Callback<OrganizationFeatureCollection>() {

            @Override
            public void success(OrganizationFeatureCollection organizationFeatureCollection, Response response) {

                ArrayList<Organization> organizations = organizationFeatureCollection.getFeatures();

                if (!organizations.isEmpty()) {

//                    memberCollection.addAll(users);

                    orgListAdapter = new OrganizationListAdapter(SearchActivity.this, organizations, true);

                    searchResults.setAdapter(orgListAdapter);

//                    attachScrollListener();

                }

            }

            @Override
            public void failure(RetrofitError error) {

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(context, SignInActivity.class));

                    }

                }

            }

        });

    }

    protected void fetchUsers(int limit, int page, final String query) {

//        final SharedPreferences prefs =
//                context.getSharedPreferences(context.getPackageName(), MODE_PRIVATE);

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        RestAdapter restAdapter = UserService.restAdapter;

        UserService service = restAdapter.create(UserService.class);

        service.getUsers(accessToken, "application/json", page, limit, query, new Callback<UserCollection>() {

            @Override
            public void success(UserCollection userCollection, Response response) {

                ArrayList<User> users = userCollection.getFeatures();

                if (!users.isEmpty()) {

//                    memberCollection.addAll(users);

                    userListAdapter = new UserListAdapter(SearchActivity.this, users, true);

                    searchResults.setAdapter(userListAdapter);

//                    attachScrollListener();

                }

            }

            @Override
            public void failure(RetrofitError error) {

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(context, SignInActivity.class));

                    }

                }

            }

        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        ButterKnife.bind(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        fetchUsers(10, 1, buildQuery("last_name", "asc", null));

        searchPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fetchUsers(10, 1, buildQuery("last_name", "asc", null));

            }
        });

        searchOrgs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fetchOrganizations(10, 1, buildQuery("name", "asc", null));

            }
        });


        // Instantiate a ViewPager and a PagerAdapter.
//        mPager = (ViewPager) findViewById(R.id.search_results);
//        mPagerAdapter = new PagerAdapter(this, getSupportFragmentManager());
//        mPager.setAdapter(mPagerAdapter);
//        mPager.setPageTransformer(true, new DepthPageTransformer());

        // Add tabs for navigating the ViewPager
//        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.search_categories);
//        mSlidingTabLayout.setDistributeEvenly(true);
//        mSlidingTabLayout.setViewPager(mPager);
//        tabLayout.setupWithViewPager(mPager);

    }

}
