package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.Gson;
import com.viableindustries.waterreporter.data.CancelableCallback;
import com.viableindustries.waterreporter.data.QueryFilter;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserCollection;
import com.viableindustries.waterreporter.data.UserService;

import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by brendanmcintyre on 2/10/17.
 */

public class UserSearchFragment extends Fragment {

    private Context context;

    private RestAdapter restAdapter;

    private UserService service;

    private final List<User> memberCollection = new ArrayList<>();

    private UserListAdapter listAdapter;

    private ListView resultList;

    // newInstance constructor for creating fragment with arguments
    public static UserSearchFragment newInstance() {

        return new UserSearchFragment();

    }

    private void attachScrollListener() {

        resultList.setOnScrollListener(new EndlessScrollListener() {

            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new data needs to be appended to the list

                fetchUsers(10, page, buildQuery());

                return true; // ONLY if more data is actually being loaded; false otherwise.

            }

        });

    }

    private String buildQuery() {

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<>();

        QuerySort querySort = new QuerySort("last_name", "asc");

        queryOrder.add(querySort);

        // Create filter list and add a filter parameter

        List<Object> queryFilters = new ArrayList<>();

//        QueryFilter complexVal = new QueryFilter("id", "eq", organization.id);
//
        QueryFilter userFilter = new QueryFilter("picture", "is_not_null", null);
//
        queryFilters.add(userFilter);

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(queryFilters, queryOrder);

        return new Gson().toJson(queryParams);

    }

    private void fetchUsers(int limit, int page, final String query) {

        final SharedPreferences prefs =
                context.getSharedPreferences(context.getPackageName(), MODE_PRIVATE);

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        service.getUsers(accessToken, "application/json", page, limit, query, new CancelableCallback<UserCollection>() {

            @Override
            public void onSuccess(UserCollection userCollection, Response response) {

                ArrayList<User> users = userCollection.getFeatures();

                if (!users.isEmpty()) {

                    memberCollection.addAll(users);

                    listAdapter = new UserListAdapter(context, users, true);

                    resultList.setAdapter(listAdapter);

//                    attachScrollListener();

                }

            }

            @Override
            public void onFailure(RetrofitError error) {

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

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        restAdapter = UserService.restAdapter;

        service = restAdapter.create(UserService.class);

        context = getActivity();

        fetchUsers(10, 1, buildQuery());

    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.search_result_list, container, false);

        resultList = (ListView) view.findViewById(R.id.resultList);

//        restAdapter = UserService.restAdapter;
//
//        service = restAdapter.create(UserService.class);
//
//        context = getActivity();
//
//        fetchUsers(10, 1, buildQuery());

        return view;

    }

}
