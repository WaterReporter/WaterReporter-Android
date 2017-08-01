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

public class PostCardAdapter extends RecyclerView.Adapter<PostCardHolder> {

    private List<Report> mDataset;

    private Context mContext;

    private SharedPreferences mPreferences;

    private boolean mIsProfile;

    private OnLoadMoreListener loadMoreListener;
    private boolean isLoading = false;
    private boolean isMoreDataAvailable = true;

    // Provide a suitable constructor (depends on the kind of dataset)
    public PostCardAdapter(Activity activity, List<Report> reports, boolean isProfile) {
        mDataset = reports;
        mContext = activity;
        mPreferences = mContext.getSharedPreferences(mContext.getPackageName(), 0);
        mIsProfile = isProfile;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PostCardHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.timeline_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
//        ...
        PostCardHolder vh = new PostCardHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(PostCardHolder holder, int position) {

        if (position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
            isLoading = true;
            loadMoreListener.onLoadMore();
        }
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Report post = mDataset.get(position);

        holder.bindPost(post, mContext, mPreferences, mIsProfile);

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


    interface OnLoadMoreListener {
        void onLoadMore();
    }

    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

}
