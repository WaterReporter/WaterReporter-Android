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
import com.viableindustries.waterreporter.api.models.campaign.CampaignMember;
import com.viableindustries.waterreporter.utilities.CircleTransform;

import java.util.List;

/**
 * Created by brendanmcintyre on 3/26/18.
 */

public class CampaignMemberListAdapter extends ArrayAdapter<CampaignMember> {

    private final Context mContext;

    protected String name;

    protected int id;

    private final List<CampaignMember> sourceList;

    public CampaignMemberListAdapter(Context aContext, List<CampaignMember> features) {

        super(aContext, 0, features);

        this.sourceList = features;

        this.mContext = aContext;

    }

    private static class ViewHolder {
        ImageView featureIcon;
        TextView featureName;
        //        TextView postCount;
        LinearLayout campaignMemberItem;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        CampaignMemberListAdapter.ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.campaign_member_item, parent, false);

            viewHolder = new CampaignMemberListAdapter.ViewHolder();

            viewHolder.featureIcon = (ImageView) convertView.findViewById(R.id.featureIcon);
            viewHolder.featureName = (TextView) convertView.findViewById(R.id.featureName);
            viewHolder.campaignMemberItem = (LinearLayout) convertView.findViewById(R.id.campaignMemberItem);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (CampaignMemberListAdapter.ViewHolder) convertView.getTag();

        }

        final CampaignMember campaignMember = sourceList.get(position);

        // Populate layout elements

//        viewHolder.postCount.setText(String.valueOf(campaignLeader.posts));

        viewHolder.featureName.setText(campaignMember.name);

        Picasso.with(mContext)
                .load(campaignMember.picture)
                .placeholder(R.drawable.user_avatar_placeholder)
                .transform(new CircleTransform())
                .into(viewHolder.featureIcon);

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