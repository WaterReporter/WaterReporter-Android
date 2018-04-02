package com.viableindustries.waterreporter.user_interface.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.api.models.campaign.Campaign;
import com.viableindustries.waterreporter.api.models.snapshot.SnapshotShallowCampaign;
import com.viableindustries.waterreporter.user_interface.listeners.CampaignProfileListener;

import java.util.List;

/**
 * Created by brendanmcintyre on 4/2/18.
 */

public class SnapshotShallowCampaignListAdapter extends ArrayAdapter<SnapshotShallowCampaign> {

    private final SharedPreferences mSharedPreferences;

    final private FragmentManager mFragmentManager;

    private final Context mContext;

    protected String name;

    protected int id;

    private final List<SnapshotShallowCampaign> sourceList;

    public SnapshotShallowCampaignListAdapter(Context aContext, List<SnapshotShallowCampaign> features,
                               FragmentManager fragmentManager) {

        super(aContext, 0, features);

        this.sourceList = features;

        this.mContext = aContext;

        this.mSharedPreferences = mContext.getSharedPreferences(mContext.getPackageName(), 0);

        this.mFragmentManager = fragmentManager;

    }

    private static class ViewHolder {
        ImageView campaignImage;
        TextView campaignName;
        LinearLayout campaignItem;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        SnapshotShallowCampaignListAdapter.ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.campaign_list_item, parent, false);

            viewHolder = new SnapshotShallowCampaignListAdapter.ViewHolder();

            viewHolder.campaignImage = (ImageView) convertView.findViewById(R.id.campaignImage);
            viewHolder.campaignName = (TextView) convertView.findViewById(R.id.campaignName);
            viewHolder.campaignItem = (LinearLayout) convertView.findViewById(R.id.campaignItem);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (SnapshotShallowCampaignListAdapter.ViewHolder) convertView.getTag();

        }

        final SnapshotShallowCampaign snapshotShallowCampaign = sourceList.get(position);

        // Populate layout elements

        viewHolder.campaignName.setText(snapshotShallowCampaign.name);

        Picasso.with(mContext).load(snapshotShallowCampaign.picture).into(viewHolder.campaignImage);

        // Add click listeners to layout elements

        Campaign campaign = new Campaign();

        campaign.id = snapshotShallowCampaign.id;

        viewHolder.campaignItem.setOnClickListener(new CampaignProfileListener(mContext, campaign));

        return convertView;

    }

}
