package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.OrganizationService;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class OrganizationListActivity extends AppCompatActivity {

    @Bind(R.id.search_box)
    EditText listFilter;

    @Bind(R.id.list)
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_organization_list);

        ButterKnife.bind(this);

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

        int id = item.getItemId();

        if (id == R.id.skip) {

            startActivity(new Intent(this, MainActivity.class));

        }

        return true;

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

        service.getOrganizations(access_token, "application/json", 100, null, new Callback<OrganizationFeatureCollection>() {

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

    private void changeOrgStatus(final Organization organization, boolean selected) {

        SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        String op = "add";

        // Build wrapper for the operation object

        Map<String, List<Map>> opListWrapper = new HashMap<String, List<Map>>();

        List<Map> opList = new ArrayList<>();

        Map<String, Integer> opObj = new HashMap<String, Integer>();

        final String access_token = prefs.getString("access_token", "");

        UserService service = UserService.restAdapter.create(UserService.class);

        int id = prefs.getInt("user_id", 0);

        String token = prefs.getString("access_token", "");

        if (selected) {

            op = "remove";

        }

        opObj.put("id", organization.id);

        opList.add(opObj);

        opListWrapper.put(op, opList);

        Map<String, Map> userPatch = new HashMap<String, Map>();

        userPatch.put("organization", opListWrapper);

        service.updateUserOrganization(access_token, "application/json", id, userPatch, new Callback<User>() {

            @Override
            public void success(User user, Response response) {

                CharSequence text = String.format("Successfully joined %s", organization.properties.name);
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(getBaseContext(), text, duration);
                toast.show();

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

        final ArrayList<Organization> orgCollection = orgs;

        final OrganizationListAdapter adapter = new OrganizationListAdapter(this, orgCollection);

//        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////                intent = new Intent(ProfileActivity.this, SiteDetailActivity.class);
////                LinearLayout v = (LinearLayout) view.findViewById(R.id.site_card);
////                int featureId = Integer.parseInt(view.getTag().toString());
////                intent.putExtra("site_id", featureId);
////                startActivity(intent);
//                changeOrgStatus(orgCollection.get(position), false);
//            }
//        };

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

//        listView.setOnItemClickListener(listener);

    }

}
