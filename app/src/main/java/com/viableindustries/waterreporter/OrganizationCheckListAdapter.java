package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserOrgPatch;
import com.viableindustries.waterreporter.data.UserService;
import com.vividsolutions.jts.awt.ShapeReader;

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Organization feature = sourceList.get(position);

        name = feature.properties.name;

        id = feature.id;

        Log.d("orgid", String.valueOf(id) + name);

        // Check if an existing view is being reused, otherwise inflate the view

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.organization_checklist_item, parent, false);
        }

        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.check_box);

        int selected = groupPrefs.getInt(name, 0);

        Log.d("orgid_s", String.valueOf(selected) + name);

        if (selected > 0) {

            checkBox.setChecked(true);

        }

        // Lookup view for data population
        TextView siteName = (TextView) convertView.findViewById(R.id.organization_name);

        convertView.setTag(id);

        siteName.setText(name);

        return convertView;

    }

}
