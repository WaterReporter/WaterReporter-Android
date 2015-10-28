package com.viableindustries.waterreporter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.Report;

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

    @Bind(R.id.marker_date) TextView tvDate;

    @Bind(R.id.marker_caption) TextView tvCaption;

    @Bind(R.id.marker_watershed) TextView tvWatershed;

    @Bind(R.id.marker_image) ImageView iv;

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

    protected void requestData(int id){

        SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        RestAdapter restAdapter = ReportService.restAdapter;

        ReportService service = restAdapter.create(ReportService.class);

        service.getSingleReport(access_token, "application/json", id, new Callback<Report>() {

            @Override
            public void success(Report report, Response response) {

                if(report.properties.images.size() != 0){

                    String filePath = report.properties.images.get(0).properties.square_retina;

                    Picasso.with(getBaseContext())
                            .load(filePath)
                            .placeholder(R.drawable.square_placeholder)
                            .into(iv);

                }

                tvDate.setText(String.format("%s %s \u00B7 %s",
                                report.properties.owner.properties.first_name,
                                report.properties.owner.properties.last_name,
                                report.properties.getFormattedDateString()));

                tvCaption.setText(report.properties.report_description);

                tvWatershed.setVisibility(View.VISIBLE);

                tvWatershed.setText(String.format("%s Watershed", report.properties.territory.properties.huc_6_name));

            }

            @Override
            public void failure(RetrofitError error) {
            }

        });

    }

}