package com.viableindustries.waterreporter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerViewManager;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.viableindustries.waterreporter.data.Comment;
import com.viableindustries.waterreporter.data.CommentCollection;
import com.viableindustries.waterreporter.data.HUCFeature;
import com.viableindustries.waterreporter.data.PostCommentListener;
import com.viableindustries.waterreporter.data.PostFavoriteCountListener;
import com.viableindustries.waterreporter.data.QueryParams;
import com.viableindustries.waterreporter.data.QuerySort;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.Territory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;

/**
 * Created by Ryan Hamley on 10/14/14.
 * This activity displays detailed information about a report after clicking on its map marker.
 */
public class PostDetailActivity extends AppCompatActivity {

    @Bind(R.id.commentList)
    ListView commentList;

    @Bind(R.id.mapview)
    MapView mapView;

//    @Nullable
//    @Bind(R.id.customActionBar)
//    LinearLayout customActionBar;
//
//    @Nullable
//    @Bind(R.id.actionBarTitle)
//    TextView actionBarTitle;
//
//    @Nullable
//    @Bind(R.id.actionBarSubtitle)
//    TextView actionBarSubtitle;

    RelativeLayout mCommentCount;

    TextView mFullCommentCount;

    RelativeLayout mFavoriteCount;

    TextView mFullFavoriteCount;

    protected List<String> groups;

    private Context mContext;

    private SharedPreferences sharedPreferences;

    protected CommentAdapter commentAdapter;

    protected List<Comment> commentCollectionList = new ArrayList<Comment>();

    protected Report mPost;

    protected ViewGroup mListViewHeader;

    private MapboxMap mMapboxMap;

    private GestureDetectorCompat mDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here.
        Mapbox.getInstance(this, getString(R.string.mapBoxToken));

        setContentView(R.layout.activity_post_detail);

        ButterKnife.bind(this);

        mContext = this;

        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        mapView.onCreate(savedInstanceState);

        mDetector = new GestureDetectorCompat(this, new PostHeaderGestureListener() {

            @Override
            public boolean onDown(MotionEvent event) {

                final LinearLayout postContainer = (LinearLayout) mListViewHeader.findViewById(R.id.postContainer);

                float scale = getResources().getDisplayMetrics().density;
                final int pixels = (int) (240 * scale);

                ValueAnimator animator = ValueAnimator.ofInt(postContainer.getPaddingTop(), pixels);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        postContainer.setPadding(0, (Integer) valueAnimator.getAnimatedValue(), 0, 0);
                    }
                });
                animator.setDuration(250);
                animator.start();

//                TranslateAnimation anim = new TranslateAnimation(0, 0 , 0, 240);
//                anim.setDuration(1000);
//                anim.setFillAfter(true);
//                commentList.startAnimation(anim);

                return true;

            }

        });

