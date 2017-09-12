package com.viableindustries.waterreporter;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.user.UserGroupList;
import com.viableindustries.waterreporter.user_interface.adapters.OrganizationListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by brendanmcintyre on 11/15/16.
 */

public class ManageGroupsActivity extends AppCompatActivity {

    @Bind(R.id.search_box)
    EditText listFilter;

    @Bind(R.id.list)
    ListView listView;

    private boolean generic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_groups);

        ButterKnife.bind(this);

        ArrayList<Organization> organizations = UserGroupList.getList();

        Collections.sort(organizations, new Comparator<Organization>() {
            @Override
            public int compare(Organization organization1, Organization organization2) {
                return organization1.properties.name.compareTo(organization2.properties.name);
            }
        });

        populateOrganizations(organizations);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.organization_list, menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateOrganizations(ArrayList<Organization> orgs) {

        final OrganizationListAdapter adapter = new OrganizationListAdapter(this, orgs);

        listView.setAdapter(adapter);

        listFilter.setVisibility(View.VISIBLE);

        listFilter.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Log.d("filter", s.toString());

                adapter.getFilter().filter(s.toString());

            }

        });

        // Enable ListView filtering

        listView.setTextFilterEnabled(true);

    }

}
