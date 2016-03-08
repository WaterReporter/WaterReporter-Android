package com.viableindustries.waterreporter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.GroupNameComparator;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserOrgPatch;
import com.viableindustries.waterreporter.data.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    @Bind(R.id.report_detail)
    LinearLayout reportDetail;

    @Bind(R.id.group_affiliation)
    TextView groupAffiliation;

    @Bind(R.id.loading_spinner)
    ProgressBar progressBar;

    Report report;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_marker_detail);

        ButterKnife.bind(this);

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

    protected void requestData(int id) {

        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(MarkerDetailActivity.this, R.color.base_blue),
                android.graphics.PorterDuff.Mode.SRC_IN);

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        RestAdapter restAdapter = ReportService.restAdapter;

        ReportService service = restAdapter.create(ReportService.class);

        service.getSingleReport(access_token, "application/json", id, new Callback<Report>() {

            @Override
            public void success(Report reportResponse, Response response) {

                progressBar.setVisibility(View.GONE);

                report = reportResponse;

                if (report.properties.images.size() != 0) {

                    String filePath = report.properties.images.get(0).properties.square_retina;

                    Picasso.with(getBaseContext())
                            .load(filePath)
                            .placeholder(R.drawable.square_placeholder)
                            .into(iv);

                }

                if (report.properties.groups.size() != 0) {

                    ArrayList<Organization> organizations = report.properties.groups;

                    Collections.sort(organizations, new GroupNameComparator());

                    populateOrganizations(organizations);

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

        groupAffiliation.setVisibility(View.VISIBLE);

        final OrganizationListAdapter adapter = new OrganizationListAdapter(this, orgs, true);

        final int adapterCount = adapter.getCount();

        for (int i = 0; i < adapterCount; i++) {

            View item = adapter.getView(i, null, null);

            reportDetail.addView(item);

        }

    }

}