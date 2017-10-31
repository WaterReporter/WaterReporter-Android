package com.viableindustries.waterreporter.user_interface.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.api.models.open_graph.OpenGraphProperties;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.user_interface.listeners.PostDetailListener;

import java.util.List;

/**
 * Created by brendanmcintyre on 7/25/17.
 */

public class MarkerCardAdapter extends RecyclerView.Adapter<MarkerCardAdapter.ViewHolder> {

    private final List<Report> mDataset;

    private final Context mContext;

    // Provide a reference to the views for each api item
    // Complex api items may need more than one view per item, and
    // you provide access to all the views for a api item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each api item is just a string in this case
        public final FrameLayout cardView;
        public final ImageView postImage;
        public final RelativeLayout openLink;
        public final RelativeLayout actionBadge;

        public ViewHolder(FrameLayout v) {
            super(v);
            cardView = v;
            postImage = (ImageView) v.findViewById(R.id.postImage);
            openLink = (RelativeLayout) v.findViewById(R.id.openLink);
            actionBadge = (RelativeLayout) v.findViewById(R.id.actionBadge);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MarkerCardAdapter(Activity activity, List<Report> reports) {
        mDataset = reports;
        mContext = activity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MarkerCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        FrameLayout v = (FrameLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.marker_card_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
//        ...
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Report post = mDataset.get(position);

        // Set action badge state

        TimelineAdapterHelpers.setActionBadge(post, holder.actionBadge);

        if (post.properties.open_graph.size() > 0) {

            Log.v("open--graph", "post has OG data");

            holder.openLink.setVisibility(View.VISIBLE);

            final OpenGraphProperties openGraphProperties = post.properties.open_graph.get(0).properties;

            String imageUrl = openGraphProperties.imageUrl;

            Picasso
                    .with(mContext)
                    .load(imageUrl)
                    .resize(120, 120)
                    .error(R.drawable.open_graph_placeholder)
                    .into(holder.postImage);

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri webpage = Uri.parse(openGraphProperties.url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                    if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                        mContext.startActivity(intent);
                    }
                }
            });

        } else {

            holder.openLink.setVisibility(View.GONE);

            Picasso
                    .with(mContext)
                    .load(post.properties.images.get(0).properties.thumbnail_retina)
                    .into(holder.postImage);

            holder.postImage.setOnClickListener(new PostDetailListener(mContext, post));

        }

        holder.cardView.setTag(post.id);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}

