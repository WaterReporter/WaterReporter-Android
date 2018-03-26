package com.viableindustries.waterreporter.user_interface.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.api.models.campaign.CampaignWatershed;
import com.viableindustries.waterreporter.constants.HucStates;
import com.viableindustries.waterreporter.utilities.UtilityMethods;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by brendanmcintyre on 3/26/18.
 */

public class CampaignWatershedListAdapter extends ArrayAdapter<CampaignWatershed> {

    private final Context mContext;

    protected String name;

    protected int id;

    private final List<CampaignWatershed> sourceList;

    public CampaignWatershedListAdapter(Context aContext, List<CampaignWatershed> features) {

        super(aContext, 0, features);

        this.sourceList = features;

        this.mContext = aContext;

    }

    private static class ViewHolder {
        TextView featureName;
        TextView postCount;
        TextView stateList;
        LinearLayout campaignWatershedItem;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        CampaignWatershedListAdapter.ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.campaign_watershed_item, parent, false);

            viewHolder = new CampaignWatershedListAdapter.ViewHolder();

            viewHolder.featureName = (TextView) convertView.findViewById(R.id.featureName);
            viewHolder.postCount = (TextView) convertView.findViewById(R.id.postCount);
            viewHolder.stateList = (TextView) convertView.findViewById(R.id.stateList);
            viewHolder.campaignWatershedItem = (LinearLayout) convertView.findViewById(R.id.campaignWatershedItem);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (CampaignWatershedListAdapter.ViewHolder) convertView.getTag();

        }

        final CampaignWatershed campaignWatershed = sourceList.get(position);

        // Populate layout elements

        viewHolder.featureName.setText(campaignWatershed.name);

        viewHolder.stateList.setText(HucStates.STATES.get(campaignWatershed.code));

        String subText = UtilityMethods.makeSecondaryListPostCountText(mContext.getResources(),
                campaignWatershed.posts, campaignWatershed.last_active);

        viewHolder.postCount.setText(subText);

        long timeDelta = UtilityMethods.timeDelta(
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
                campaignWatershed.last_active);

        Log.v("time-delta", timeDelta + "");

        if (timeDelta > 0 && timeDelta < (DateUtils.DAY_IN_MILLIS * 30)) {

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