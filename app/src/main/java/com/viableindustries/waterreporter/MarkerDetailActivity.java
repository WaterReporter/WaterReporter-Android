//package com.viableindustries.waterreporter;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.ActionBarActivity;
//import android.support.v7.app.AppCompatActivity;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.util.Log;
//import android.view.Menu;
//import android.view.View;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//import android.widget.ProgressBar;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.squareup.picasso.Picasso;
//import com.viableindustries.waterreporter.data.Geometry;
//import com.viableindustries.waterreporter.data.GroupNameComparator;
//import com.viableindustries.waterreporter.data.Organization;
//import com.viableindustries.waterreporter.data.ReportPhoto;
//import com.viableindustries.waterreporter.data.ReportService;
//import com.viableindustries.waterreporter.data.Report;
//import com.viableindustries.waterreporter.data.User;
//import com.viableindustries.waterreporter.data.UserOrgPatch;
//import com.viableindustries.waterreporter.data.UserService;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//import retrofit.Callback;
//import retrofit.RestAdapter;
//import retrofit.RetrofitError;
//import retrofit.client.Response;
//
///**
// * Created by Ryan Hamley on 10/14/14.
// * This activity displays detailed information about a report after clicking on its map marker.
// */
//public class MarkerDetailActivity extends AppCompatActivity {
//
//    @Bind(R.id.report_date)
//    TextView reportDate;
//
//    @Bind(R.id.report_owner)
//    TextView reportOwner;
//
//    @Bind(R.id.report_watershed)
//    TextView reportWatershed;
//
//    @Bind(R.id.comment_count)
//    TextView reportComments;
//
//    @Bind(R.id.report_caption)
//    TextView reportCaption;
//
//    @Bind(R.id.report_groups)
//    TextView reportGroups;
//
//    @Bind(R.id.owner_avatar)
//    ImageView ownerAvatar;
//
//    @Bind(R.id.report_thumb)
//    ImageView reportThumb;
//
//    @Bind(R.id.action_badge)
//    RelativeLayout actionBadge;
//
//    @Bind(R.id.report_stub)
//    LinearLayout reportStub;
//
//    @Bind(R.id.location_icon)
//    RelativeLayout locationIcon;
//
//    private String creationDate;
//
//    private Integer featureId;
//
//    private String imagePath;
//
//    private String watershedName;
//
//    protected List<String> groups;
//
//    private String groupList;
//
//    private String commentCount;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_marker_detail);
//
//        ButterKnife.bind(this);
//
//        int reportId = getIntent().getExtras().getInt("REPORT_ID");
//
//        requestData(reportId);
//
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        getMenuInflater().inflate(R.menu.marker, menu);
//
//        return true;
//
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
////        mapView.onResume();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
////        mapView.onPause();
//    }
//
//    @Override
//    protected void onDestroy() {
//
//        super.onDestroy();
//
////        Picasso.with(this).cancelRequest(iv);
//
//        ButterKnife.unbind(this);
//
//    }
//
//    protected void requestData(int id) {
//
////        progressBar.getIndeterminateDrawable().setColorFilter(
////                ContextCompat.getColor(MarkerDetailActivity.this, R.color.base_blue),
////                android.graphics.PorterDuff.Mode.SRC_IN);
//
//        final SharedPreferences prefs =
//                getSharedPreferences(getPackageName(), MODE_PRIVATE);
//
//        final String accessToken = prefs.getString("access_token", "");
//
//        RestAdapter restAdapter = ReportService.restAdapter;
//
//        ReportService service = restAdapter.create(ReportService.class);
//
//        service.getSingleReport(accessToken, "application/json", id, new Callback<Report>() {
//
//            @Override
//            public void success(Report reportResponse, Response response) {
//
//                final Report report = reportResponse;
//
//                populateView(report);
//
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//            }
//
//        });
//
//    }
//
//    private void populateOrganizations(ArrayList<Organization> orgs) {
//
////        groupAffiliation.setVisibility(View.VISIBLE);
////
////        final OrganizationListAdapter adapter = new OrganizationListAdapter(this, orgs, true);
////
////        final int adapterCount = adapter.getCount();
////
////        for (int i = 0; i < adapterCount; i++) {
////
////            View item = adapter.getView(i, null, null);
////
////            reportDetail.addView(item);
////
////        }
//
//    }
//
//    private void populateView(final Report report) {
//
//        ReportPhoto image = (ReportPhoto) report.properties.images.get(0);
//
//        imagePath = (String) image.properties.square_retina;
//
//        creationDate = (String) report.properties.created;
//
//        featureId = (Integer) report.id;
//
//        // Extract watershed name, if any
//
//        try {
//
//            watershedName = String.format("%s Watershed", report.properties.territory.properties.huc_6_name);
//
//        } catch (NullPointerException ne) {
//
//            watershedName = "Watershed not available";
//
//        }
//
//        // Extract group names, if any
//
//        if (!report.properties.groups.isEmpty()) {
//
//            groupList = report.properties.groups.get(0).properties.name;
//
//        } else {
//
//            groupList = "This report is not affiliated with any groups.";
//
//        }
//
//        try {
//            //create SimpleDateFormat object with source string date format
//            SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
//
//            //parse the string into Date object
//            Date date = sdfSource.parse(creationDate);
//
//            //create SimpleDateFormat object with desired date format
//            SimpleDateFormat sdfOutput = new SimpleDateFormat("MMM dd, yyyy");
//
//            //parse the date into another format
//            creationDate = sdfOutput.format(date);
//
//        } catch (ParseException pe) {
//            System.out.println("Parse Exception : " + pe);
//        }
//
//        // Attach click listeners to active UI components
//
//        locationIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
////                Intent intent = new Intent(MarkerDetailActivity.this, MapDetailActivity.class);
////
////                Geometry geometry = report.geometry.geometries.get(0);
////
////                Log.d("geometry", geometry.toString());
////
////                intent.putExtra("REPORT_LATITUDE", geometry.coordinates.get(1));
////                intent.putExtra("REPORT_LONGITUDE", geometry.coordinates.get(0));
////
////                intent.putExtra("REPORT_ID", report.id);
////
////                startActivity(intent);
//
//            }
//        });
//
//        // Populate the data into the template view using the data object
//        reportDate.setText(creationDate);
//        reportOwner.setText(String.format("%s %s", report.properties.owner.properties.first_name, report.properties.owner.properties.last_name));
//        reportWatershed.setText(watershedName);
//        reportCaption.setText(report.properties.report_description.trim());
//        reportGroups.setText(groupList);
//
//        // Display badge if report is closed
//        if (report.properties.state.equals("closed")) {
//
//            actionBadge.setVisibility(View.VISIBLE);
//
//        } else {
//
//            actionBadge.setVisibility(View.GONE);
//
//        }
//
//        // Set value of comment count string
//        if (report.properties.comments.size() != 1) {
//
//            commentCount = String.format("%s comments", report.properties.comments.size());
//
//        } else {
//
//            commentCount = "1 comment";
//
//        }
//
//        reportComments.setText(commentCount);
//
//        Log.v("url", imagePath);
//
//        Picasso.with(this).load(report.properties.owner.properties.picture).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(ownerAvatar);
//
//        Picasso.with(this).load(imagePath).fit().centerCrop().into(reportThumb);
//
//    }
//
//}