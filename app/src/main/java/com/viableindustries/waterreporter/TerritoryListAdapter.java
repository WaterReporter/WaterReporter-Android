package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viableindustries.waterreporter.data.HucStates;
import com.viableindustries.waterreporter.data.Territory;
import com.viableindustries.waterreporter.data.TerritoryHolder;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 2/21/17.
 */

public class TerritoryListAdapter extends ArrayAdapter<Territory> {

    private final Context mContext;

    protected String name;

    protected int id;

    private ArrayList<Territory> sourceList;

    public TerritoryListAdapter(Context aContext, ArrayList<Territory> features, boolean aShowLeaveButton) {

        super(aContext, 0, features);

        this.sourceList = features;

        this.mContext = aContext;

    }

    private static class ViewHolder {
        ImageView territoryIcon;
        TextView territoryName;
        TextView territoryStates;
        LinearLayout territoryItem;
    }

    public Territory getItem(int position) {

        return sourceList.get(position);

    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        TerritoryListAdapter.ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_territory_list_item, parent, false);

            viewHolder = new TerritoryListAdapter.ViewHolder();

            viewHolder.territoryIcon = (ImageView) convertView.findViewById(R.id.territoryIcon);
            viewHolder.territoryName = (TextView) convertView.findViewById(R.id.territoryName);
            viewHolder.territoryStates = (TextView) convertView.findViewById(R.id.territoryStates);
            viewHolder.territoryItem = (LinearLayout) convertView.findViewById(R.id.territoryItem);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (TerritoryListAdapter.ViewHolder) convertView.getTag();

        }

        final Territory territory = sourceList.get(position);

        // Populate layout elements

        viewHolder.territoryName.setText(territory.properties.huc_8_name);

        viewHolder.territoryStates.setText(HucStates.STATES.get(territory.properties.huc_8_code));

        // Add click listeners to layout elements

        viewHolder.territoryItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TerritoryHolder.setTerritory(territory);

                mContext.startActivity(new Intent(mContext, TerritoryActivity.class));

            }
        });

        return convertView;

    }

}
