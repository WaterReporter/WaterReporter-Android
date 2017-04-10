package com.viableindustries.waterreporter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.viableindustries.waterreporter.data.FeatureCollection;
import com.viableindustries.waterreporter.data.GroupNameComparator;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.QueryFilter;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.UserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by brendanmcintyre on 8/16/16.
 */

public class UserFeedFragment extends Fragment {

    private int userId;

    protected RestAdapter restAdapter = ReportService.restAdapter;

    protected ReportService service = restAdapter.create(ReportService.class);

    protected TimelineAdapter timelineAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ListView timeLine;

    protected List<Report> reportCollection = new ArrayList<Report>();

    private void attachScrollListener() {

        timeLine.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new data needs to be appended to the list
                fetchReports(userId, 10, page, false);

                return true; // ONLY if more data is actually being loaded; false otherwise.

            }
        });

    }

    private void fetchReports(int userId, int limit, int page, final boolean refresh) {

        SharedPreferences prefs =
                getActivity().getSharedPreferences(getActivity().getPackageName(), MODE_PRIVATE);

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<QuerySort>();

        QuerySort querySort = new QuerySort("created", "desc");

        queryOrder.add(querySort);

        // Create filter list and add a filter parameter

        List<Object> queryFilters = new ArrayList<>();

        QueryFilter queryFilter = new QueryFilter("owner_id", "eq", userId);

        queryFilters.add(queryFilter);

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(queryFilters, queryOrder);

        String query = new Gson().toJson(queryParams);

        Log.d("URL", query);

        service.getReports(accessToken, "application/json", page, limit, query, new Callback<FeatureCollection>() {

            @Override
            public void success(FeatureCollection featureCollection, Response response) {

                List<Report> reports = featureCollection.getFeatures();

                Log.v("list", reports.toString());

                if (!reports.isEmpty()) {

                    reportCollection.addAll(reports);

                    try {

                        timelineAdapter.notifyDataSetChanged();

                    } catch (NullPointerException ne) {

                        populateTimeline(reportCollection);

                    }

                } else {

                    CharSequence text = "Your report collection is empty. Tap on the plus sign in the menu bar to start a new report.";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(getContext(), text, duration);
                    toast.show();

                }

                if (refresh) {

                    reportCollection = reports;

                    populateTimeline(reportCollection);

                }

                swipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void failure(RetrofitError error) {

                swipeRefreshLayout.setRefreshing(false);

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(getActivity(), SignInActivity.class));

                    }

                }

            }

        });

    }

    private void populateTimeline(List list) {

        timelineAdapter = new TimelineAdapter(getActivity(), list, true);

        // Attach the adapter to a ListView
        timeLine.setAdapter(timelineAdapter);

        attachScrollListener();

    }

    // newInstance constructor for creating fragment with arguments
    public static UserFeedFragment newInstance(int userId) {
        UserFeedFragment userFeedFragment = new UserFeedFragment();
        Bundle args = new Bundle();
        args.putInt("userId", userId);
        userFeedFragment.setArguments(args);
        return userFeedFragment;
    }

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.user_feed_fragment, parent, false);

    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        userId = getArguments().getInt("userId", 0);

        // Setup any handles to view objects here
        timeLine = (ListView) view.findViewById(R.id.timeline_items);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.timeline);

        fetchReports(userId, 10, 1, false);

    }

}
