package com.viableindustries.waterreporter.user_interface.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.api.models.campaign.CampaignGroup;
import com.viableindustries.waterreporter.utilities.CircleTransform;
import com.viableindustries.waterreporter.utilities.UtilityMethods;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by brendanmcintyre on 3/26/18.
 */

public class CampaignGroupListAdapter extends ArrayAdapter<CampaignGroup> {

    private final Context mContext;

    protected String name;

    protected int id;

    private final List<CampaignGroup> sourceList;

    public CampaignGroupListAdapter(Context aContext, List<CampaignGroup> features) {

        super(aContext, 0, features);

        this.sourceList = features;

        this.mContext = aContext;

    }

    private static class ViewHolder {
        ImageView featureIcon;
        TextView featureName;
        TextView postCount;
        LinearLayout campaignGroupItem;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        CampaignGroupListAdapter.ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.campaign_group_item, parent, false);

            viewHolder = new CampaignGroupListAdapter.ViewHolder();

            viewHolder.featureIcon = (ImageView) convertView.findViewById(R.id.featureIcon);
            viewHolder.featureName = (TextView) convertView.findViewById(R.id.featureName);
            viewHolder.postCount = (TextView) convertView.findViewById(R.id.postCount);
            viewHolder.campaignGroupItem = (LinearLayout) convertView.findViewById(R.id.campaignGroupItem);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (CampaignGroupListAdapter.ViewHolder) convertView.getTag();

        }

        final CampaignGroup campaignGroup = sourceList.get(position);

        // Populate layout elements

        viewHolder.featureName.setText(campaignGroup.name);

        Picasso.with(mContext)
                .load(campaignGroup.picture)
                .placeholder(R.drawable.user_avatar_placeholder)
                .transform(new CircleTransform())
                .into(viewHolder.featureIcon);

        String subText = UtilityMethods.makeSecondaryListPostCountText(mContext.getResources(),
                campaignGroup.posts, campaignGroup.last_active);

        viewHolder.postCount.setText(subText);

        long timeDelta = UtilityMethods.timeDelta(
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
                campaignGroup.last_active);

        if (timeDelta < 0 && timeDelta < (DateUtils.DAY_IN_MILLIS * 30)) {

            viewHolder.postCount.setTextColor(ContextCompat.getColor(mContext, R.color.post_count_orange));

        }

        // Add click listeners to layout elements

//        viewHolder.leaderBoardItem.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent intent = new Intent(mContext, UserProfileActivity.class);
//
//                mContext.startActivity(intent);
//            }
//        });

//        User user = User.createUser(campaignLeader.id, null);

//        viewHolder.leaderBoardItem.setOnClickListener(new UserProfileListener(mContext, user));

        return convertView;

    }

}