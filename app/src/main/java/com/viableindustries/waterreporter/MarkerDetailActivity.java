package com.viableindustries.waterreporter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.GroupNameComparator;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationProfileListener;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportPhoto;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserOrgPatch;
import com.viableindustries.waterreporter.data.UserService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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

    @Bind(R.id.report_date)
    TextView reportDate;

    @Bind(R.id.report_owner)
    TextView reportOwner;

    @Bind(R.id.report_watershed)
    TextView reportWatershed;

    @Bind(R.id.comment_count)
    TextView reportComments;

    @Bind(R.id.report_caption)
    TextView reportCaption;

    @Bind(R.id.report_groups)
    LinearLayout reportGroups;

    @Bind(R.id.owner_avatar)
    ImageView ownerAvatar;

    @Bind(R.id.report_thumb)
    ImageView reportThumb;

    @Bind(R.id.action_badge)
    RelativeLayout actionBadge;

    @Bind(R.id.report_stub)
    LinearLayout reportStub;

    @Bind(R.id.location_icon)
    RelativeLayout locationIcon;

    private String creationDate;

    private Integer featureId;

    private String imagePath;

    private String watershedName;

    protected List<String> groups;

    private String groupList;

    private String commentCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_marker_detail);

        ButterKnife.bind(this);

        Report report = ReportHolder.getReport();

        populateView(report);

    }

    private void populateOrganizations(ArrayList<Organization> orgs) {}

    private void populateView(final Report report) {

        ReportPhoto image = (ReportPhoto) report.properties.images.get(0);

        imagePath = (String) image.properties.square_retina;

        creationDate = (String) AttributeTransformUtility.relativeTime(report.properties.created);

        featureId = (Integer) report.id;

        // Extract watershed name, if any

        watershedName = AttributeTransformUtility.parseWatershedName(report.properties.territory);

        // Extract group names, if any
        groupList = AttributeTransformUtility.groupListSize(report.properties.groups);

        // Attach click listeners to active UI components

        locationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

        // Populate the data into the template view using the data object
        reportDate.setText(creationDate);
        reportOwner.setText(String.format("%s %s", report.properties.owner.properties.first_name, report.properties.owner.properties.last_name));
        reportWatershed.setText(watershedName);
        reportCaption.setText(report.properties.report_description.trim());

        // Add clickable organization views, if any

        reportGroups.setVisibility(View.VISIBLE);

        reportGroups.removeAllViews();

        if (report.properties.groups.size() > 0) {

            for (Organization organization : report.properties.groups) {

                TextView groupName = (TextView) LayoutInflater.from(this).inflate(R.layout.related_group_item, reportGroups, false);

                groupName.setText(organization.properties.name);

                groupName.setTag(organization);

                groupName.setOnClickListener(new OrganizationProfileListener(this, organization));

                reportGroups.addView(groupName);

            }

        } else {

            reportGroups.setVisibility(View.GONE);

        }

        // Display badge if report is closed
        if ("closed".equals(report.properties.state)) {

            actionBadge.setVisibility(View.VISIBLE);

        } else {

            actionBadge.setVisibility(View.GONE);

        }

        // Set value of comment count string
        commentCount = AttributeTransformUtility.countComments(report.properties.comments);

        reportComments.setText(commentCount);

        Log.v("url", imagePath);

        Picasso.with(this).load(report.properties.owner.properties.picture).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(ownerAvatar);

        Picasso.with(this).load(imagePath).fit().centerCrop().into(reportThumb);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.marker, menu);

        return true;

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        Picasso.with(this).cancelRequest(reportThumb);

        Picasso.with(this).cancelRequest(ownerAvatar);

        ButterKnife.unbind(this);

    }

}