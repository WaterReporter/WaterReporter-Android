package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.interfaces.data.territory.TerritoryGeometryCallbacks;
import com.viableindustries.waterreporter.api.interfaces.data.territory.TerritoryHelpers;
import com.viableindustries.waterreporter.api.models.comment.Comment;
import com.viableindustries.waterreporter.api.models.comment.CommentCollection;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.query.QueryParams;
import com.viableindustries.waterreporter.api.models.query.QuerySort;
import com.viableindustries.waterreporter.api.models.territory.HucFeature;
import com.viableindustries.waterreporter.api.models.territory.Territory;
import com.viableindustries.waterreporter.user_interface.adapters.CommentAdapter;
import com.viableindustries.waterreporter.user_interface.adapters.TimelineAdapter;
import com.viableindustries.waterreporter.user_interface.adapters.TimelineItemViewHolder;
import com.viableindustries.waterreporter.user_interface.listeners.PostCommentListener;
import com.viableindustries.waterreporter.user_interface.listeners.PostFavoriteCountListener;
import com.viableindustries.waterreporter.utilities.AttributeTransformUtility;
import com.viableindustries.waterreporter.utilities.DeviceDimensionsHelper;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;

/**
 * Created by Ryan Hamley on 10/14/14.
 * This activity displays detailed information about a report after clicking on its map marker.
 */

public class PostDetailActivity extends AppCompatActivity {

    ListView commentList;

    MapView mapView;

    View mapMask;

    View listViewLock;

    LinearLayout listViewGroup;

    protected List<String> groups;

    private Context mContext;

    private SharedPreferences mSharedPreferences;

    private List<Comment> commentCollectionList = new ArrayList<>();

    private Report mPost;

    private boolean mDisplayMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here.
        Mapbox.getInstance(this, getString(R.string.mapBoxToken));

        setContentView(R.layout.activity_post_detail);

        commentList = (ListView) findViewById(R.id.commentList);

        listViewLock = findViewById(R.id.listViewLock);

        mapMask = findViewById(R.id.mapMask);

        listViewGroup = (LinearLayout) findViewById(R.id.listViewGroup);

        mContext = this;

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        mapView = (MapView) findViewById(R.id.mapView);

        mapView.onCreate(savedInstanceState);

        mDisplayMap = false;

        listViewLock.setVisibility(View.GONE);
        listViewLock.setClickable(false);
        listViewLock.setFocusable(false);

        // Inspect intent and handle app link data

        Intent appLinkIntent = getIntent();
        Uri appLinkData = appLinkIntent.getData();

