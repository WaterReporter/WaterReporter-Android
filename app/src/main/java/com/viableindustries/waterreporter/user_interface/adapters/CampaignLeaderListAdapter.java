package com.viableindustries.waterreporter.user_interface.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.UserProfileActivity;
import com.viableindustries.waterreporter.api.models.snapshot.CampaignLeader;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.user_interface.listeners.UserProfileListener;
import com.viableindustries.waterreporter.utilities.CircleTransform;

import java.util.List;

/**
 * Created by brendanmcintyre on 3/22/18.
 */

public class CampaignLeaderListAdapter extends ArrayAdapter<CampaignLeader> {

    private final Context mContext;

    protected String name;

    protected int id;

    private final List<CampaignLeader> sourceList;

    public CampaignLeaderListAdapter(Context aContext, List<CampaignLeader> features) {

        super(aContext, 0, features);

        this.sourceList = features;

        this.mContext = aContext;

    }

    private static class ViewHolder {
        ImageView userAvatar;
        TextView postCount;
        FrameLayout leaderBoardItem;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        CampaignLeaderListAdapter.ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.campaign_leader_item, parent, false);

            viewHolder = new CampaignLeaderListAdapter.ViewHolder();

            viewHolder.userAvatar = (ImageView) convertView.findViewById(R.id.userAvatar);
            viewHolder.postCount = (TextView) convertView.findViewById(R.id.postCount);
            viewHolder.leaderBoardItem = (FrameLayout) convertView.findViewById(R.id.leaderBoardItem);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (CampaignLeaderListAdapter.ViewHolder) convertView.getTag();

        }

        final CampaignLeader campaignLeader = sourceList.get(position);

        // Populate layout elements

        viewHolder.postCount.setText(String.valueOf(campaignLeader.posts));

        Picasso.with(mContext)
                .load(campaignLeader.picture)
                .placeholder(R.drawable.user_avatar_placeholder)
                .transform(new CircleTransform())
                .into(viewHolder.userAvatar);

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

        User user = User.createUser(campaignLeader.id, null);

        viewHolder.leaderBoardItem.setOnClickListener(new UserProfileListener(mContext, user));

        return convertView;

    }

}
