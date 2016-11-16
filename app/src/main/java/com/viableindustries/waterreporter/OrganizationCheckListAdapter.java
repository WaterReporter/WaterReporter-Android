package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserOrgPatch;
import com.viableindustries.waterreporter.data.UserService;
//import com.vividsolutions.jts.awt.ShapeReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by brendanmcintyre on 2/8/16.
 */
public class OrganizationCheckListAdapter extends ArrayAdapter<Organization> {

    private final Context context;

    protected String name;

    protected int id;

    private ArrayList<Organization> sourceList;

    protected SwitchCompat associateGroup;

    protected SharedPreferences prefs;

    protected SharedPreferences groupPrefs;

    public OrganizationCheckListAdapter(Context context, ArrayList<Organization> features) {

        super(context, 0, features);

        this.sourceList = features;

        this.context = context;

        prefs = context.getSharedPreferences(context.getPackageName(), 0);

        groupPrefs = context.getSharedPreferences(context.getString(R.string.associated_group_key), 0);

    }

    public int getCount() {

        return sourceList.size();

    }

    public Organization getItem(int position) {

        return sourceList.get(position);

    }

    private void updateGroupSelection(final Organization organization) {

        int selected = groupPrefs.getInt(organization.properties.name, 0);

        if (selected > 0) {

            groupPrefs.edit().putInt(organization.properties.name, 0).apply();

        } else {

            groupPrefs.edit().putInt(organization.properties.name, organization.properties.id).apply();

        }

        notifyDataSetChanged();

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Organization feature = sourceList.get(position);

        name = feature.properties.name;

        id = feature.id;

        Log.d("orgid", String.valueOf(id) + name);

        // Check if an existing view is being reused, otherwise inflate the view

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.organization_checklist_item, parent, false);

        }

        // Layout elements

        associateGroup = (SwitchCompat) convertView.findViewById(R.id.associate_group);

        associateGroup.setTag(String.format("%s__%s", name, id));

        // Set click listener

        associateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("Button Event", "Clicked the associate group button.");

                Button button = (Button) v;

                int selected = groupPrefs.getInt(feature.properties.name, 0);

                if (selected > 0) {

                    groupPrefs.edit().putInt(feature.properties.name, 0).apply();

                    button.setBackgroundResource(R.drawable.green_button);

                    button.setText(R.string.add_group);

                } else {

                    groupPrefs.edit().putInt(feature.properties.name, feature.properties.id).apply();

                    button.setBackgroundResource(R.drawable.orange_button);

                    button.setText(R.string.remove_group);

                }

            }
        });

        // Lookup view for data population
        TextView siteName = (TextView) convertView.findViewById(R.id.organization_name);

        siteName.setText(name);

        return convertView;

    }

}