        if (appLinkData != null) {

            try {

                int postId = Integer.parseInt(appLinkData.getLastPathSegment());

                Log.d("post--id", postId + "");

                if (postId > 0) fetchReport(postId);

            } catch (NumberFormatException e) {

                // Retrieve stored Post

                retrieveStoredPost();

            }

        } else {

            retrieveStoredPost();

        }

    }

    private void retrieveStoredPost() {

        mPost = ModelStorage.getStoredPost(mSharedPreferences);

        try {

            addListViewHeader(mPost);

        } catch (NullPointerException e) {

            e.printStackTrace();

            Log.v("NO-STORED-POST", e.getMessage());

//            Log.v("NO-STORED-POST", e.printStackTrace());

            Log.v("NO-STORED-POST", "Unable to render list header.");

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

        try {

            fetchGeometry(mPost);

        } catch (NullPointerException e) {

            Log.v("NO-STORED-POST", e.getMessage());

            Log.v("NO-STORED-POST", "Unable to load watershed geometry.");

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

        try {

            fetchComments(50, 1);

        } catch (NullPointerException e) {

            Log.v("NO-STORED-POST", e.getMessage());

            Log.v("NO-STORED-POST", "Unable to load post comments.");

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

    }

    // Fetch watershed geometry and metadata related to current post

    private void fetchGeometry(Report post) {

        if (post.properties.territory != null) {

            final Territory territory = post.properties.territory;

            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final MapboxMap mapboxMap) {

                    mapboxMap.getUiSettings().setAllGesturesEnabled(false);

                    TerritoryHelpers.fetchTerritoryGeometry(mContext, territory, new TerritoryGeometryCallbacks() {

                        @Override
                        public void onSuccess(@NonNull HucFeature hucFeature) {

                            LatLng southWest = new LatLng(hucFeature.properties.bounds.get(1), hucFeature.properties.bounds.get(0));
                            LatLng northEast = new LatLng(hucFeature.properties.bounds.get(3), hucFeature.properties.bounds.get(2));

                            List<LatLng> latLngs = new ArrayList<>();

                            latLngs.add(southWest);
                            latLngs.add(northEast);

                            // Move camera to watershed bounds
                            LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(latLngs).build();
                            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100, 100, 100, 100), 500);

                        }

                        @Override
                        public void onError(@NonNull RetrofitError error) {

                            Response errorResponse = error.getResponse();

                            // If we have a valid response object, check the status code and redirect to log in view if necessary

                            if (errorResponse != null) {

                                int status = errorResponse.getStatus();

                                if (status == 403) {

                                    mContext.startActivity(new Intent(mContext, SignInActivity.class));

                                }

                            }
                        }

                    });

                    String code = AttributeTransformUtility.getTerritoryCode(territory);
                    String url = String.format("https://huc.waterreporter.org/8/%s", code);

                    try {

                        URL geoJsonUrl = new URL(url);
                        GeoJsonSource geoJsonSource = new GeoJsonSource("geojson", geoJsonUrl);
                        mapboxMap.addSource(geoJsonSource);

                        // Create a FillLayer with style properties

                        FillLayer layer = new FillLayer("geojson", "geojson");

                        layer.withProperties(
                                fillColor("#9843c4"),
                                fillOpacity(0.4f)
                        );

                        mapboxMap.addLayer(layer);

                    } catch (Exception e) {

                        Log.d("Malformed URL", e.getMessage());

                    }

                }
            });

        }

    }

    private void setHeaderMargin(final LinearLayout linearLayout) {

        if (!mDisplayMap) {

            try {

                mapMask.setVisibility(View.GONE);
                mapMask.setEnabled(false);
                mapMask.setBackgroundColor(Color.TRANSPARENT);

            } catch (NullPointerException e) {

                finish();

            }

            float scale = getResources().getDisplayMetrics().density;
            final int pixels = (int) (240 * scale);

            listViewGroup.animate().y(pixels).setDuration(250).start();

            listViewLock.setVisibility(View.VISIBLE);
            listViewLock.setClickable(true);
            listViewLock.setFocusable(true);

        } else {

            try {

                mapMask.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mapMask.setVisibility(View.VISIBLE);
                        mapMask.setEnabled(true);
                        mapMask.setBackgroundColor(Color.WHITE);
                    }

                }, 250);

            } catch (NullPointerException e) {

                finish();

            }

            listViewGroup.animate().y(0).setDuration(250).start();

            listViewLock.setVisibility(View.GONE);
            listViewLock.setClickable(false);
            listViewLock.setFocusable(false);

        }

        mDisplayMap = !mDisplayMap;

    }

    private void addListViewHeader(Report post) {

        LayoutInflater inflater = getLayoutInflater();

        ViewGroup mListViewHeader = (ViewGroup) inflater.inflate(R.layout.post_detail_header, commentList, false);

        final LinearLayout postContainer = (LinearLayout) mListViewHeader.findViewById(R.id.postContainer);

        final TimelineItemViewHolder viewHolder;

        viewHolder = new TimelineItemViewHolder();

        viewHolder.postHeader = (LinearLayout) postContainer.findViewById(R.id.postHeader);
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

        // Field book

        viewHolder.fieldBookIndicator = (RelativeLayout) postContainer.findViewById(R.id.fieldBookIndicator);
        viewHolder.fieldBookIcon = (ImageView) postContainer.findViewById(R.id.fieldBookIcon);

        // Set dimensions of post image container

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DeviceDimensionsHelper.getDisplayWidth(mContext));

        viewHolder.postThumb.setLayoutParams(layoutParams);

        postContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                setHeaderMargin(listViewGroup);

            }
        });

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

        // Set click listeners on post header

        listViewLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                setHeaderMargin(listViewGroup);

            }
        });

        postContainer.setTag(viewHolder);

        TimelineAdapter.bindData(post, mContext, mSharedPreferences, getSupportFragmentManager(), viewHolder, false, true);

        // Display comment and favorite counts

        populateActionCounts(mListViewHeader, post);

        // Add populated header view to report timeline

        if (commentList == null) {

            commentList = (ListView) findViewById(R.id.commentList);

        }

        commentList.addHeaderView(mListViewHeader, null, false);

    }

    private void populateActionCounts(final ViewGroup viewGroup, final Report post) {

        RelativeLayout mCommentCount = (RelativeLayout) viewGroup.findViewById(R.id.commentCount);
        TextView mFullCommentCount = (TextView) viewGroup.findViewById(R.id.fullCommentCount);
        RelativeLayout mFavoriteCount = (RelativeLayout) viewGroup.findViewById(R.id.favoriteCount);
        TextView mFullFavoriteCount = (TextView) viewGroup.findViewById(R.id.fullFavoriteCount);

        // Set value of comment count string
        int commentCount = post.properties.comments.size();

        if (commentCount > 0) {

            mCommentCount.setVisibility(View.VISIBLE);

            mFullCommentCount.setText(mContext.getResources().getQuantityString(R.plurals.comment_label, commentCount, commentCount));

            mCommentCount.setOnClickListener(new PostCommentListener(mContext, post));

        }

        // Set value of favorite count string
        int favoriteCount = post.properties.favorites.size();

        if (favoriteCount > 0) {

            mFavoriteCount.setVisibility(View.VISIBLE);

            mFullFavoriteCount.setText(mContext.getResources().getQuantityString(R.plurals.favorite_label, favoriteCount, favoriteCount));

            mFullFavoriteCount.setOnClickListener(new PostFavoriteCountListener(mContext, post));

        }

    }

    private void fetchReport(int postId) {

        RestClient.getReportService().getSingleReport("application/json", postId, new Callback<Report>() {

            @Override
            public void success(Report report, Response response) {

                mPost = report;

                // Write Post to temporary storage in SharedPreferences

                ModelStorage.storeModel(mSharedPreferences, mPost, "stored_post");

                addListViewHeader(mPost);

                fetchGeometry(mPost);

                fetchComments(50, 1);

            }

            @Override
            public void failure(RetrofitError error) {

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    startActivity(new Intent(mContext, MainActivity.class));

                }

            }

        });

    }

    private void fetchComments(int limit, int page) {

        final String accessToken = mSharedPreferences.getString("access_token", "");

        Log.d("", accessToken);

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<>();

        QuerySort querySort = new QuerySort("created", "desc");

        queryOrder.add(querySort);

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(null, queryOrder);

        String query = new Gson().toJson(queryParams);

        RestClient.getReportService().getReportComments(accessToken, "application/json", mPost.id, page, limit, query, new Callback<CommentCollection>() {

            @Override
            public void success(CommentCollection commentCollection, Response response) {

                List<Comment> comments = commentCollection.getFeatures();

                Log.v("list", comments.toString());

                commentCollectionList = comments;

                populateComments(commentCollectionList);

            }

            @Override
            public void failure(RetrofitError error) {

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(mContext, SignInActivity.class));

                    }

                }

            }

        });

    }

    private void populateComments(List<Comment> comments) {

        CommentAdapter commentAdapter = new CommentAdapter(this, mSharedPreferences, commentList, comments);

        commentList.setAdapter(commentAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.marker, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onResume() {

        super.onResume();

        mapView.onResume();

        // Retrieve stored post

        if (mPost == null) {

            retrieveStoredPost();

        }

    }

    @Override
    public void onPause() {

        super.onPause();

        mapView.onPause();

    }

    @Override
    public void onStop() {

        super.onStop();

        mapView.onStop();

        // Clear model from temporary storage

        ModelStorage.removeModel(mSharedPreferences, "stored_post");

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        mapView.onDestroy();

        // Clear model from temporary storage

        ModelStorage.removeModel(mSharedPreferences, "stored_post");

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {

        // Clear model from temporary storage

        ModelStorage.removeModel(mSharedPreferences, "stored_post");

        finish();

    }

}