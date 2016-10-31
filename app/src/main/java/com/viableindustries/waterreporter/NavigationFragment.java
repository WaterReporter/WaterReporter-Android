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

        LinearLayout feedTab = (LinearLayout) view.findViewById(R.id.feed);
        LinearLayout submitTab = (LinearLayout) view.findViewById(R.id.submit);
        LinearLayout profileTab = (LinearLayout) view.findViewById(R.id.profile);

        feedTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context context = getContext();

                Log.d("context", context.toString());

                if (context.toString().contains("MainActivity")) {

                    ListView listView = (ListView) getActivity().findViewById(R.id.timeline_items);

                    listView.smoothScrollToPosition(0);

                } else {

                    startActivity(new Intent(getActivity(), MainActivity.class));

                }

            }
        });

        submitTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PhotoMetaActivity.class));
            }
        });

        profileTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final SharedPreferences coreProfile = getContext().getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

                int coreId = coreProfile.getInt("id", 0);

//                public UserProperties (String aDescription, int aId, String aLastName,
//                        String aFirstName, ArrayList<Organization> aOrganizations,
//                        String aOrganizationName, String aPicture, String aPublicEmail, String aTitle){

                // UserProperties userProperties = (UserProperties) coreProfile.getAll();

                UserProperties userProperties = new UserProperties(coreId, coreProfile.getString("description", ""),
                        coreProfile.getString("first_name", ""), coreProfile.getString("last_name", ""),
                        coreProfile.getString("organization_name", ""), coreProfile.getString("picture", null),
                        coreProfile.getString("public_email", ""), coreProfile.getString("title", ""), null, null, null);

                User coreUser = User.createUser(coreId, userProperties);

                UserHolder.setUser(coreUser);

//                Map<String,?> keys = coreProfile.getAll();
//
//                for(Map.Entry<String,?> entry : keys.entrySet()){
//                    Log.d("map values",entry.getKey() + ": " +
//                            entry.getValue().toString());
//                }

                startActivity(new Intent(getActivity(), UserProfileActivity.class));

            }
        });

    }
}