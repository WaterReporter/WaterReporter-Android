package com.viableindustries.waterreporter.user_interface.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.models.group.Group;
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.api.models.user.UserMembershipPatch;
import com.viableindustries.waterreporter.utilities.CircleTransform;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by brendanmcintyre on 10/5/16.
 */

public class GroupActionListAdapter extends ArrayAdapter<Organization> implements Filterable {

    private final Context mContext;

    protected String name;

    protected int id;

    private final ArrayList<Organization> sourceList;

    private ArrayList<Organization> filteredList;

    private GroupActionListAdapter.OrganizationFilter mFilter;

    private final SharedPreferences mSharedPreferences;

    private final SharedPreferences groupMembership;

    private String[] userGroups;

    public GroupActionListAdapter(Context aContext, ArrayList<Organization> features, boolean aShowLeaveButton) {

        super(aContext, 0, features);

        this.sourceList = features;

        this.filteredList = features;

        this.mContext = aContext;

        mSharedPreferences = mContext.getSharedPreferences(mContext.getPackageName(), 0);

        groupMembership = mContext.getSharedPreferences(mContext.getString(R.string.group_membership_key), 0);

        boolean showLeaveButton = aShowLeaveButton;

    }

    private static class ViewHolder {
        ImageView organizationLogo;
        TextView organizationName;
        LinearLayout organizationItem;
        Button groupMembershipButton;
    }

    public int getCount() {

        return filteredList.size();

    }

    public Organization getItem(int position) {

        return filteredList.get(position);

    }

    @NonNull
    public Filter getFilter() {

        // TODO Auto-generated method stub

        if (mFilter == null) {

            mFilter = new GroupActionListAdapter.OrganizationFilter();

        }

        return mFilter;

    }

    private void upDatePrefs(User user) {

        List<Organization> organizations = user.properties.organizations;

        String orgIds = "";

        if (!organizations.isEmpty()) {

            for (Organization organization : organizations) {

                orgIds += String.format(",%s", organization.id);

            }

        }

        mSharedPreferences.edit().putString("user_groups", orgIds).apply();

    }

//    private List<Map<String, Object>> buildGroupPatch(int organizationId, int userId) {
//
//        User user = ModelStorage.getStoredUser(mSharedPreferences);
//
//        List<Map<String, Object>> groupObjects = new ArrayList<>();
//
//        if (user.properties.gro)
//
//            return groupObjects;
//
//    }

    private void changeOrgStatus(final Organization organization, final View view) {

        final String operation = (view.getTag().equals("join_group")) ? "add" : "remove";

        // Retrieve API token

        final String accessToken = mSharedPreferences.getString("access_token", "");

        // Retrieve user ID

        int id = mSharedPreferences.getInt("user_id", 0);

        // Build request object

        List<Group> currentGroups = new ArrayList<>();

        Map<String, ?> storedGroups = groupMembership.getAll();

        Iterator it = storedGroups.entrySet().iterator();

        while (it.hasNext()) {

            Map.Entry pair = (Map.Entry)it.next();

            System.out.println(pair.getKey() + " = " + pair.getValue());

            currentGroups.add(ModelStorage.getStoredGroup(groupMembership, pair.getKey().toString()));

            it.remove(); // avoids a ConcurrentModificationException

        }

        Map<String, List> userPatch = UserMembershipPatch.buildRequest(currentGroups, id, organization.id, operation);

        RestClient.getUserService().updateUserMemberships(accessToken, "application/json", id, userPatch, new Callback<User>() {

            @Override
            public void success(User user, Response response) {

                // Reset the user's stored groups.

                groupMembership.edit().clear().apply();

                if (user.properties.groups.size() > 0) {

                    for (Group group : user.properties.groups) {

                        ModelStorage.storeModel(groupMembership, group, group.properties.organization.properties.name);

                    }

                }

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

                Snackbar.make(view.getRootView(), text,
                        Snackbar.LENGTH_SHORT)
                        .show();

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
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.group_action_list_item, parent, false);

            viewHolder = new GroupActionListAdapter.ViewHolder();

            viewHolder.organizationLogo = (ImageView) convertView.findViewById(R.id.organizationLogo);
            viewHolder.organizationName = (TextView) convertView.findViewById(R.id.organizationName);
            viewHolder.organizationItem = (LinearLayout) convertView.findViewById(R.id.organizationItem);
            viewHolder.groupMembershipButton = (Button) convertView.findViewById(R.id.group_membership_button);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (GroupActionListAdapter.ViewHolder) convertView.getTag();

        }

        final Organization organization = filteredList.get(position);

        // Populate layout elements

        viewHolder.organizationName.setText(organization.properties.name);

        Picasso.with(mContext).load(organization.properties.picture).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(viewHolder.organizationLogo);

        // Check group membership

        Group targetGroup = ModelStorage.getStoredGroup(groupMembership, organization.properties.name);

        try {

            int selected = targetGroup.properties.organizationId;

            viewHolder.groupMembershipButton.setTag("leave_group");

            viewHolder.groupMembershipButton.setText(R.string.leave_button);

            viewHolder.groupMembershipButton.setBackgroundResource(R.drawable.orange_button);

        } catch (NullPointerException e) {

            viewHolder.groupMembershipButton.setTag("join_group");

            viewHolder.groupMembershipButton.setText(R.string.join_button);

            viewHolder.groupMembershipButton.setBackgroundResource(R.drawable.green_button);

        }

        viewHolder.groupMembershipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeOrgStatus(organization, v);

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