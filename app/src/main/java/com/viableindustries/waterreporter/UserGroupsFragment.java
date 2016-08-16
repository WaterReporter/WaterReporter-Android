package com.viableindustries.waterreporter;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.GroupNameComparator;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.UserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by brendanmcintyre on 8/16/16.
 */

public class UserGroupsFragment extends android.support.v4.app.Fragment {

    ListView groupList;

    private void populateOrganizations(ArrayList<Organization> orgs) {

        final OrganizationListAdapter adapter = new OrganizationListAdapter(getActivity(), orgs, true);

        groupList.setAdapter(adapter);

    }

    protected void fetchUserGroups(int userId) {

        final SharedPreferences prefs =
                getActivity().getSharedPreferences(getActivity().getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        Log.d("", access_token);

        UserService service = UserService.restAdapter.create(UserService.class);

        service.getUserOrganization(access_token, "application/json", userId, new Callback<OrganizationFeatureCollection>() {

            @Override
            public void success(OrganizationFeatureCollection organizationCollectionResponse, Response response) {

                ArrayList<Organization> organizations = organizationCollectionResponse.getFeatures();

                String orgIds = "";

                if (!organizations.isEmpty()) {

                    for (Organization organization : organizations) {

                        //orgIds += String.format(",%s", organization.id);
                        Log.d("orgName", organization.properties.name);

                    }

                    Collections.sort(organizations, new GroupNameComparator());

                    populateOrganizations(organizations);

//                    for (Organization organization : organizations) {
//
//                        orgIds += String.format(",%s", organization.id);
//
//                    }

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

                        startActivity(new Intent(getActivity(), SignInActivity.class));

                    }

                }

            }

        });

    }

    // newInstance constructor for creating fragment with arguments
    public static UserGroupsFragment newInstance(int userId) {
        UserGroupsFragment userGroupsFragment = new UserGroupsFragment();
        Bundle args = new Bundle();
        args.putInt("userId", userId);
        userGroupsFragment.setArguments(args);
        return userGroupsFragment;
    }

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.user_group_list, parent, false);

    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        int userId = getArguments().getInt("userId", 0);

        // Setup any handles to view objects here
        groupList = (ListView) view.findViewById(R.id.groupList);

        fetchUserGroups(userId);

    }

}
