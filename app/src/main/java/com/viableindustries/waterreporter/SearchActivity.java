package com.viableindustries.waterreporter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.viableindustries.waterreporter.data.BooleanQueryFilter;
import com.viableindustries.waterreporter.data.CompoundQueryFilter;
import com.viableindustries.waterreporter.data.HashTag;
import com.viableindustries.waterreporter.data.HashtagCollection;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.OrganizationService;
import com.viableindustries.waterreporter.data.QueryFilter;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.TagService;
import com.viableindustries.waterreporter.data.Territory;
import com.viableindustries.waterreporter.data.TerritoryCollection;
import com.viableindustries.waterreporter.data.TerritoryService;
import com.viableindustries.waterreporter.data.TrendingGroups;
import com.viableindustries.waterreporter.data.TrendingPeople;
import com.viableindustries.waterreporter.data.TrendingService;
import com.viableindustries.waterreporter.data.TrendingTags;
import com.viableindustries.waterreporter.data.TrendingTerritories;
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

    @Bind(R.id.spinner)
    ProgressBar progressBar;

    @Bind(R.id.search_message_separator)
    View searchMessageSeparator;

    @Bind(R.id.search_message)
    TextView searchMessage;

    @Bind(R.id.search_box)
    EditText searchBox;

    @Bind(R.id.clear_search)
    ImageButton clearSearch;

    @Bind(R.id.search_people)
    Button searchPeople;

    @Bind(R.id.search_organizations)
    Button searchOrgs;

    @Bind(R.id.search_watersheds)
    Button searchWatersheds;

    @Bind(R.id.search_tags)
    Button searchTags;

    @Bind(R.id.search_results)
    ListView searchResults;

    SharedPreferences prefs;

    Intent intent;

    Context context;

    RestAdapter restAdapter;

    OrganizationService service;

    protected TerritoryListAdapter territoryListAdapter;

    protected OrganizationListAdapter orgListAdapter;

    protected UserListAdapter userListAdapter;

    protected TagListAdapter tagListAdapter;

    ArrayList<Organization> baseOrganizationList;

    ArrayList<Territory> baseTerritoryList;

    ArrayList<User> baseUserList;

    ArrayList<HashTag> baseTagList;

    private int activeTab = 0;

    private String query;

    Handler handler;

    Runnable userSearchRunnable;

    Runnable orgSearchRunnable;

    Runnable territorySearchRunnable;

    Runnable tagSearchRunnable;

    private Resources resources;

    private String messageText;

    private String buildQuery(String collection, String sortField, String sortDirection, String searchChars) {

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<QuerySort>();

        QuerySort querySort = new QuerySort(sortField, sortDirection);

        queryOrder.add(querySort);

        // Create filter list and add a filter parameter

        List<Object> queryFilters = new ArrayList<>();

        if ("user".equals(collection)) {

            if (searchChars != null && searchChars.length() > 0) {

                List<Object> nameFilters = new ArrayList<>();

                String[] names = searchChars.trim().split("\\s+");

                for (String name : names) {

                    Log.d("name", name);

                }

                QueryFilter userFirstNameFilter = new QueryFilter("first_name", "ilike", String.format("%s%s%s", "%", names[0], "%"));

                nameFilters.add(userFirstNameFilter);

                if (names.length > 1) {

                    QueryFilter userLastNameFilter = new QueryFilter("last_name", "ilike", String.format("%s%s%s", "%", names[1], "%"));

                    nameFilters.add(userLastNameFilter);

                    queryFilters = nameFilters;

                } else {

                    QueryFilter userLastNameFilter = new QueryFilter("last_name", "ilike", String.format("%s%s%s", "%", names[0], "%"));

                    nameFilters.add(userLastNameFilter);

                    BooleanQueryFilter booleanNameFilter = new BooleanQueryFilter(nameFilters);

                    queryFilters.add(booleanNameFilter);

                }

            } else {

                QueryFilter nullNameFilter = new QueryFilter("first_name", "is_not_null", null);

                queryFilters.add(nullNameFilter);

                QueryFilter pictureFilter = new QueryFilter("picture", "is_not_null", null);

                queryFilters.add(pictureFilter);

            }

        } else if ("organization".equals(collection)) {

            if (searchChars != null) {

                QueryFilter orgNameFilter = new QueryFilter("name", "ilike", String.format("%s%s%s", "%", searchChars, "%"));

                queryFilters.add(orgNameFilter);

            }

        } else if ("tag".equals(collection)) {

            if (searchChars != null) {

                QueryFilter tagNameFilter = new QueryFilter("tag", "ilike", String.format("%s%s%s", "%", searchChars, "%"));

                queryFilters.add(tagNameFilter);

            }

        } else {

            if (searchChars != null) {

                QueryFilter territoryNameFilter = new QueryFilter("huc_8_name", "ilike", String.format("%s%s%s", "%", searchChars, "%"));

                queryFilters.add(territoryNameFilter);

            }

        }

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(queryFilters, queryOrder);

        return new Gson().toJson(queryParams);

    }

    protected void displaySeparator(boolean show) {

        if (show) {

            searchMessageSeparator.setVisibility(View.VISIBLE);

        } else {

            searchMessageSeparator.setVisibility(View.GONE);

        }

    }

    protected void displayMatchCount(int matchCount) {

        if (matchCount > 0) {

            messageText = resources.getQuantityString(R.plurals.search_match_count, matchCount, matchCount);

        } else {

            messageText = resources.getString(R.string.search_no_matches);

        }

        searchMessage.setText(messageText);

    }

    protected void displayProgressMessage(String category) {

        messageText = resources.getString(R.string.search_in_progress, category);

        searchMessage.setText(messageText);

    }

    protected void fetchOrganizations(int limit, int page, final String query, final boolean filterResults, final boolean switchCollection) {

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        RestAdapter restAdapter = OrganizationService.restAdapter;

        OrganizationService service = restAdapter.create(OrganizationService.class);

        service.getOrganizations(accessToken, "application/json", page, limit, query, new Callback<OrganizationFeatureCollection>() {

            @Override
            public void success(OrganizationFeatureCollection organizationFeatureCollection, Response response) {

                onGroupSuccess(organizationFeatureCollection.getFeatures(), filterResults, switchCollection);

            }

            @Override
            public void failure(RetrofitError error) {

                onRequestError(error);

            }

        });

    }

    protected void fetchTrendingGroups(int limit, int page, final boolean filterResults, final boolean switchCollection) {

        progressBar.setVisibility(View.VISIBLE);

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        RestAdapter restAdapter = TrendingService.restAdapter;

        TrendingService service = restAdapter.create(TrendingService.class);

        service.getTrendingGroups(accessToken, "application/json", page, limit, new Callback<TrendingGroups>() {

            @Override
            public void success(TrendingGroups trendingGroups, Response response) {

                onGroupSuccess(trendingGroups.getFeatures(), filterResults, switchCollection);

            }

            @Override
            public void failure(RetrofitError error) {

                onRequestError(error);

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

                onPeopleSuccess(userCollection.getFeatures(), filterResults, switchCollection);

            }

            @Override
            public void failure(RetrofitError error) {

                onRequestError(error);

            }

        });

    }

    protected void fetchTrendingPeople(int limit, int page, final boolean filterResults, final boolean switchCollection) {

        progressBar.setVisibility(View.VISIBLE);

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        RestAdapter restAdapter = TrendingService.restAdapter;

        TrendingService service = restAdapter.create(TrendingService.class);

        service.getTrendingPeople(accessToken, "application/json", page, limit, new Callback<TrendingPeople>() {

            @Override
            public void success(TrendingPeople trendingPeople, Response response) {

                onPeopleSuccess(trendingPeople.getFeatures(), filterResults, switchCollection);

            }

            @Override
            public void failure(RetrofitError error) {

                onRequestError(error);

            }

        });

    }

    protected void fetchTerritories(int limit, int page, final String query, final boolean filterResults, final boolean switchCollection) {

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        RestAdapter restAdapter = TerritoryService.restAdapter;

        TerritoryService service = restAdapter.create(TerritoryService.class);

        service.search(accessToken, "application/json", page, limit, query, new Callback<TerritoryCollection>() {

            @Override
            public void success(TerritoryCollection territoryCollection, Response response) {

                onTerritorySuccess(territoryCollection.getFeatures(), filterResults, switchCollection);

            }

            @Override
            public void failure(RetrofitError error) {

                onRequestError(error);

            }

        });

    }

    protected void fetchTrendingTerritories(int limit, int page, final boolean filterResults, final boolean switchCollection) {

        progressBar.setVisibility(View.VISIBLE);

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        RestAdapter restAdapter = TrendingService.restAdapter;

        TrendingService service = restAdapter.create(TrendingService.class);

        service.getTrendingTerritories(accessToken, "application/json", page, limit, new Callback<TrendingTerritories>() {

            @Override
            public void success(TrendingTerritories trendingTerritories, Response response) {

                onTerritorySuccess(trendingTerritories.getFeatures(), filterResults, switchCollection);

            }

            @Override
            public void failure(RetrofitError error) {

                onRequestError(error);

            }

        });

    }

    protected void fetchTags(int limit, int page, final String query, final boolean filterResults, final boolean switchCollection) {

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        RestAdapter restAdapter = TagService.restAdapter;

        TagService service = restAdapter.create(TagService.class);

        service.getMany(accessToken, "application/json", page, limit, query, new Callback<HashtagCollection>() {

            @Override
            public void success(HashtagCollection hashtagCollection, Response response) {

                onTagSuccess(hashtagCollection.getFeatures(), filterResults, switchCollection);

            }

            @Override
            public void failure(RetrofitError error) {

                onRequestError(error);

            }

        });

    }

    protected void fetchTrendingTags(int limit, int page, final boolean filterResults, final boolean switchCollection) {

        progressBar.setVisibility(View.VISIBLE);

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        RestAdapter restAdapter = TrendingService.restAdapter;

        TrendingService service = restAdapter.create(TrendingService.class);

        service.getTrendingTags(accessToken, "application/json", page, limit, new Callback<TrendingTags>() {

            @Override
            public void success(TrendingTags trendingTags, Response response) {

                onTagSuccess(trendingTags.getFeatures(), filterResults, switchCollection);

            }

            @Override
            public void failure(RetrofitError error) {

                onRequestError(error);

            }

        });

    }

    protected void onPeopleSuccess(ArrayList<User> users, boolean filterResults, boolean switchCollection) {

        progressBar.setVisibility(View.GONE);

        displayMatchCount(users.size());

        if (!users.isEmpty()) {

            searchResults.setVisibility(View.VISIBLE);

            if (!filterResults) {

                baseUserList.clear();

                baseUserList.addAll(users);

                userListAdapter = new UserListAdapter(SearchActivity.this, baseUserList, true);

            } else {

                userListAdapter = new UserListAdapter(SearchActivity.this, users, true);

            }

            if (switchCollection || activeTab == 0) {

                if (searchResults != null) {

                    searchResults.setAdapter(userListAdapter);
                }

            }

        } else {

            searchResults.setVisibility(View.GONE);

        }

    }

    protected void onGroupSuccess(ArrayList<Organization> organizations, boolean filterResults, boolean switchCollection) {

        progressBar.setVisibility(View.GONE);

        displayMatchCount(organizations.size());

        if (!organizations.isEmpty()) {

            searchResults.setVisibility(View.VISIBLE);

            if (!filterResults) {

                baseOrganizationList.clear();

                baseOrganizationList.addAll(organizations);

                orgListAdapter = new OrganizationListAdapter(SearchActivity.this, baseOrganizationList, true);

            } else {

                orgListAdapter = new OrganizationListAdapter(SearchActivity.this, organizations, true);

            }

            if (switchCollection || activeTab == 1) {

                if (searchResults != null) {

                    searchResults.setAdapter(orgListAdapter);
                }

            }

        } else {

            searchResults.setVisibility(View.GONE);

        }

    }

    protected void onTagSuccess(ArrayList<HashTag> hashTags, boolean filterResults, boolean switchCollection) {

        progressBar.setVisibility(View.GONE);

        displayMatchCount(hashTags.size());

        if (!hashTags.isEmpty()) {

            searchResults.setVisibility(View.VISIBLE);

            if (!filterResults) {

                baseTagList.clear();

                baseTagList.addAll(hashTags);

                tagListAdapter = new TagListAdapter(SearchActivity.this, baseTagList, true);

            } else {

                tagListAdapter = new TagListAdapter(SearchActivity.this, hashTags, true);

            }

            if (switchCollection || activeTab == 2) {

                if (searchResults != null) {

                    searchResults.setAdapter(tagListAdapter);
                }

            }

        } else {

            searchResults.setVisibility(View.GONE);

        }

    }

    protected void onTerritorySuccess(ArrayList<Territory> territories, boolean filterResults, boolean switchCollection) {

        progressBar.setVisibility(View.GONE);

        displayMatchCount(territories.size());

        if (!territories.isEmpty()) {

            searchResults.setVisibility(View.VISIBLE);

            if (!filterResults) {

                baseTerritoryList.clear();

                baseTerritoryList.addAll(territories);

                territoryListAdapter = new TerritoryListAdapter(SearchActivity.this, baseTerritoryList, true);

            } else {

                territoryListAdapter = new TerritoryListAdapter(SearchActivity.this, territories, true);

            }

            if (switchCollection || activeTab == 3) {

                if (searchResults != null) {

                    searchResults.setAdapter(territoryListAdapter);

                }

            }

        } else {

            searchResults.setVisibility(View.GONE);

        }

    }

    protected void onRequestError(RetrofitError error) {

        progressBar.setVisibility(View.GONE);

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        ButterKnife.bind(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        resources = getResources();

        // Set ProgressBar appearance

        progressBar.setIndeterminateDrawable(ContextCompat.getDrawable(this, R.drawable.blue_progress_compat));

        // Initialize empty list to hold organizations

        baseOrganizationList = new ArrayList<Organization>();

        // Initialize empty list to hold users

        baseUserList = new ArrayList<User>();

        // Initialize empty list to hold territories

        baseTerritoryList = new ArrayList<Territory>();

        // Initialize empty list to hold hashtags

        baseTagList = new ArrayList<HashTag>();

        fetchTrendingPeople(10, 1, false, true);

        fetchTrendingGroups(10, 1, false, false);

        fetchTrendingTerritories(10, 1, false, false);

        fetchTrendingTags(10, 1, false, false);

        searchPeople.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("Switch tab", "users");

                searchResults.setAdapter(null);

                activeTab = 0;

                if (query != null && !query.isEmpty()) {

                    fetchUsers(10, 1, buildQuery("user", "last_name", "asc", query), true, true);

                } else if (baseUserList.isEmpty()) {

                    Log.d("Switch tab", "User list is empty");

                    fetchTrendingPeople(10, 1, false, true);

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

                Log.d("Switch tab", "orgs");

                searchResults.setAdapter(null);

                activeTab = 1;

                if (query != null && !query.isEmpty()) {

                    fetchOrganizations(10, 1, buildQuery("organization", "name", "asc", query), true, true);

                } else if (baseOrganizationList.isEmpty()) {

                    Log.d("Switch tab", "Org list is empty");

                    fetchTrendingGroups(10, 1, false, true);

                } else {

                    Log.d("Switch tab", "Org list not empty");

                    orgListAdapter = new OrganizationListAdapter(SearchActivity.this, baseOrganizationList, true);

                    searchResults.setAdapter(orgListAdapter);

                }

            }

        });

        searchTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("Switch tab", "tags");

                searchResults.setAdapter(null);

                activeTab = 2;

                if (query != null && !query.isEmpty()) {

                    fetchTags(10, 1, buildQuery("tag", "id", "desc", query), true, false);

                } else if (baseTerritoryList.isEmpty()) {

                    Log.d("Switch tab", "Tag list is empty");

                    fetchTrendingTags(10, 1, false, true);

                } else {

                    Log.d("Switch tab", "Tag list not empty");

                    tagListAdapter = new TagListAdapter(SearchActivity.this, baseTagList, true);

                    searchResults.setAdapter(tagListAdapter);

                }

            }

        });

        searchWatersheds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("Switch tab", "territories");

                searchResults.setAdapter(null);

                activeTab = 3;

                if (query != null && !query.isEmpty()) {

                    fetchTerritories(10, 1, buildQuery("territory", "huc_8_name", "asc", query), true, true);

                } else if (baseTerritoryList.isEmpty()) {

                    Log.d("Switch tab", "Territory list is empty");

                    fetchTrendingTerritories(10, 1, false, true);

                } else {

                    Log.d("Switch tab", "Territory list not empty");

                    territoryListAdapter = new TerritoryListAdapter(SearchActivity.this, baseTerritoryList, true);

                    searchResults.setAdapter(territoryListAdapter);

                }

            }

        });

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                searchBox.setText("");

            }
        });

        // Observe changes in search input and respond accordingly

        handler = new Handler(Looper.getMainLooper());

        userSearchRunnable = new Runnable() {
            @Override
            public void run() {

                if (query.isEmpty()) {

                    displaySeparator(false);

                    searchMessage.setVisibility(View.GONE);

                    fetchTrendingPeople(10, 1, false, false);

                } else {

                    displaySeparator(true);

                    progressBar.setVisibility(View.GONE);

                    displayProgressMessage("people");

                    searchMessage.setVisibility(View.VISIBLE);

                    fetchUsers(10, 1, buildQuery("user", "last_name", "asc", query), true, true);

                }

            }
        };

        orgSearchRunnable = new Runnable() {
            @Override
            public void run() {

                if (query.isEmpty()) {

                    displaySeparator(false);

                    searchMessage.setVisibility(View.GONE);

                    fetchTrendingGroups(10, 1, false, false);

                } else {

                    displaySeparator(true);

                    progressBar.setVisibility(View.GONE);

                    displayProgressMessage("groups");

                    searchMessage.setVisibility(View.VISIBLE);

                    fetchOrganizations(10, 1, buildQuery("organization", "name", "asc", query), true, false);

                }

            }
        };

        territorySearchRunnable = new Runnable() {
            @Override
            public void run() {

                if (query.isEmpty()) {

                    displaySeparator(false);

                    searchMessage.setVisibility(View.GONE);

                    fetchTrendingTerritories(10, 1, false, false);

                } else {

                    displaySeparator(true);

                    progressBar.setVisibility(View.GONE);

                    displayProgressMessage("watersheds");

                    searchMessage.setVisibility(View.VISIBLE);

                    fetchTerritories(10, 1, buildQuery("territory", "huc_8_name", "asc", query), true, false);

                }

            }
        };

        tagSearchRunnable = new Runnable() {
            @Override
            public void run() {

                if (query.isEmpty()) {

                    displaySeparator(false);

                    searchMessage.setVisibility(View.GONE);

                    fetchTrendingTags(10, 1, false, false);

                } else {

                    displaySeparator(true);

                    progressBar.setVisibility(View.GONE);

                    displayProgressMessage("tags");

                    searchMessage.setVisibility(View.VISIBLE);

                    fetchTags(10, 1, buildQuery("tag", "id", "desc", query), true, false);

                }

            }
        };

        searchBox.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {

                query = cs.toString();

                if (query.isEmpty()) {

                    clearSearch.setVisibility(View.GONE);

                } else {

                    clearSearch.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                switch (activeTab) {

                    case 0:

                        handler.removeCallbacks(userSearchRunnable);

                        handler.postDelayed(userSearchRunnable, 300 /*delay*/);

                        break;

                    case 1:

                        handler.removeCallbacks(orgSearchRunnable);

                        handler.postDelayed(orgSearchRunnable, 300 /*delay*/);

                        break;

                    case 2:

                        handler.removeCallbacks(tagSearchRunnable);

                        handler.postDelayed(tagSearchRunnable, 300 /*delay*/);

                        break;

                    case 3:

                        handler.removeCallbacks(territorySearchRunnable);

                        handler.postDelayed(territorySearchRunnable, 300 /*delay*/);

                        break;

                }

            }

        });

    }

}
