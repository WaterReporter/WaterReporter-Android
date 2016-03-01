package com.viableindustries.waterreporter;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.GeometryResponse;
import com.viableindustries.waterreporter.data.GroupNameComparator;
import com.viableindustries.waterreporter.data.ImageProperties;
import com.viableindustries.waterreporter.data.ImageService;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportPostBody;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.UserService;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class GroupChecklistActivity extends AppCompatActivity {

    @Bind(R.id.loading_spinner)
    ProgressBar progressBar;

    @Bind(R.id.list)
    ListView listView;

    SharedPreferences groupPrefs;

    protected Map<String, Integer> groupMap = new HashMap<>();

    // Check for a data connection!

    protected boolean connectionActive() {

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_checklist);

        groupPrefs = getSharedPreferences(getString(R.string.associated_group_key), MODE_PRIVATE);

        ButterKnife.bind(this);

        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.base_blue),
                android.graphics.PorterDuff.Mode.SRC_IN);

        if (!connectionActive()) {

            CharSequence text = "Looks like you're not connected to the internet, so we couldn't submit your report. Please connect to a network and try again.";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(getBaseContext(), text, duration);
            toast.show();

            startActivity(new Intent(this, MainActivity.class));

            return;

        }

        // Retrieve the user's group collection

        fetchUserGroups();

    }

    // Pass groups to report activity

    protected void setGroups(boolean resultCode) {

        int code = resultCode ? RESULT_OK : RESULT_CANCELED;

        Intent intent = new Intent();

        setResult(code, intent);

        finish();

    }

    protected void fetchUserGroups() {

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        Log.d("", access_token);

        // We shouldn't need to retrieve this value again, but we'll deal with that issue later
        int user_id = prefs.getInt("user_id", 0);

        UserService service = UserService.restAdapter.create(UserService.class);

        service.getUserOrganization(access_token, "application/json", user_id, new Callback<OrganizationFeatureCollection>() {

            @Override
            public void success(OrganizationFeatureCollection organizationCollectionResponse, Response response) {

                progressBar.setVisibility(View.GONE);

                ArrayList<Organization> organizations = organizationCollectionResponse.getFeatures();

                if (organizations.size() > 0) {

                    Collections.sort(organizations, new GroupNameComparator());

                    populateOrganizations(organizations);

                }

            }

            @Override
            public void failure(RetrofitError error) {

                if (error == null) return;

                Response errorResponse = error.getResponse();

                progressBar.setVisibility(View.GONE);

                // If we have a valid response object, check the status code and redirect to log in view if necessary

//                if (errorResponse != null) {
//
//                    int status = errorResponse.getStatus();
//
//                    if (status == 403) {
//
//                        startActivity(new Intent(MainActivity.this, SignInActivity.class));
//
//                    }
//
//                }

            }

        });

    }

//    private View.OnClickListener handleCheckListClick(final View view) {
//
//        return new View.OnClickListener() {
//
//            public void onClick(View v) {
//
//                CheckBox checkBox = (CheckBox) view.findViewById(R.id.check_box);
//
//                TextView siteName = (TextView) view.findViewById(R.id.organization_name);
//
//                String groupName = (String) siteName.getText();
//
//                if (checkBox.isChecked()) {
//
//                    //groupMap.put(groupName, 0);
//                    prefs.edit().putInt(groupName, 0).apply();
//
//                } else {
//
//                    int groupId = (Integer) view.getTag();
//
//                    //groupMap.put(groupName, groupId);
//                    prefs.edit().putInt(groupName, groupId).apply();
//
//                }
//
//                checkBox.toggle();
//
//            }
//        };
//    }

    private void populateOrganizations(ArrayList<Organization> orgs) {

        final OrganizationCheckListAdapter adapter = new OrganizationCheckListAdapter(this, orgs);

        listView.setVisibility(View.VISIBLE);

//        listFilter.addTextChangedListener(new TextWatcher() {
//
//            public void afterTextChanged(Editable s) {
//            }
//
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                Log.d("filter", s.toString());
//
//                adapter.getFilter().filter(s.toString());
//
//            }
//
//        });

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                CheckBox checkBox = (CheckBox) view.findViewById(R.id.check_box);

                TextView siteName = (TextView) view.findViewById(R.id.organization_name);

                String groupName = (String) siteName.getText();

                if (checkBox.isChecked()) {

                    groupPrefs.edit().putInt(groupName, 0).apply();

                } else {

                    int groupId = (Integer) view.getTag();

                    groupPrefs.edit().putInt(groupName, groupId).apply();

                }

                checkBox.toggle();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.group_checklist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            case android.R.id.home:

                setGroups(false);

                return true;

            case R.id.action_set_groups:

                setGroups(true);

                return true;

        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onResume() {

        super.onResume();

        // Check for a data connection!

        if (!connectionActive()) {

            CharSequence text = "Looks like you're not connected to the internet, so we couldn't retrieve your groups. Please connect to a network and try again.";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(getBaseContext(), text, duration);
            toast.show();

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

    }

    @Override
    protected void onPause() {

        super.onPause();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

    }

}
