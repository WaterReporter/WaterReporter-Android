package com.viableindustries.waterreporter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Favorite;
import com.viableindustries.waterreporter.data.FavoritePostBody;
import com.viableindustries.waterreporter.data.FavoriteService;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.HtmlCompat;
import com.viableindustries.waterreporter.data.OpenGraph;
import com.viableindustries.waterreporter.data.OpenGraphObject;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationProfileListener;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportPhoto;
import com.viableindustries.waterreporter.data.TerritoryProfileListener;
import com.viableindustries.waterreporter.data.UserHolder;
import com.viableindustries.waterreporter.data.UserProfileHeader;
import com.viableindustries.waterreporter.data.UserProfileListener;
import com.viableindustries.waterreporter.dialogs.ReportActionDialogListener;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.viableindustries.waterreporter.R.id.parent;

/**
 * Created by brendanmcintyre on 8/1/17.
 */

public class PostCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private boolean mHasHeader;

    private List<Report> mDataset;

    private Context mContext;

    private SharedPreferences mPreferences;

    private boolean mIsProfile;

    private int mHeaderId;

    private OnLoadMoreListener loadMoreListener;
    private boolean isLoading = false;
    private boolean isMoreDataAvailable = true;

    // Provide a suitable constructor (depends on the kind of dataset)
    public PostCardAdapter(Activity activity, List<Report> reports, boolean isProfile, boolean hasHeader, int headerId) {
        mDataset = reports;
        mContext = activity;
        mPreferences = mContext.getSharedPreferences(mContext.getPackageName(), 0);
        mIsProfile = isProfile;
        mHasHeader = hasHeader;
        mHeaderId = headerId;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {

        if (viewType == TYPE_HEADER) {

            LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_profile_header, parent, false);

            return new UserHeaderHolder(v);

        }

        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.timeline_item, parent, false);

        return new PostCardHolder(v);


    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof UserHeaderHolder) {

            UserHeaderHolder userHeaderHolder = (UserHeaderHolder) holder;

            final UserProfileHeader profileHeader = new UserProfileHeader();

            profileHeader.setData(UserHolder.getUser());

            userHeaderHolder.bindData(mContext,
                    profileHeader.getName(),
                    profileHeader.getTitle(),
                    profileHeader.getDescription(),
                    profileHeader.getAvatarUrl());

        } else if (holder instanceof PostCardHolder) {

            PostCardHolder postCardHolder = (PostCardHolder) holder;

            // Get item from dataset at `position`
            final Report post = mDataset.get(position);

            postCardHolder.bindData(post, mContext, mPreferences, mIsProfile);

        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    static class LoadHolder extends RecyclerView.ViewHolder {
        public LoadHolder(View itemView) {
            super(itemView);
        }
    }

    public void setMoreDataAvailable(boolean moreDataAvailable) {
        isMoreDataAvailable = moreDataAvailable;
    }

    /* notifyDataSetChanged is final method so we can't override it
         call adapter.notifyDataChanged(); after update the list
         */
    public void notifyDataChanged() {
        notifyDataSetChanged();
        isLoading = false;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position) && mHasHeader) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    interface OnLoadMoreListener {
        void onLoadMore();
    }

    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

}