package com.viableindustries.waterreporter.user_interface.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
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
import com.viableindustries.waterreporter.user_interface.listeners.CampaignProfileListener;
import com.viableindustries.waterreporter.user_interface.listeners.OrganizationProfileListener;
import com.viableindustries.waterreporter.utilities.CircleTransform;

import java.util.List;

/**
 * Created by brendanmcintyre on 3/21/18.
 */

public class CampaignListAdapter extends ArrayAdapter<Campaign> {

    private final Context mContext;

    protected String name;

    protected int id;

    private final List<Campaign> sourceList;

    public CampaignListAdapter(Context aContext, List<Campaign> features) {

        super(aContext, 0, features);

        this.sourceList = features;

        this.mContext = aContext;

    }

    private static class ViewHolder {
        ImageView campaignImage;
        TextView campaignName;
        LinearLayout campaignItem;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        CampaignListAdapter.ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.campaign_list_item, parent, false);

            viewHolder = new CampaignListAdapter.ViewHolder();

            viewHolder.campaignImage = (ImageView) convertView.findViewById(R.id.campaignImage);
            viewHolder.campaignName = (TextView) convertView.findViewById(R.id.campaignName);
            viewHolder.campaignItem = (LinearLayout) convertView.findViewById(R.id.campaignItem);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (CampaignListAdapter.ViewHolder) convertView.getTag();

        }

        final Campaign campaign = sourceList.get(position);

        // Populate layout elements

        viewHolder.campaignName.setText(campaign.properties.name);

        Picasso.with(mContext).load(campaign.properties.picture).into(viewHolder.campaignImage);

        // Add click listeners to layout elements

        viewHolder.campaignItem.setOnClickListener(new CampaignProfileListener(mContext, campaign));

        return convertView;

    }

}
