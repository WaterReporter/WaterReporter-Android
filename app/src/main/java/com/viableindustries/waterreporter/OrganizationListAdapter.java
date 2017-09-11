package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationHolder;

import java.util.ArrayList;


public class OrganizationListAdapter extends ArrayAdapter<Organization> implements Filterable {

    private final Context mContext;

    protected String name;

    protected int id;

    private ArrayList<Organization> sourceList;

    private ArrayList<Organization> filteredList;

    private OrganizationFilter mFilter;

    public OrganizationListAdapter(Context aContext, ArrayList<Organization> features, boolean aShowLeaveButton) {

        super(aContext, 0, features);

        this.sourceList = features;

        this.filteredList = features;

        this.mContext = aContext;

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
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

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

        Picasso.with(mContext).load(organization.properties.picture).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(viewHolder.organizationLogo);

        // Add click listeners to layout elements

        viewHolder.organizationItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OrganizationHolder.setOrganization(organization);

                mContext.startActivity(new Intent(mContext, OrganizationProfileActivity.class));

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