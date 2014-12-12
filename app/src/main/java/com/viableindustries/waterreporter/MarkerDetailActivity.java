package com.viableindustries.waterreporter;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.CommonsCloudService;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.SingleFeatureResponse;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Ryan Hamley on 10/14/14.
 * This activity displays detailed information about a report after clicking on its map marker.
 */
public class MarkerDetailActivity extends ActionBarActivity {
    @InjectView(R.id.marker_type) TextView tvType;
    @InjectView(R.id.marker_activity) TextView tvActivity;
    @InjectView(R.id.marker_date) TextView tvDate;
    @InjectView(R.id.marker_caption) TextView tvCaption;
    @InjectView(R.id.marker_image) ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);

        ButterKnife.inject(this);

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
        ButterKnife.reset(this);
    }

    protected void requestData(int id){
        RestAdapter restAdapter = CommonsCloudService.restAdapter;

        CommonsCloudService service = restAdapter.create(CommonsCloudService.class);

        service.getSingleReport(id, new Callback<SingleFeatureResponse>() {
            @Override
            public void success(SingleFeatureResponse singleFeatureResponse, Response response) {
                Report report = singleFeatureResponse.response;

                if(report.photo.size() != 0){
                    String filePath = report.photo.get(0).filepath;
                    Picasso.with(getBaseContext()).load(filePath).into(iv);
                }

                if(report.isPollution){
                    tvActivity.setText(report.pollution.get(0).name);
                } else {
                    tvActivity.setText(report.activity.get(0).name);
                }

                tvType.setText(report.type.get(0).name);
                tvDate.setText("Submitted on " + report.getFormattedDateString());
                tvCaption.setText(report.comments);
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }
}