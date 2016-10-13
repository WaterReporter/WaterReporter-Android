package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.OrganizationService;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class GroupActionListActivity extends AppCompatActivity {

    @Bind(R.id.search_box)
    EditText listFilter;

    @Bind(R.id.list)
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_action_list);

        ButterKnife.bind(this);

        assert getSupportActionBar() != null;
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        connectionStatus();


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

    // This needs to be in its own utility service

    private void connectionStatus() {

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            buildList();

        } else {

            CharSequence text = "Looks like you're not connected to the internet, so we couldn't retrieve your site collection.";

            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(getBaseContext(), text, duration);

            toast.show();

        }

    }

    private void buildList() {

        SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        OrganizationService service = OrganizationService.restAdapter.create(OrganizationService.class);

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<QuerySort>();

        QuerySort querySort = new QuerySort("name", "asc");

        queryOrder.add(querySort);

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(null, queryOrder);

        String query = new Gson().toJson(queryParams);

        Log.d("URL", query);

        service.getOrganizations(access_token, "application/json", 100, query, new Callback<OrganizationFeatureCollection>() {

            @Override
            public void success(OrganizationFeatureCollection organizationCollectionResponse, Response response) {

                ArrayList<Organization> features = organizationCollectionResponse.getFeatures();

                Log.v("list", features.toString());

                if (!features.isEmpty()) {

//                    progressBar.setVisibility(View.GONE);

                    if (response != null) {

                        populateOrganizations(features);

                    }

                }

            }

            @Override
            public void failure(RetrofitError error) {

                Response response = error.getResponse();

                int status = response.getStatus();

                error.printStackTrace();

            }

        });

    }

    private void populateOrganizations(ArrayList<Organization> orgs) {

        final GroupActionListAdapter adapter = new GroupActionListAdapter(this, orgs, true);

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

        listView.setAdapter(adapter);

        // Enable ListView filtering

        listView.setTextFilterEnabled(true);

    }

    public void toFeed(View view) {

//        int id = item.getItemIdemId();
//
//        if (id == R.id.skip) {

        startActivity(new Intent(this, MainActivity.class));

//        }
//
//        return true;

    }

}
