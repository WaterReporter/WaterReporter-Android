package com.viableindustries.waterreporter.user_interface.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.R;
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
        public final CardView cardView;
        public final ImageView postImage;

        public ViewHolder(CardView v) {
            super(v);
            cardView = v;
            postImage = (ImageView) v.findViewById(R.id.postImage);
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
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
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

        Picasso.with(mContext).load(post.properties.images.get(0).properties.thumbnail_retina).fit().into(holder.postImage);

        holder.cardView.setTag(post.id);

        holder.postImage.setOnClickListener(new PostDetailListener(mContext, post));

//        holder.postImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ReportHolder.setReport(r);
//                Intent markerIntent = new Intent(mContext, PostDetailActivity.class);
//                mContext.startActivity(markerIntent);
//            }
//        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
