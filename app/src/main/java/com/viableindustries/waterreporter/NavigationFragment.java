package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserHolder;
import com.viableindustries.waterreporter.data.UserProperties;

import java.util.Map;

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

        LinearLayout timelineTab = (LinearLayout) view.findViewById(R.id.timeline);
        LinearLayout searchTab = (LinearLayout) view.findViewById(R.id.search);
        LinearLayout submitTab = (LinearLayout) view.findViewById(R.id.submit);
        LinearLayout profileTab = (LinearLayout) view.findViewById(R.id.profile);

        final ImageView timelineIcon = (ImageView) view.findViewById(R.id.timeline_icon);
        final ImageView searchIcon = (ImageView) view.findViewById(R.id.search_icon);
        final ImageView submitIcon = (ImageView) view.findViewById(R.id.submit_icon);
        final ImageView profileIcon = (ImageView) view.findViewById(R.id.profile_icon);

        timelineTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context context = getContext();

                Log.d("context", context.toString());

                timelineIcon.setAlpha(Float.valueOf("0.8"));
                searchIcon.setAlpha(Float.valueOf("0.4"));
                submitIcon.setAlpha(Float.valueOf("0.4"));
                profileIcon.setAlpha(Float.valueOf("0.4"));

                // If the current activity is the main feed, simply refresh the timeline

                if ("MainActivity".equals(getActivity().getClass().getSimpleName())) {

                    Log.d("activity", getActivity().getClass().getSimpleName());

                    final SwipeRefreshLayout timeline = (SwipeRefreshLayout) getActivity().findViewById(R.id.timeline);

                    timeline.setRefreshing(true);

                    ((MainActivity) getActivity()).requestData(5, 1, false, true);

                } else {

                    startActivity(new Intent(getActivity(), MainActivity.class));

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

                startActivity(new Intent(getActivity(), SearchActivity.class));

            }
        });

        submitTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                timelineIcon.setAlpha(Float.valueOf("0.4"));
                searchIcon.setAlpha(Float.valueOf("0.4"));
                submitIcon.setAlpha(Float.valueOf("0.8"));
                profileIcon.setAlpha(Float.valueOf("0.4"));

                startActivity(new Intent(getActivity(), PhotoMetaActivity.class));

            }
        });

        profileTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final SharedPreferences coreProfile = getContext().getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

                int coreId = coreProfile.getInt("id", 0);

                Log.d("avatar", coreProfile.getString("picture", ""));

                timelineIcon.setAlpha(Float.valueOf("0.4"));
                searchIcon.setAlpha(Float.valueOf("0.4"));
                submitIcon.setAlpha(Float.valueOf("0.4"));
                profileIcon.setAlpha(Float.valueOf("0.8"));

                UserProperties userProperties = new UserProperties(coreId, coreProfile.getString("description", ""),
                        coreProfile.getString("first_name", ""), coreProfile.getString("last_name", ""),
                        coreProfile.getString("organization_name", ""), coreProfile.getString("picture", null),
                        coreProfile.getString("public_email", ""), coreProfile.getString("title", ""), null, null, null);

                User coreUser = User.createUser(coreId, userProperties);

                UserHolder.setUser(coreUser);

                startActivity(new Intent(getActivity(), UserProfileActivity.class));

            }
        });

    }
}