package com.viableindustries.waterreporter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.utilities.ModelStorage;
import com.viableindustries.waterreporter.utilities.QueryBuilder;

import static android.content.Context.MODE_PRIVATE;

public class NavigationFragment extends Fragment {
    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.bottom_navigation, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        // Setup any handles to view objects here

        final Activity activity = getActivity();

        final SharedPreferences coreProfile = getContext().getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

        final int coreId = coreProfile.getInt("id", 0);

        LinearLayout timelineTab = (LinearLayout) view.findViewById(R.id.timeline);
        LinearLayout searchTab = (LinearLayout) view.findViewById(R.id.search);
        LinearLayout submitTab = (LinearLayout) view.findViewById(R.id.submit);
        LinearLayout profileTab = (LinearLayout) view.findViewById(R.id.profile);

        final ImageView timelineIcon = (ImageView) view.findViewById(R.id.timeline_icon);
        final ImageView searchIcon = (ImageView) view.findViewById(R.id.search_icon);
        final ImageView submitIcon = (ImageView) view.findViewById(R.id.submit_icon);
        final ImageView profileIcon = (ImageView) view.findViewById(R.id.profile_icon);

        if ("AuthUserActivity".equals(activity.getClass().getSimpleName())) {

            timelineIcon.setAlpha(Float.valueOf("0.4"));
            searchIcon.setAlpha(Float.valueOf("0.4"));
            submitIcon.setAlpha(Float.valueOf("0.4"));
            profileIcon.setAlpha(Float.valueOf("0.8"));

        } else if ("MainActivity".equals(activity.getClass().getSimpleName())) {

            timelineIcon.setAlpha(Float.valueOf("0.8"));
            searchIcon.setAlpha(Float.valueOf("0.4"));
            submitIcon.setAlpha(Float.valueOf("0.4"));
            profileIcon.setAlpha(Float.valueOf("0.4"));

        }

        timelineTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context context = getContext();

                Log.d("context", context.toString());

                // If the current activity is the main feed, simply refresh the timeline

                if ("MainActivity".equals(activity.getClass().getSimpleName())) {

                    Log.d("activity", activity.getClass().getSimpleName());

                    final SwipeRefreshLayout timeline = (SwipeRefreshLayout) activity.findViewById(R.id.timeline);

                    timeline.setRefreshing(true);

                    ((MainActivity) activity).fetchPosts(5, 1, true);

                } else {

                    startActivity(new Intent(activity, MainActivity.class));

                }

            }
        });

        searchTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                timelineIcon.setAlpha(Float.valueOf("0.4"));
                searchIcon.setAlpha(Float.valueOf("0.8"));
                submitIcon.setAlpha(Float.valueOf("0.4"));
                profileIcon.setAlpha(Float.valueOf("0.4"));

                startActivity(new Intent(activity, SearchActivity.class));

            }
        });

        submitTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, PhotoMetaActivity.class);

                if ("TagProfileActivity".equals(activity.getClass().getSimpleName())) {

                    Log.d("activityContext", activity.getClass().getSimpleName());

                    final TextView tagName = (TextView) activity.findViewById(R.id.tag_name);

                    intent.putExtra("autoTag", tagName.getText().toString());

                }

                if ("OrganizationProfileActivity".equals(activity.getClass().getSimpleName())) {

                    Log.d("activityContext", activity.getClass().getSimpleName());

                    final TextView tagName = (TextView) activity.findViewById(R.id.organizationName);

                    intent.putExtra("autoTag", String.format("\u0023%s", tagName.getText().toString().replaceAll("[^a-zA-Z0-9]+", "")));

                }

                if ("TerritoryActivity".equals(activity.getClass().getSimpleName())) {

                    Log.d("activityContext", activity.getClass().getSimpleName());

                    final TextView tagName = (TextView) activity.findViewById(R.id.territoryName);

                    intent.putExtra("autoTag", String.format("\u0023%s", tagName.getText().toString().replaceAll("[^a-zA-Z0-9]+", "")));

                }

                timelineIcon.setAlpha(Float.valueOf("0.4"));
                searchIcon.setAlpha(Float.valueOf("0.4"));
                submitIcon.setAlpha(Float.valueOf("0.8"));
                profileIcon.setAlpha(Float.valueOf("0.4"));

                startActivity(intent);

                activity.overridePendingTransition(R.anim.animation_enter_right,
                        R.anim.animation_exit_left);

            }
        });

        profileTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // If the current activity is the authenticated user's profile, simply refresh the timeline

                if ("AuthUserActivity".equals(activity.getClass().getSimpleName())) {

                    Log.d("activity", activity.getClass().getSimpleName());

                    final SwipeRefreshLayout timeline = (SwipeRefreshLayout) activity.findViewById(R.id.timeline);

                    timeline.setRefreshing(true);

                    ((AuthUserActivity) activity).fetchPosts(5, 1, QueryBuilder.userQuery(true, coreId, null), true);

                } else {

                    final SharedPreferences coreProfile = getContext().getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

                    User authUser = ModelStorage.getStoredUser(coreProfile, "auth_user");

                    try {

                        SharedPreferences sharedPreferences = getContext().getSharedPreferences(getContext().getPackageName(), MODE_PRIVATE);

                        ModelStorage.storeModel(sharedPreferences, authUser, "stored_user");

                        startActivity(new Intent(activity, AuthUserActivity.class));

                    } catch (NullPointerException e) {

                        startActivity(new Intent(activity, SignInActivity.class));

                    }

                }

            }
        });

    }
}