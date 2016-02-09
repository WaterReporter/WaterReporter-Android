package com.viableindustries.waterreporter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserOrgPatch;
import com.viableindustries.waterreporter.data.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Ryan Hamley on 10/14/14.
 * This activity displays detailed information about a report after clicking on its map marker.
 */
public class MarkerDetailActivity extends AppCompatActivity {

    @Bind(R.id.marker_date)
    TextView tvDate;

    @Bind(R.id.marker_caption)
    TextView tvCaption;

    @Bind(R.id.marker_watershed)
    TextView tvWatershed;

    @Bind(R.id.marker_image)
    ImageView iv;

    @Bind(R.id.groups)
    LinearLayout groupList;

    @Bind(R.id.list)
    ListView listView;

//    @Bind(R.id.organization_name)
//    TextView tvOrgName;
//
//    @Bind(R.id.join_group)
//    Button joinButton;

    Report report;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_marker_detail);

        ButterKnife.bind(this);

//        joinButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                joinGroup();
//            }
//        });

        int reportId = getIntent().getExtras().getInt("REPORT_ID");

        requestData(reportId);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.marker, menu);

        return true;

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        Picasso.with(this).cancelRequest(iv);
        ButterKnife.unbind(this);
    }

    private void joinGroup() {

        SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        // Retrieve API token

        final String access_token = prefs.getString("access_token", "");

        // Retrieve user ID

        int id = prefs.getInt("user_id", 0);

        // Build request object

        Map<String, Map> userPatch = UserOrgPatch.buildRequest(report.properties.groups.get(0).id, false);

        UserService service = UserService.restAdapter.create(UserService.class);

        service.updateUserOrganization(access_token, "application/json", id, userPatch, new Callback<User>() {

            @Override
            public void success(User user, Response response) {

                CharSequence text = String.format("Successfully joined %s", report.properties.groups.get(0).properties.name);
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(getBaseContext(), text, duration);
                toast.show();

            }

            @Override
            public void failure(RetrofitError error) {

                Response response = error.getResponse();

                int status = response.getStatus();

                error.printStackTrace();

                CharSequence text = "Sorry, something went wrong and we couldn\'t complete your request.";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(getBaseContext(), text, duration);
                toast.show();

            }

        });

    }

    protected void requestData(int id) {

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        RestAdapter restAdapter = ReportService.restAdapter;

        ReportService service = restAdapter.create(ReportService.class);

        service.getSingleReport(access_token, "application/json", id, new Callback<Report>() {

            @Override
            public void success(Report reportResponse, Response response) {

                report = reportResponse;

                if (report.properties.images.size() != 0) {

                    String filePath = report.properties.images.get(0).properties.square_retina;

                    Picasso.with(getBaseContext())
                            .load(filePath)
                            .placeholder(R.drawable.square_placeholder)
                            .into(iv);

                }

                if (report.properties.groups.size() != 0) {

                    populateOrganizations(report.properties.groups);

//                    String userGroups = prefs.getString("user_groups", "");
//
//                    if (userGroups.length() > 0) {
//
//                        String[] orgIds = userGroups.split(",");
//
//                        for (Organization organization : report.properties.groups) {
//
//                            if (Arrays.asList(orgIds).contains(String.valueOf(organization.id))) {
//
//                                joinButton.setVisibility(View.GONE);
//
//                            }
//
//                        }
//
//                    }
//
//                    groupList.setVisibility(View.VISIBLE);
//
//                    tvOrgName.setText(report.properties.groups.get(0).properties.name);

                }

                tvDate.setText(String.format("%s %s \u00B7 %s",
                        report.properties.owner.properties.first_name,
                        report.properties.owner.properties.last_name,
                        report.properties.getFormattedDateString()));

                tvCaption.setText(report.properties.report_description);

                tvWatershed.setVisibility(View.VISIBLE);

                try {

                    tvWatershed.setText(String.format("%s Watershed", report.properties.territory.properties.huc_6_name));

                } catch (NullPointerException ne) {

                    tvWatershed.setText(String.format("%s Watershed", "none found"));

                }

            }

            @Override
            public void failure(RetrofitError error) {
            }

        });

    }

    private void populateOrganizations(ArrayList<Organization> orgs) {

        groupList.setVisibility(View.VISIBLE);

        final OrganizationListAdapter adapter = new OrganizationListAdapter(this, orgs);

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

    }

}