//        if (Build.VERSION.SDK_INT >= 19) {
//
//            AttributeTransformUtility.setStatusBarTranslucent(getWindow(), true);
//
//        }

        // Retrieve report and attempt to display data

        mPost = ReportHolder.getReport();

        try {

            addListViewHeader(mPost);

            fetchGeometry(mPost);

            fetchComments(50, 1);

        } catch (NullPointerException e) {

            Intent intent = getIntent();
            String action = intent.getAction();
            Uri data = intent.getData();

            Log.d("intentData", data.getLastPathSegment());

            try {

                int id = Integer.parseInt(data.getLastPathSegment());

                fetchReport(id);

            } catch (NumberFormatException nfe) {

                startActivity(new Intent(mContext, MainActivity.class));

            }

        }

    }

    // Fetch watershed geometry and metadata related to current post

    private void fetchGeometry(Report post) {

        if (post.properties.territory != null) {

            final Territory territory = post.properties.territory;

            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final MapboxMap mapboxMap) {

                    TerritoryHelpers.fetchTerritoryGeometry(mContext, territory, new TerritoryGeometryCallbacks() {

                        @Override
                        public void onSuccess(@NonNull HUCFeature hucFeature) {

                            LatLng southWest = new LatLng(hucFeature.properties.bounds.get(1), hucFeature.properties.bounds.get(0));
                            LatLng northEast = new LatLng(hucFeature.properties.bounds.get(3), hucFeature.properties.bounds.get(2));

                            List<LatLng> latLngs = new ArrayList<LatLng>();

                            latLngs.add(southWest);
                            latLngs.add(northEast);

                            // Move camera to watershed bounds
                            LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(latLngs).build();
                            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100, 100, 100, 100), 2000);

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

                    try

                    {

                        URL geoJsonUrl = new URL(url);
                        GeoJsonSource geoJsonSource = new GeoJsonSource("geojson", geoJsonUrl);
                        mapboxMap.addSource(geoJsonSource);

                        // Create a FillLayer with style properties

                        FillLayer layer = new FillLayer("geojson", "geojson");

                        layer.withProperties(
                                fillColor("#4355b8"),
                                fillOpacity(0.4f)
                        );

                        mapboxMap.addLayer(layer);

                    } catch (
                            MalformedURLException e)

                    {

                        Log.d("Malformed URL", e.getMessage());

                    }


                }
            });


        }

    }

    protected void setHeaderPadding(final LinearLayout linearLayout){

        int topPadding = linearLayout.getPaddingTop();
        int targetPadding = (topPadding == 0) ? 240 : 0;

        float scale = getResources().getDisplayMetrics().density;
        final int pixels = (int) (targetPadding * scale);

        ValueAnimator animator = ValueAnimator.ofInt(topPadding, pixels);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                linearLayout.setPadding(0, (Integer) valueAnimator.getAnimatedValue(), 0, 0);
            }
        });
        animator.setDuration(250);
        animator.start();

    }

    protected void addListViewHeader(Report post) {

        LayoutInflater inflater = getLayoutInflater();

        mListViewHeader = (ViewGroup) inflater.inflate(R.layout.post_detail_header, commentList, false);

        LinearLayout postContainer = (LinearLayout) mListViewHeader.findViewById(R.id.postContainer);

        final TimelineAdapter.ViewHolder viewHolder;

        viewHolder = new TimelineAdapter.ViewHolder();

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

        // Set dimensions of post image container

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DeviceDimensionsHelper.getDisplayWidth(mContext));

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

        // Set click listeners on post header

        postContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                setHeaderPadding((LinearLayout) view);

            }
        });

        postContainer.setTag(viewHolder);

        TimelineAdapter.bindData(post, mContext, sharedPreferences, getSupportFragmentManager(), viewHolder, false);

        // Display comment and favorite counts

        populateActionCounts(mListViewHeader, post);

        // Add populated header view to report timeline

        commentList.addHeaderView(mListViewHeader, null, false);

    }

    private void populateActionCounts(final ViewGroup viewGroup, final Report post) {

        mCommentCount = (RelativeLayout) viewGroup.findViewById(R.id.commentCount);
        mFullCommentCount = (TextView) viewGroup.findViewById(R.id.fullCommentCount);
        mFavoriteCount = (RelativeLayout) viewGroup.findViewById(R.id.favoriteCount);
        mFullFavoriteCount = (TextView) viewGroup.findViewById(R.id.fullFavoriteCount);

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

        RestAdapter restAdapter = ReportService.restAdapter;

        ReportService service = restAdapter.create(ReportService.class);

        service.getSingleReport("", "application/json", postId, new Callback<Report>() {

            @Override
            public void success(Report report, Response response) {

                mPost = report;

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

        final String accessToken = sharedPreferences.getString("access_token", "");

        Log.d("", accessToken);

        // Create order_by list and add a sort parameter

        List<QuerySort> queryOrder = new ArrayList<QuerySort>();

        QuerySort querySort = new QuerySort("created", "desc");

        queryOrder.add(querySort);

        // Create query string from new QueryParams

        QueryParams queryParams = new QueryParams(null, queryOrder);

        String query = new Gson().toJson(queryParams);

        Log.d("URL", query);

        ReportService service = ReportService.restAdapter.create(ReportService.class);

//        commentListContainer.setRefreshing(true);

        service.getReportComments(accessToken, "application/json", mPost.id, page, limit, query, new Callback<CommentCollection>() {

            @Override
            public void success(CommentCollection commentCollection, Response response) {

                List<Comment> comments = commentCollection.getFeatures();

                Log.v("list", comments.toString());

                commentCollectionList = comments;

                populateComments(commentCollectionList);

//                commentListContainer.setRefreshing(false);

            }

            @Override
            public void failure(RetrofitError error) {

//                commentListContainer.setRefreshing(false);

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

        commentAdapter = new CommentAdapter(this, comments);

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
        sharedPreferences.edit().putBoolean("markerDetailOpen", false).apply();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        sharedPreferences.edit().putBoolean("markerDetailOpen", false).apply();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        sharedPreferences.edit().putBoolean("markerDetailOpen", false).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        ButterKnife.unbind(this);
        sharedPreferences.edit().putBoolean("markerDetailOpen", false).apply();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}