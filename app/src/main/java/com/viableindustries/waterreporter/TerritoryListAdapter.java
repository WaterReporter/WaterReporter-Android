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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationHolder;
import com.viableindustries.waterreporter.data.Territory;
import com.viableindustries.waterreporter.data.TerritoryHolder;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 2/21/17.
 */

public class TerritoryListAdapter extends ArrayAdapter<Territory> {

    private final Context context;

    protected String name;

    protected int id;

    private ArrayList<Territory> sourceList;

//    private ArrayList<Organization> filteredList;

//    private OrganizationListAdapter.OrganizationFilter mFilter;

    public TerritoryListAdapter(Context aContext, ArrayList<Territory> features, boolean aShowLeaveButton) {

        super(aContext, 0, features);

        this.sourceList = features;

//        this.filteredList = features;

        this.mContext = aContext;

    }

    private static class ViewHolder {
        ImageView territoryIcon;
        TextView territoryName;
        LinearLayout territoryItem;
    }

//    public int getCount() {
//
//        return filteredList.size();
//
//    }

    public Territory getItem(int position) {

        return sourceList.get(position);

    }

//    @NonNull
//    public Filter getFilter() {
//
//        // TODO Auto-generated method stub
//
//        if (mFilter == null) {
//
//            mFilter = new OrganizationListAdapter.OrganizationFilter();
//
//        }
//
//        return mFilter;
//
//    }

    @Override
    @NonNull
    public View getView(int position, View convertView, ViewGroup parent) {

        TerritoryListAdapter.ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.territory_list_item, parent, false);

            viewHolder = new TerritoryListAdapter.ViewHolder();

            viewHolder.territoryIcon = (ImageView) convertView.findViewById(R.id.territoryIcon);
            viewHolder.territoryName = (TextView) convertView.findViewById(R.id.territoryName);
            viewHolder.territoryItem = (LinearLayout) convertView.findViewById(R.id.territoryItem);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (TerritoryListAdapter.ViewHolder) convertView.getTag();

        }

        final Territory territory = sourceList.get(position);

        // Populate layout elements

        viewHolder.territoryName.setText(territory.properties.huc_8_name);

//        Picasso.with(context).load(territory.properties.picture).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(viewHolder.organizationLogo);

        // Add click listeners to layout elements

        viewHolder.territoryItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TerritoryHolder.setTerritory(territory);

                context.startActivity(new Intent(context, TerritoryActivity.class));

            }
        });

        return convertView;

    }

//    private class OrganizationFilter extends Filter {
//
//        @Override
//        protected FilterResults performFiltering(CharSequence constraint) {
//
//            FilterResults results = new FilterResults();
//
//            // Perform filtering operation
//            // May need to implement partial/fuzzy matching as the number of organizations grows
//
//            ArrayList<Organization> nOrgList = new ArrayList<>();
//
//            for (Organization org : sourceList) {
//
//                if (org.properties.name.toUpperCase().startsWith(constraint.toString().toUpperCase())) {
//
//                    Log.d("name", org.properties.name);
//
//                    nOrgList.add(org);
//
//                }
//
//            }
//
//            results.values = nOrgList;
//
//            results.count = nOrgList.size();
//
//            return results;
//
//        }
//
//        // Probably not the best idea, need to find a better solution
//        @SuppressWarnings("unchecked")
//        @Override
//        protected void publishResults(CharSequence constraint,
//                                      FilterResults results) {
//
//            // Inform the adapter about the new filtered list
//
//            if (results.count == 0) {
//
//                notifyDataSetInvalidated();
//
//            } else {
//
//                filteredList = (ArrayList<Organization>) results.values;
//
//                notifyDataSetChanged();
//
//            }
//
//        }
//
//    }

}
