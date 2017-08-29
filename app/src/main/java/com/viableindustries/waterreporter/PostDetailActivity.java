package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.HUCFeature;
import com.viableindustries.waterreporter.data.PostCommentListener;
import com.viableindustries.waterreporter.data.PostFavoriteCountListener;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportService;

import java.util.List;

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
public class PostDetailActivity extends AppCompatActivity {

    @Bind(R.id.postContainer)
    ScrollView postContainer;

    @Bind(R.id.ownerAvatar)
    ImageView ownerAvatar;

    @Bind(R.id.postThumb)
    ImageView postThumb;

    @Bind(R.id.customActionBar)
    LinearLayout customActionBar;

    @Bind(R.id.actionBarTitle)
    TextView actionBarTitle;

    @Bind(R.id.actionBarSubtitle)
    TextView actionBarSubtitle;

    @Bind(R.id.commentCount)
    RelativeLayout mCommentCount;

    @Bind(R.id.fullCommentCount)
    TextView mFullCommentCount;

    @Bind(R.id.favoriteCount)
    RelativeLayout mFavoriteCount;

    @Bind(R.id.fullFavoriteCount)
    TextView mFullFavoriteCount;

    protected List<String> groups;

    private Context context;

    private SharedPreferences sharedPreferences;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post_detail);

        ButterKnife.bind(this);

        context = this;

        if (Build.VERSION.SDK_INT >= 19) {

            AttributeTransformUtility.setStatusBarTranslucent(getWindow(), true);

        }

        // Retrieve report and attempt to display data

        Report report = ReportHolder.getReport();

        try {

            populateView(report);

        } catch (NullPointerException e) {

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

        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        fragmentManager = getSupportFragmentManager();

        // Fetch watershed geometry and metadata related to current post

        TerritoryHelpers.fetchTerritoryGeometry(context, report.properties.territory, new TerritoryGeometryCallbacks() {

            @Override
            public void onSuccess(@NonNull HUCFeature hucFeature) {

                actionBarTitle.setText(hucFeature.properties.name);

                actionBarSubtitle.setText(hucFeature.properties.states.concat);

                customActionBar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });

            }

            @Override
            public void onError(@NonNull RetrofitError error) {

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        context.startActivity(new Intent(context, SignInActivity.class));

                    }

                }
            }

        });

    }

    private void populateView(final Report post) {

        // Set value of comment count string
        int commentCount = post.properties.comments.size();

        if (commentCount > 0) {

            mCommentCount.setVisibility(View.VISIBLE);

            mFullCommentCount.setText(context.getResources().getQuantityString(R.plurals.comment_label, commentCount, commentCount));

            mCommentCount.setOnClickListener(new PostCommentListener(context, post));

        }

        // Set value of favorite count string
        int favoriteCount = post.properties.favorites.size();

        if (favoriteCount > 0) {

            mFavoriteCount.setVisibility(View.VISIBLE);

            mFullFavoriteCount.setText(context.getResources().getQuantityString(R.plurals.favorite_label, favoriteCount, favoriteCount));

            mFullFavoriteCount.setOnClickListener(new PostFavoriteCountListener(context, post));

        }

        final TimelineAdapter.ViewHolder viewHolder;

        viewHolder = new TimelineAdapter.ViewHolder();

        viewHolder.postDate = (TextView) postContainer.findViewById(R.id.postDate);
        viewHolder.postOwner = (TextView) postContainer.findViewById(R.id.postOwner);
        viewHolder.postWatershed = (TextView) postContainer.findViewById(R.id.postWatershed);
        viewHolder.postCaption = (TextView) postContainer.findViewById(R.id.postCaption);
        viewHolder.ownerAvatar = (ImageView) postContainer.findViewById(R.id.ownerAvatar);
        viewHolder.postGroups = (FlexboxLayout) postContainer.findViewById(R.id.postGroups);
        viewHolder.postThumb = (ImageView) postContainer.findViewById(R.id.postThumb);
        viewHolder.actionBadge = (RelativeLayout) postContainer.findViewById(R.id.actionBadge);
        viewHolder.postStub = (LinearLayout) postContainer.findViewById(R.id.postStub);
        viewHolder.locationIcon = (RelativeLayout) postContainer.findViewById(R.id.locationIcon);

        viewHolder.commentIcon = (FlexboxLayout) postContainer.findViewById(R.id.commentIcon);
        viewHolder.favoriteIcon = (FlexboxLayout) postContainer.findViewById(R.id.favoriteIcon);
        viewHolder.shareIcon = (RelativeLayout) postContainer.findViewById(R.id.shareIcon);
        viewHolder.actionsEllipsis = (RelativeLayout) postContainer.findViewById(R.id.actionEllipsis);
        viewHolder.locationIconView = (ImageView) postContainer.findViewById(R.id.locationIconView);
        viewHolder.extraActionsIconView = (ImageView) postContainer.findViewById(R.id.extraActionsIconView);

        viewHolder.shareIconView = (ImageView) postContainer.findViewById(R.id.shareIconView);
        viewHolder.commentIconView = (ImageView) postContainer.findViewById(R.id.commentIconView);
        viewHolder.favoriteIconView = (ImageView) postContainer.findViewById(R.id.favoriteIconView);

        // Set dimensions of post image container

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DeviceDimensionsHelper.getDisplayWidth(context));

        viewHolder.postThumb.setLayoutParams(layoutParams);

        // Action counts

        viewHolder.abbrFavoriteCount = (TextView) postContainer.findViewById(R.id.abbrFavoriteCount);

        viewHolder.abbrCommentCount = (TextView) postContainer.findViewById(R.id.abbrCommentCount);

        viewHolder.tracker = (TextView) postContainer.findViewById(R.id.tracker);

        // Open Graph

        viewHolder.openGraphData = (CardView) postContainer.findViewById(R.id.ogData);
        viewHolder.ogImage = (ImageView) postContainer.findViewById(R.id.ogImage);
        viewHolder.ogTitle = (TextView) postContainer.findViewById(R.id.ogTitle);
        viewHolder.ogDescription = (TextView) postContainer.findViewById(R.id.ogDescription);
        viewHolder.ogUrl = (TextView) postContainer.findViewById(R.id.ogUrl);

        postContainer.setTag(viewHolder);

        TimelineAdapter.bindData(post, context, sharedPreferences, fragmentManager, viewHolder, false);

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

        sharedPreferences.edit().putBoolean("markerDetailOpen", false).apply();

    }

    @Override
    public void onPause() {

        super.onPause();

        sharedPreferences.edit().putBoolean("markerDetailOpen", false).apply();

    }

    @Override
    public void onStop() {

        super.onStop();

        sharedPreferences.edit().putBoolean("markerDetailOpen", false).apply();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        Picasso.with(this).cancelRequest(postThumb);

        Picasso.with(this).cancelRequest(ownerAvatar);

        ButterKnife.unbind(this);

        sharedPreferences.edit().putBoolean("markerDetailOpen", false).apply();

    }

}