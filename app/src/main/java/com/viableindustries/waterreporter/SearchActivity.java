package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.interfaces.data.hashtag.HashTagService;
import com.viableindustries.waterreporter.api.interfaces.data.organization.OrganizationService;
import com.viableindustries.waterreporter.api.interfaces.data.territory.TerritoryService;
import com.viableindustries.waterreporter.api.interfaces.data.trending.TrendingService;
import com.viableindustries.waterreporter.api.interfaces.data.user.UserService;
import com.viableindustries.waterreporter.api.models.hashtag.HashTag;
import com.viableindustries.waterreporter.api.models.hashtag.HashtagCollection;
import com.viableindustries.waterreporter.api.models.hashtag.TrendingTags;
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.organization.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.api.models.organization.TrendingGroups;
import com.viableindustries.waterreporter.api.models.query.BooleanQueryFilter;
import com.viableindustries.waterreporter.api.models.query.QueryFilter;
import com.viableindustries.waterreporter.api.models.query.QueryParams;
import com.viableindustries.waterreporter.api.models.query.QuerySort;
import com.viableindustries.waterreporter.api.models.territory.Territory;
import com.viableindustries.waterreporter.api.models.territory.TerritoryCollection;
import com.viableindustries.waterreporter.api.models.territory.TrendingTerritories;
import com.viableindustries.waterreporter.api.models.user.TrendingPeople;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.api.models.user.UserCollection;
import com.viableindustries.waterreporter.user_interface.adapters.OrganizationListAdapter;
import com.viableindustries.waterreporter.user_interface.adapters.TagListAdapter;
import com.viableindustries.waterreporter.user_interface.adapters.TerritoryListAdapter;
import com.viableindustries.waterreporter.user_interface.adapters.UserListAdapter;
import com.viableindustries.waterreporter.utilities.CancelableCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
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
    RelativeLayout searchPeople;

    @Bind(R.id.search_organizations)
    RelativeLayout searchOrgs;

    @Bind(R.id.search_watersheds)
    RelativeLayout searchWatersheds;

    @Bind(R.id.search_tags)
    RelativeLayout searchTags;

    @Bind(R.id.search_results)
    ListView searchResults;

    private SharedPreferences prefs;

    Intent intent;

    private Context context;

    RestAdapter restAdapter;

    OrganizationService service;

    private TerritoryListAdapter territoryListAdapter;

    private OrganizationListAdapter orgListAdapter;

    private UserListAdapter userListAdapter;

    private TagListAdapter tagListAdapter;

    private ArrayList<Organization> baseOrganizationList;

    private ArrayList<Territory> baseTerritoryList;

    private ArrayList<User> baseUserList;

    private ArrayList<HashTag> baseTagList;

    private int activeTab = 0;

    private String query;

    private Handler handler;

    private Runnable userSearchRunnable;

    private Runnable orgSearchRunnable;

    private Runnable territorySearchRunnable;

    private Runnable tagSearchRunnable;

    private Resources resources;

    private String messageText;

    private String buildQuery(String collection, String sortField, String sortDirection, String searchChars) {

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<>();

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

    private void displaySeparator(boolean show) {

        if (show) {

            searchMessageSeparator.setVisibility(View.VISIBLE);

        } else {

            searchMessageSeparator.setVisibility(View.GONE);

        }

    }

    private void displayMatchCount(int matchCount) {

        if (matchCount > 0) {

            messageText = resources.getQuantityString(R.plurals.search_match_count, matchCount, matchCount);

        } else {

            messageText = resources.getString(R.string.search_no_matches);

        }

        searchMessage.setText(messageText);

    }

    private void displayProgressMessage(String category) {

        messageText = resources.getString(R.string.search_in_progress, category);

        searchMessage.setText(messageText);

    }

    private void fetchOrganizations(int limit, int page, final String query, final boolean filterResults, final boolean switchCollection) {

        final String accessToken = prefs.getString("access_token", "");

        RestClient.getOrganizationService().getOrganizations(accessToken, "application/json", page, limit, query, new CancelableCallback<OrganizationFeatureCollection>() {

            @Override
            public void onSuccess(OrganizationFeatureCollection organizationFeatureCollection, Response response) {

                onGroupSuccess(organizationFeatureCollection.getFeatures(), filterResults, switchCollection);

            }

            @Override
            public void onFailure(RetrofitError error) {

                onRequestError(error);

            }

        });

    }

    private void fetchTrendingGroups(int limit, int page, final boolean filterResults, final boolean switchCollection) {

        progressBar.setVisibility(View.VISIBLE);

        final String accessToken = prefs.getString("access_token", "");

        RestClient.getTrendingService().getTrendingGroups(accessToken, "application/json", page, limit, new CancelableCallback<TrendingGroups>() {

            @Override
            public void onSuccess(TrendingGroups trendingGroups, Response response) {

                onGroupSuccess(trendingGroups.getFeatures(), filterResults, switchCollection);

            }

            @Override
            public void onFailure(RetrofitError error) {

                onRequestError(error);

            }

        });

    }

    private void fetchUsers(int limit, int page, final String query, final boolean filterResults, final boolean switchCollection) {

        final String accessToken = prefs.getString("access_token", "");

        RestClient.getUserService().getUsers(accessToken, "application/json", page, limit, query, new CancelableCallback<UserCollection>() {

            @Override
            public void onSuccess(UserCollection userCollection, Response response) {

                onPeopleSuccess(userCollection.getFeatures(), filterResults, switchCollection);

            }

            @Override
            public void onFailure(RetrofitError error) {

                onRequestError(error);

            }

        });

    }

    private void fetchTrendingPeople(int limit, int page, final boolean filterResults, final boolean switchCollection) {

        progressBar.setVisibility(View.VISIBLE);

        final String accessToken = prefs.getString("access_token", "");

        RestClient.getTrendingService().getTrendingPeople(accessToken, "application/json", page, limit, new CancelableCallback<TrendingPeople>() {

            @Override
            public void onSuccess(TrendingPeople trendingPeople, Response response) {

                onPeopleSuccess(trendingPeople.getFeatures(), filterResults, switchCollection);

            }

            @Override
            public void onFailure(RetrofitError error) {

                onRequestError(error);

            }

        });

    }

    private void fetchTerritories(int limit, int page, final String query, final boolean filterResults, final boolean switchCollection) {

        final String accessToken = prefs.getString("access_token", "");

        RestClient.getTerritoryService().search(accessToken, "application/json", page, limit, query, new CancelableCallback<TerritoryCollection>() {

            @Override
            public void onSuccess(TerritoryCollection territoryCollection, Response response) {

                onTerritorySuccess(territoryCollection.getFeatures(), filterResults, switchCollection);

            }

            @Override
            public void onFailure(RetrofitError error) {

                onRequestError(error);

            }

        });

    }

    private void fetchTrendingTerritories(int limit, int page, final boolean filterResults, final boolean switchCollection) {

        progressBar.setVisibility(View.VISIBLE);

        final String accessToken = prefs.getString("access_token", "");

        RestClient.getTrendingService().getTrendingTerritories(accessToken, "application/json", page, limit, new CancelableCallback<TrendingTerritories>() {

            @Override
            public void onSuccess(TrendingTerritories trendingTerritories, Response response) {

                onTerritorySuccess(trendingTerritories.getFeatures(), filterResults, switchCollection);

            }

            @Override
            public void onFailure(RetrofitError error) {

                onRequestError(error);

            }

        });

    }

    private void fetchTags(int limit, int page, final String query, final boolean filterResults, final boolean switchCollection) {

        final String accessToken = prefs.getString("access_token", "");

        RestClient.getHashTagService().getMany(accessToken, "application/json", page, limit, query, new CancelableCallback<HashtagCollection>() {

            @Override
            public void onSuccess(HashtagCollection hashtagCollection, Response response) {

                onTagSuccess(hashtagCollection.getFeatures(), filterResults, switchCollection);

            }

            @Override
            public void onFailure(RetrofitError error) {

                onRequestError(error);

            }

        });

    }

    private void fetchTrendingTags(int limit, int page, final boolean filterResults, final boolean switchCollection) {

        progressBar.setVisibility(View.VISIBLE);

        final String accessToken = prefs.getString("access_token", "");

        RestClient.getTrendingService().getTrendingTags(accessToken, "application/json", page, limit, new CancelableCallback<TrendingTags>() {

            @Override
            public void onSuccess(TrendingTags trendingTags, Response response) {

                onTagSuccess(trendingTags.getFeatures(), filterResults, switchCollection);

            }

            @Override
            public void onFailure(RetrofitError error) {

                onRequestError(error);

            }

        });

    }

    private void onPeopleSuccess(ArrayList<User> users, boolean filterResults, boolean switchCollection) {

        progressBar.setVisibility(View.GONE);

        displayMatchCount(users.size());

        if (!users.isEmpty()) {

            searchResults.setVisibility(View.VISIBLE);

            if (!filterResults) {

                baseUserList.clear();

                baseUserList.addAll(users);

                userListAdapter = new UserListAdapter(SearchActivity.this, baseUserList);

            } else {

                userListAdapter = new UserListAdapter(SearchActivity.this, users);

            }

            if (switchCollection || activeTab == 0) {

                searchResults.setAdapter(userListAdapter);

            }

        } else {

            searchResults.setVisibility(View.GONE);

        }

    }

    private void onGroupSuccess(ArrayList<Organization> organizations, boolean filterResults, boolean switchCollection) {

        progressBar.setVisibility(View.GONE);

        displayMatchCount(organizations.size());

        if (!organizations.isEmpty()) {

            searchResults.setVisibility(View.VISIBLE);

            if (!filterResults) {

                baseOrganizationList.clear();

                baseOrganizationList.addAll(organizations);

                orgListAdapter = new OrganizationListAdapter(SearchActivity.this, baseOrganizationList);

            } else {

                orgListAdapter = new OrganizationListAdapter(SearchActivity.this, organizations);

            }

            if (switchCollection || activeTab == 1) {

                searchResults.setAdapter(orgListAdapter);

            }

        } else {

            searchResults.setVisibility(View.GONE);

        }

    }

    private void onTagSuccess(ArrayList<HashTag> hashTags, boolean filterResults, boolean switchCollection) {

        progressBar.setVisibility(View.GONE);

        displayMatchCount(hashTags.size());

        if (!hashTags.isEmpty()) {

            searchResults.setVisibility(View.VISIBLE);

            if (!filterResults) {

                baseTagList.clear();

                baseTagList.addAll(hashTags);

                tagListAdapter = new TagListAdapter(SearchActivity.this, baseTagList);

            } else {

                tagListAdapter = new TagListAdapter(SearchActivity.this, hashTags);

            }

            if (switchCollection || activeTab == 2) {

                searchResults.setAdapter(tagListAdapter);

            }

        } else {

            searchResults.setVisibility(View.GONE);

        }

    }

    private void onTerritorySuccess(ArrayList<Territory> territories, boolean filterResults, boolean switchCollection) {

        progressBar.setVisibility(View.GONE);

        displayMatchCount(territories.size());

        if (!territories.isEmpty()) {

            searchResults.setVisibility(View.VISIBLE);

            if (!filterResults) {

                baseTerritoryList.clear();

                baseTerritoryList.addAll(territories);

                territoryListAdapter = new TerritoryListAdapter(SearchActivity.this, baseTerritoryList);

            } else {

                territoryListAdapter = new TerritoryListAdapter(SearchActivity.this, territories);

            }

            if (switchCollection || activeTab == 3) {

                searchResults.setAdapter(territoryListAdapter);

            }

        } else {

            searchResults.setVisibility(View.GONE);

        }

    }

    private void onRequestError(RetrofitError error) {

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

    private void highlightButton(RelativeLayout button) {

        button.setAlpha(0.8f);

    }

    private void dimButtons(RelativeLayout[] buttons) {

        for (RelativeLayout btn : buttons) {

            btn.setAlpha(0.4f);

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

//        progressBar.setIndeterminateDrawable(ContextCompat.getDrawable(this, R.drawable.blue_progress_compat));

        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.splash_blue), android.graphics.PorterDuff.Mode.SRC_IN);

        // Initialize empty list to hold organizations

        baseOrganizationList = new ArrayList<>();

        // Initialize empty list to hold users

        baseUserList = new ArrayList<>();

        // Initialize empty list to hold territories

        baseTerritoryList = new ArrayList<>();

        // Initialize empty list to hold hashtags

        baseTagList = new ArrayList<>();

        // Set initial category highlight

        highlightButton(searchPeople);

        dimButtons(new RelativeLayout[]{
                searchWatersheds,
                searchTags,
                searchOrgs
        });

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

                highlightButton(searchPeople);

                dimButtons(new RelativeLayout[]{
                        searchWatersheds,
                        searchTags,
                        searchOrgs
                });

                if (query != null && !query.isEmpty()) {

                    fetchUsers(10, 1, buildQuery("user", "last_name", "asc", query), true, true);

                } else if (baseUserList.isEmpty()) {

                    Log.d("Switch tab", "User list is empty");

                    fetchTrendingPeople(10, 1, false, true);

                } else {

                    Log.d("Switch tab", "User list not empty");

                    userListAdapter = new UserListAdapter(SearchActivity.this, baseUserList);

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

                highlightButton(searchOrgs);

                dimButtons(new RelativeLayout[]{
                        searchWatersheds,
                        searchTags,
                        searchPeople
                });


                if (query != null && !query.isEmpty()) {

                    fetchOrganizations(10, 1, buildQuery("organization", "name", "asc", query), true, true);

                } else if (baseOrganizationList.isEmpty()) {

                    Log.d("Switch tab", "Org list is empty");

                    fetchTrendingGroups(10, 1, false, true);

                } else {

                    Log.d("Switch tab", "Org list not empty");

                    orgListAdapter = new OrganizationListAdapter(SearchActivity.this, baseOrganizationList);

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

                highlightButton(searchTags);

                dimButtons(new RelativeLayout[]{
                        searchWatersheds,
                        searchPeople,
                        searchOrgs
                });


                if (query != null && !query.isEmpty()) {

                    fetchTags(10, 1, buildQuery("tag", "id", "desc", query), true, false);

                } else if (baseTerritoryList.isEmpty()) {

                    Log.d("Switch tab", "Tag list is empty");

                    fetchTrendingTags(10, 1, false, true);

                } else {

                    Log.d("Switch tab", "Tag list not empty");

                    tagListAdapter = new TagListAdapter(SearchActivity.this, baseTagList);

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

                highlightButton(searchWatersheds);

                dimButtons(new RelativeLayout[]{
                        searchPeople,
                        searchTags,
                        searchOrgs
                });


//                searchWatersheds.setTextColor(ContextCompat.getColor(SearchActivity.this, R.color.base_blue));

                if (query != null && !query.isEmpty()) {

                    fetchTerritories(10, 1, buildQuery("territory", "huc_8_name", "asc", query), true, true);

                } else if (baseTerritoryList.isEmpty()) {

                    Log.d("Switch tab", "Territory list is empty");

                    fetchTrendingTerritories(10, 1, false, true);

                } else {

                    Log.d("Switch tab", "Territory list not empty");

                    territoryListAdapter = new TerritoryListAdapter(SearchActivity.this, baseTerritoryList);

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

    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

        // Cancel all pending network requests

        CancelableCallback.cancelAll();

    }

}
