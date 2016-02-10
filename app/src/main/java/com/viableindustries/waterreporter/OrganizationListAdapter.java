package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserOrgPatch;
import com.viableindustries.waterreporter.data.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class OrganizationListAdapter extends ArrayAdapter<Organization> implements Filterable {

    private final Context context;

    protected String name;

    protected int id;

    private ArrayList<Organization> sourceList;

    private ArrayList<Organization> filteredList;

    private OrganizationFilter mFilter;

    protected SharedPreferences prefs;

    protected String[] userGroups;

    protected Button joinGroupButton;

    protected Button leaveGroupButton;

    protected boolean showLeaveButton;

    public OrganizationListAdapter(Context context, ArrayList<Organization> features, boolean aShowLeaveButton) {

        super(context, 0, features);

        this.sourceList = features;

        this.filteredList = features;

        this.context = context;

        prefs = context.getSharedPreferences(context.getPackageName(), 0);

        showLeaveButton = aShowLeaveButton;

    }

    public int getCount() {

        return filteredList.size();

    }

    public Organization getItem(int position) {

        return filteredList.get(position);

    }

    public Filter getFilter() {

        // TODO Auto-generated method stub

        if (mFilter == null) {

            mFilter = new OrganizationFilter();

        }

        return mFilter;

    }

    protected void upDatePrefs(User user) {

        List<Organization> organizations = user.properties.organizations;

        String orgIds = "";

        if (!organizations.isEmpty()) {

            for (Organization organization : organizations) {

                orgIds += String.format(",%s", organization.id);

            }

        }

        prefs.edit().putString("user_groups", orgIds).apply();

    }

    private void changeOrgStatus(final Organization organization, final boolean selected) {

        // Retrieve API token

        final String access_token = prefs.getString("access_token", "");

        // Retrieve user ID

        int id = prefs.getInt("user_id", 0);

        // Build request object

        Map<String, Map> userPatch = UserOrgPatch.buildRequest(organization.id, selected);

        UserService service = UserService.restAdapter.create(UserService.class);

        service.updateUserOrganization(access_token, "application/json", id, userPatch, new Callback<User>() {

            @Override
            public void success(User user, Response response) {

                upDatePrefs(user);

                String action = selected ? "left" : "joined";

                CharSequence text = String.format("Successfully %s %s", action, organization.properties.name);
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                notifyDataSetChanged();

            }

            @Override
            public void failure(RetrofitError error) {

                Response response = error.getResponse();

                int status = response.getStatus();

                error.printStackTrace();

            }

        });

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Organization feature = filteredList.get(position);

        name = feature.properties.name;

        id = feature.id;

        Log.d("orgid", String.valueOf(id));

        // Check if an existing view is being reused, otherwise inflate the view

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.organization_list_item, parent, false);
        }

        // Layout elements

        joinGroupButton = (Button) convertView.findViewById(R.id.join_group_button);

        leaveGroupButton = (Button) convertView.findViewById(R.id.leave_group_button);

        // Check for stored group IDs and compare to the global list

        String userGroupString = prefs.getString("user_groups", "");

        joinGroupButton.setVisibility(View.VISIBLE);

        leaveGroupButton.setVisibility(View.GONE);

        if (userGroupString.length() > 0) {

            userGroups = userGroupString.split(",");

            if (Arrays.asList(userGroups).contains(String.valueOf(id))) {

                joinGroupButton.setVisibility(View.GONE);

                if (showLeaveButton) {

                    leaveGroupButton.setVisibility(View.VISIBLE);

                }

            }

        }

        // Lookup view for data population
        TextView siteName = (TextView) convertView.findViewById(R.id.organization_name);

        siteName.setTag(id);

        siteName.setText(name);

        joinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeOrgStatus(feature, false);

                if (!showLeaveButton) {

                    v.setVisibility(View.GONE);

                }

            }
        });

        leaveGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeOrgStatus(feature, true);
            }
        });

        return convertView;

    }

    private class OrganizationFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            // Perform filtering operation
            // May need to implement partial/fuzzy matching as the number of organizations grows

            ArrayList<Organization> nOrgList = new ArrayList<>();

            for (Organization org : sourceList) {

                if (org.properties.name.toUpperCase().startsWith(constraint.toString().toUpperCase())) {

                    Log.d("name", org.properties.name);

                    nOrgList.add(org);

                }

            }

            results.values = nOrgList;

            results.count = nOrgList.size();

            return results;

        }

        // Probably not the best idea, need to find a better solution
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            // Inform the adapter about the new filtered list

            if (results.count == 0) {

                notifyDataSetInvalidated();

            } else {

                filteredList = (ArrayList<Organization>) results.values;

                notifyDataSetChanged();

            }

        }

    }

}