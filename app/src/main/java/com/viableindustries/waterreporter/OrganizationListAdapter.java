package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationHolder;
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

    public OrganizationListAdapter(Context context, ArrayList<Organization> features, boolean aShowLeaveButton) {

        super(context, 0, features);

        this.sourceList = features;

        this.filteredList = features;

        this.context = context;

    }

    private static class ViewHolder {
        ImageView organizationLogo;
        TextView organizationName;
        LinearLayout organizationItem;
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

            mFilter = new OrganizationFilter();

        }

        return mFilter;

    }

    @Override
    @NonNull
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.organization_list_item, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.organizationLogo = (ImageView) convertView.findViewById(R.id.organizationLogo);
            viewHolder.organizationName = (TextView) convertView.findViewById(R.id.organizationName);
            viewHolder.organizationItem = (LinearLayout) convertView.findViewById(R.id.organizationItem);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) convertView.getTag();

        }

        final Organization organization = filteredList.get(position);

        // Populate layout elements

        viewHolder.organizationName.setText(organization.properties.name);

        Picasso.with(context).load(organization.properties.picture).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(viewHolder.organizationLogo);

        // Add click listeners to layout elements

        viewHolder.organizationItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OrganizationHolder.setOrganization(organization);

                context.startActivity(new Intent(context, OrganizationProfileActivity.class));

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