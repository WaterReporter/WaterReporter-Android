package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.FeatureCollection;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.GroupNameComparator;
import com.viableindustries.waterreporter.data.HtmlCompat;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationProfileListener;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportPhoto;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.TerritoryProfileListener;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserOrgPatch;
import com.viableindustries.waterreporter.data.UserProfileListener;
import com.viableindustries.waterreporter.data.UserService;
import com.viableindustries.waterreporter.dialogs.ReportActionDialogListener;
import com.viableindustries.waterreporter.dialogs.ShareActionDialogListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

    @Bind(R.id.masthead)
    RelativeLayout masthead;

    @Bind(R.id.post_container)
    ScrollView postContainer;

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
    FlexboxLayout reportGroups;

    @Bind(R.id.owner_avatar)
    ImageView ownerAvatar;

    @Bind(R.id.report_thumb)
    ImageView reportThumb;

    @Bind(R.id.action_badge)
    RelativeLayout actionBadge;

    @Bind(R.id.comment_icon)
    RelativeLayout commentIcon;

    @Bind(R.id.share_icon)
    RelativeLayout shareIcon;

    @Bind(R.id.text_descriptors)
    LinearLayout textDescriptors;

    @Bind(R.id.report_stub)
    LinearLayout reportStub;

    private String creationDate;

    private Integer featureId;

    private String imagePath;

    private String watershedName;

    protected List<String> groups;

    private String groupList;

    private int commentCount;

    private Context context;

    private int socialOptions;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_marker_detail);

        ButterKnife.bind(this);

        context = this;

        // Set dimensions of post image

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DeviceDimensionsHelper.getDisplayWidth(context));

        reportThumb.setLayoutParams(layoutParams);

        // Retrieve report and attempt to display data

        Report report = ReportHolder.getReport();

        try {

            populateView(report);

        } catch (NullPointerException npe) {

            Intent intent = getIntent();
            String action = intent.getAction();
            Uri data = intent.getData();

            Log.d("intentData", data.getLastPathSegment());

            try {

                int id = Integer.parseInt(data.getLastPathSegment());

                fetchReport(id);

            } catch (NumberFormatException nfe) {

                startActivity(new Intent(context, MainActivity.class));

            }

        }

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

    }

    private void populateView(final Report report) {

        featureId = (Integer) report.id;

        masthead.setVisibility(View.VISIBLE);

        postContainer.setVisibility(View.VISIBLE);

        ReportPhoto image = (ReportPhoto) report.properties.images.get(0);

        imagePath = (String) image.properties.square_retina;

        // Display user name
        reportOwner.setText(String.format("%s %s", report.properties.owner.properties.first_name, report.properties.owner.properties.last_name));

        creationDate = (String) AttributeTransformUtility.relativeTime(report.properties.created);
        reportDate.setText(creationDate);

        // Display watershed name, if any

        watershedName = AttributeTransformUtility.parseWatershedName(report.properties.territory);
        reportWatershed.setText(watershedName);

        reportWatershed.setOnClickListener(new TerritoryProfileListener(this, report.properties.territory));

        textDescriptors.setBackgroundColor(ContextCompat.getColor(this, R.color.white));

        // Extract group names, if any

        groupList = AttributeTransformUtility.groupListSize(report.properties.groups);

        // Display report text body, if any

        if (report.properties.description != null && (report.properties.description.length() > 0)) {

            reportCaption.setVisibility(View.VISIBLE);

            reportCaption.setText(report.properties.description.trim());

            new PatternEditableBuilder().
                    addPattern(context, Pattern.compile("\\#(\\w+)"), ContextCompat.getColor(context, R.color.waterreporter_blue),
                            new PatternEditableBuilder.SpannableClickedListener() {
                                @Override
                                public void onSpanClicked(String text) {

                                    Intent intent = new Intent(context, TagProfileActivity.class);
                                    intent.putExtra("tag", text);
                                    startActivity(intent);

                                }
                            }).into(reportCaption);

        } else {

            reportCaption.setVisibility(View.GONE);

        }

        // Attach click listeners to active UI components

        commentIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ReportHolder.setReport(report);

                Intent intent = new Intent(MarkerDetailActivity.this, CommentActivity.class);

                startActivity(intent);

            }
        });

        actionBadge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ReportHolder.setReport(report);

                Intent intent = new Intent(MarkerDetailActivity.this, CommentActivity.class);

                startActivity(intent);

            }
        });

        reportGroups.setVisibility(View.VISIBLE);

        reportGroups.removeAllViews();

        if (report.properties.groups.size() > 0) {

            for (Organization organization : report.properties.groups) {

                // Inflate organization logo layout and add to FlexboxLayout

                ImageView groupView = (ImageView) LayoutInflater.from(this).inflate(R.layout.related_group_item, reportGroups, false);

                Picasso.with(this).load(organization.properties.picture).placeholder(R.drawable.user_avatar_placeholder_003).transform(new CircleTransform()).into(groupView);

                groupView.setTag(organization);

                groupView.setOnClickListener(new OrganizationProfileListener(this, organization));

                reportGroups.addView(groupView);

            }

        } else {

            reportGroups.setVisibility(View.GONE);

        }

        // Report owner

        ownerAvatar.setOnClickListener(new UserProfileListener(this, report.properties.owner));

        reportOwner.setOnClickListener(new UserProfileListener(this, report.properties.owner));

        // Display badge if report is closed

        if ("closed".equals(report.properties.state)) {

            actionBadge.setVisibility(View.VISIBLE);

        } else {

            actionBadge.setVisibility(View.GONE);

        }

        // Set value of comment count string

        commentCount = report.properties.comments.size();
        reportComments.setText(getResources().getQuantityString(R.plurals.comment_label, commentCount, commentCount));

        shareIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Resources res = context.getResources();

                String shareUrl = res.getString(R.string.share_post_url, report.id);

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
                sendIntent.setType("text/plain");
                context.startActivity(Intent.createChooser(sendIntent, res.getText(R.string.share_report_chooser_title)));

            }
        });

        // Load images assets into their targets

        Picasso.with(this).load(report.properties.owner.properties.picture).placeholder(R.drawable.user_avatar_placeholder_003).transform(new CircleTransform()).into(ownerAvatar);

        Picasso.with(this).load(imagePath).placeholder(R.drawable.reverse_letter_mark).fit().centerCrop().into(reportThumb);

    }

    private void fetchReport(int postId) {

        RestAdapter restAdapter = ReportService.restAdapter;

        ReportService service = restAdapter.create(ReportService.class);

        service.getSingleReport("", "application/json", postId, new Callback<Report>() {

            @Override
            public void success(Report report, Response response) {

                populateView(report);

            }

            @Override
            public void failure(RetrofitError error) {

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    startActivity(new Intent(context, MainActivity.class));

                }

            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.marker, menu);

        return true;

    }

    @Override
    public void onResume() {

        super.onResume();

        prefs.edit().putBoolean("markerDetailOpen", false).apply();

    }

    @Override
    public void onPause() {

        super.onPause();

        prefs.edit().putBoolean("markerDetailOpen", false).apply();

    }

    @Override
    public void onStop() {

        super.onStop();

        prefs.edit().putBoolean("markerDetailOpen", false).apply();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        Picasso.with(this).cancelRequest(reportThumb);

        Picasso.with(this).cancelRequest(ownerAvatar);

        ButterKnife.unbind(this);

        prefs.edit().putBoolean("markerDetailOpen", false).apply();

    }

}