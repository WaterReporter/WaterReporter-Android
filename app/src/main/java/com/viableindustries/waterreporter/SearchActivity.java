package com.viableindustries.waterreporter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.TextViewCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import butterknife.OnTextChanged;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SearchActivity extends FragmentActivity {

    @Bind(R.id.search_box)
    EditText searchBox;

    @Bind(R.id.clear_search)
    ImageButton clearSearch;

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

    ArrayList<Organization> baseOrganizationList;

    ArrayList<Organization> orgMatches;

    ArrayList<User> baseUserList;

    ArrayList<User> userMatches;

//    ListView resultList;

    private int activeTab = 0;

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

    // Observe changes in search input

//    @OnTextChanged(R.id.search_box)
//    void onSearchTextChanged(CharSequence q, int start, int count, int after) {
//
//        query = q.toString();
//
//        fetchUsers(10, 1, buildQuery("user", "last_name", "asc", query), true);
//
//        fetchOrganizations(10, 1, buildQuery("organization", "name", "asc", query), false);
//
//    }

    private String buildQuery(String collection, String sortField, String sortDirection, String searchChars) {

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<QuerySort>();

        QuerySort querySort = new QuerySort(sortField, sortDirection);

        queryOrder.add(querySort);

        // Create filter list and add a filter parameter

        List<QueryFilter> queryFilters = new ArrayList<QueryFilter>();

        if ("user".equals(collection)) {

            QueryFilter userPictureFilter = new QueryFilter("picture", "is_not_null", null);

            queryFilters.add(userPictureFilter);

            if (searchChars != null) {

                QueryFilter userFirstNameFilter = new QueryFilter("last_name", "ilike", String.format("%s%s", searchChars, "%"));

                queryFilters.add(userFirstNameFilter);

            }

        } else if ("organization".equals(collection)) {

            if (searchChars != null) {

                QueryFilter orgNameFilter = new QueryFilter("name", "ilike", String.format("%s%s", searchChars, "%"));

                queryFilters.add(orgNameFilter);

            }

        }

//        QueryFilter complexVal = new QueryFilter("id", "eq", organization.id);
//
//        QueryFilter userFilter = new QueryFilter("organization", "any", complexVal);
//
//        queryFilters.add(userFilter);

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(queryFilters, queryOrder);

        return new Gson().toJson(queryParams);

    }

    protected void fetchOrganizations(int limit, int page, final String query, final boolean filterResults, final boolean switchCollection) {

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        RestAdapter restAdapter = OrganizationService.restAdapter;

        OrganizationService service = restAdapter.create(OrganizationService.class);

        service.getOrganizations(accessToken, "application/json", page, limit, query, new Callback<OrganizationFeatureCollection>() {

            @Override
            public void success(OrganizationFeatureCollection organizationFeatureCollection, Response response) {

                ArrayList<Organization> organizations = organizationFeatureCollection.getFeatures();

//                searchAction = switchCollection ? 1 : searchAction;

                if (!organizations.isEmpty()) {

                    if (!filterResults) {

                        baseOrganizationList.addAll(organizations);

                        orgListAdapter = new OrganizationListAdapter(SearchActivity.this, baseOrganizationList, true);

                    } else {

                        orgListAdapter = new OrganizationListAdapter(SearchActivity.this, organizations, true);

                    }

//                    baseOrganizationList.addAll(organizations);
//
//                    orgListAdapter = new OrganizationListAdapter(SearchActivity.this, organizations, true);

                    if (switchCollection || activeTab == 1) {

                        searchResults.setAdapter(orgListAdapter);

                    }

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

    protected void fetchUsers(int limit, int page, final String query, final boolean filterResults, final boolean switchCollection) {

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        RestAdapter restAdapter = UserService.restAdapter;

        UserService service = restAdapter.create(UserService.class);

        service.getUsers(accessToken, "application/json", page, limit, query, new Callback<UserCollection>() {

            @Override
            public void success(UserCollection userCollection, Response response) {

                ArrayList<User> users = userCollection.getFeatures();

//                searchAction = switchCollection ? 0 : searchAction;

                if (!users.isEmpty()) {

                    if (!filterResults) {

                        baseUserList.addAll(users);

                        userListAdapter = new UserListAdapter(SearchActivity.this, baseUserList, true);

                    } else {

                        userListAdapter = new UserListAdapter(SearchActivity.this, users, true);

                    }

                    if (switchCollection || activeTab == 0) {

                        searchResults.setAdapter(userListAdapter);

                    }

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

        // Initialize empty list to hold organizations

        baseOrganizationList = new ArrayList<Organization>();

        // Initialize empty list to hold users

        baseUserList = new ArrayList<User>();

        fetchUsers(10, 1, buildQuery("user", "last_name", "asc", null), false, true);

        fetchOrganizations(10, 1, buildQuery("organization", "name", "asc", null), false, false);

        searchPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                searchResults.setAdapter(null);

                Log.d("Switch tab", "users");

                if (baseUserList.isEmpty()) {

                    Log.d("Switch tab", "User list is empty");

                    fetchUsers(10, 1, buildQuery("user", "last_name", "asc", null), false, true);

                } else {

                    Log.d("Switch tab", "User list not empty");

                    userListAdapter = new UserListAdapter(SearchActivity.this, baseUserList, true);

                    searchResults.setAdapter(userListAdapter);

                }

            }
        });

        searchOrgs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                searchResults.setAdapter(null);

                activeTab = 1;

                Log.d("Switch tab", "orgs");

                if (baseOrganizationList.isEmpty()) {

                    Log.d("Switch tab", "Org list is empty");

                    fetchOrganizations(10, 1, buildQuery("organization", "name", "asc", null), false, true);

                } else {

                    Log.d("Switch tab", "Org list not empty");

                    orgListAdapter = new OrganizationListAdapter(SearchActivity.this, baseOrganizationList, true);

                    searchResults.setAdapter(orgListAdapter);

                }

            }
        });

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //query = null;

                searchBox.setText("");

            }
        });

        // Observe changes in search input and respond accordingly

        final Handler handler = new Handler(Looper.getMainLooper());

        final Runnable userSearchRunnable = new Runnable() {
            @Override
            public void run() {
                fetchUsers(10, 1, buildQuery("user", "last_name", "asc", query), true, true);
            }
        };

        final Runnable orgSearchRunnable = new Runnable() {
            @Override
            public void run() {
                fetchOrganizations(10, 1, buildQuery("organization", "name", "asc", query), true, false);
            }
        };

        searchBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
//                seq = cs;

                query = cs.toString();

//                fetchUsers(10, 1, buildQuery("user", "last_name", "asc", query), true);
//
//                fetchOrganizations(10, 1, buildQuery("organization", "name", "asc", query), false);

            }

            @Override
            public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
//                new SearchTask().execute(seq.toString().trim());
//
//                query = q.toString();

                handler.removeCallbacks(userSearchRunnable);

                handler.removeCallbacks(orgSearchRunnable);

//                workRunnable = () -> fetchUsers(10, 1, buildQuery("user", "last_name", "asc", query), true);

                switch (activeTab) {

                    case 0:

                        handler.postDelayed(userSearchRunnable, 300 /*delay*/);

                        break;

                    case 1:

                        handler.postDelayed(orgSearchRunnable, 300 /*delay*/);

                        break;

                }

            }

        });

//        @Override public void afterTextChanged(Editable s) {
//            handler.removeCallbacks(workRunnable);
//            workRunnable = () -> doSmth(s.toString());
//            handler.postDelayed(workRunnable, 500 /*delay*/);
//        }


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
