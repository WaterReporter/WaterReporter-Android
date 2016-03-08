package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
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

    //protected SharedPreferences membershipPrefs;

    protected String[] userGroups;

    //protected Button joinGroupButton;

    //protected Button leaveGroupButton;

    protected Button groupMembershipButton;

    protected boolean showLeaveButton;

    public OrganizationListAdapter(Context context, ArrayList<Organization> features, boolean aShowLeaveButton) {

        super(context, 0, features);

        this.sourceList = features;

        this.filteredList = features;

        this.context = context;

        prefs = context.getSharedPreferences(context.getPackageName(), 0);

        //membershipPrefs = context.getSharedPreferences(context.getString(R.string.group_membership_key), 0);

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

    private void changeOrgStatus(final Organization organization, final View view) {

        final String operation = (view.getTag().equals("join_group")) ? "add" : "remove";

        // Retrieve API token

        final String access_token = prefs.getString("access_token", "");

        // Retrieve user ID

        int id = prefs.getInt("user_id", 0);

        // Build request object

        Map<String, Map> userPatch = UserOrgPatch.buildRequest(organization.id, operation);

        UserService service = UserService.restAdapter.create(UserService.class);

        service.updateUserOrganization(access_token, "application/json", id, userPatch, new Callback<User>() {

            @Override
            public void success(User user, Response response) {

                upDatePrefs(user);

                String action;

                if (operation.equals("add")) {

                    action = "joined";

                    ((Button) view).setText(R.string.leave_button);

                    view.setBackgroundResource(R.drawable.orange_button);

                    view.setTag("leave_group");

                } else {

                    action = "left";

                    ((Button) view).setText(R.string.join_button);

                    view.setBackgroundResource(R.drawable.green_button);

                    view.setTag("join_group");

                }

                CharSequence text = String.format("Successfully %s %s", action, organization.properties.name);
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

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

        groupMembershipButton = (Button) convertView.findViewById(R.id.group_membership_button);

        groupMembershipButton.setTag("join_group");

        // Check for stored group IDs and compare to the global list
        // We can't use group names as keys since they are subject to change

        String userGroupString = prefs.getString("user_groups", "");

        if (userGroupString.length() > 0) {

            userGroups = userGroupString.split(",");

            if (Arrays.asList(userGroups).contains(String.valueOf(id)) && showLeaveButton) {

                groupMembershipButton.setText(R.string.leave_button);

                groupMembershipButton.setBackgroundResource(R.drawable.orange_button);

                groupMembershipButton.setTag("leave_group");

            }

        }

        // Add click listener to membership button
        groupMembershipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeOrgStatus(feature, v);

            }
        });

        // Lookup view for data population
        TextView siteName = (TextView) convertView.findViewById(R.id.organization_name);

        siteName.setTag(id);

        siteName.setText(name);

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