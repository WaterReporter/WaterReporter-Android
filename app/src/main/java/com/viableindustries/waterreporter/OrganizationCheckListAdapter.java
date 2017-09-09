package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.viableindustries.waterreporter.data.AbbreviatedOrganization;
import com.viableindustries.waterreporter.data.Organization;

import java.util.ArrayList;

//import com.vividsolutions.jts.awt.ShapeReader;

/**
 * Created by brendanmcintyre on 2/8/16.
 */
public class OrganizationCheckListAdapter extends ArrayAdapter<AbbreviatedOrganization> {

    private final Context mContext;

    protected String name;

    protected int id;

    private ArrayList<AbbreviatedOrganization> sourceList;

    protected SwitchCompat associateGroup;

    protected SharedPreferences prefs;

    protected SharedPreferences associatedGroups;

    public OrganizationCheckListAdapter(Context aContext, ArrayList<AbbreviatedOrganization> features) {

        super(aContext, 0, features);

        this.sourceList = features;

        this.mContext = aContext;

        prefs = mContext.getSharedPreferences(mContext.getPackageName(), 0);

        associatedGroups = mContext.getSharedPreferences(mContext.getString(R.string.associated_group_key), 0);

    }

    public int getCount() {

        return sourceList.size();

    }

    public AbbreviatedOrganization getItem(int position) {

        return sourceList.get(position);

    }

    private void updateGroupSelection(final Organization organization) {

        int selected = associatedGroups.getInt(organization.properties.name, 0);

        if (selected > 0) {

            associatedGroups.edit().putInt(organization.properties.name, 0).apply();

        } else {

            associatedGroups.edit().putInt(organization.properties.name, organization.properties.id).apply();

        }

        notifyDataSetChanged();

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final AbbreviatedOrganization feature = sourceList.get(position);

        name = feature.name;

        id = feature.id;

        Log.d("orgid", String.valueOf(id) + name);

        // Check if an existing view is being reused, otherwise inflate the view

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.organization_checklist_item, parent, false);

        }

        // Layout elements

        associateGroup = (SwitchCompat) convertView.findViewById(R.id.associate_group);

        associateGroup.setTag(String.format("%s__%s", name, id));

        // If editing an existing report, we need to set the selected state of the SelectCompat element

        int existingId = associatedGroups.getInt(feature.name, 0);

        if (existingId == feature.id) {

            associateGroup.setChecked(true);

        } else {

            associateGroup.setChecked(false);

        }

        // Set click listener

        associateGroup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                Log.d("Button Event", "Clicked the associate group button.");

                int selected = associatedGroups.getInt(feature.name, 0);

                Log.d("associated groups", selected + feature.name);

                if (selected > 0) {

                    associatedGroups.edit().putInt(feature.name, 0).apply();

                } else {

                    associatedGroups.edit().putInt(feature.name, feature.id).apply();

                }

            }

        });

        // Lookup view for data population
        TextView siteName = (TextView) convertView.findViewById(R.id.organization_name);

        siteName.setText(name);

        return convertView;

    }

}
