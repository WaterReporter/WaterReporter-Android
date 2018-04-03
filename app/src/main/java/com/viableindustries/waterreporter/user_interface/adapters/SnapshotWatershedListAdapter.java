package com.viableindustries.waterreporter.user_interface.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.api.models.campaign.Campaign;
import com.viableindustries.waterreporter.api.models.snapshot.SnapshotShallowWatershed;
import com.viableindustries.waterreporter.api.models.territory.Territory;
import com.viableindustries.waterreporter.api.models.territory.TerritoryProperties;
import com.viableindustries.waterreporter.constants.HucStates;
import com.viableindustries.waterreporter.user_interface.dialogs.CampaignExtrasBottomSheetDialogFragment;
import com.viableindustries.waterreporter.user_interface.dialogs.WatershedExtrasBottomSheetDialogFragment;
import com.viableindustries.waterreporter.user_interface.listeners.CampaignProfileListener;
import com.viableindustries.waterreporter.user_interface.listeners.TerritoryProfileListener;
import com.viableindustries.waterreporter.utilities.ModelStorage;
import com.viableindustries.waterreporter.utilities.UtilityMethods;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by brendanmcintyre on 3/26/18.
 */

public class SnapshotWatershedListAdapter extends ArrayAdapter<SnapshotShallowWatershed> {

    private final Context mContext;

    private final SharedPreferences mSharedPreferences;

    final private FragmentManager mFragmentManager;

    protected String name;

    protected int id;

    private final List<SnapshotShallowWatershed> sourceList;

    public SnapshotWatershedListAdapter(Context aContext,
                                        List<SnapshotShallowWatershed> features,
                                        FragmentManager fragmentManager) {

        super(aContext, 0, features);

        this.sourceList = features;

        this.mContext = aContext;

        this.mSharedPreferences = mContext.getSharedPreferences(mContext.getPackageName(), 0);

        this.mFragmentManager = fragmentManager;

    }

    private static class ViewHolder {
        TextView featureName;
        TextView postCount;
        TextView stateList;
        RelativeLayout extraActions;
        ImageView extraActionsIconView;
        RelativeLayout shallowWatershedItem;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        SnapshotWatershedListAdapter.ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.campaign_watershed_item, parent, false);

            viewHolder = new SnapshotWatershedListAdapter.ViewHolder();

            viewHolder.featureName = (TextView) convertView.findViewById(R.id.featureName);
            viewHolder.postCount = (TextView) convertView.findViewById(R.id.postCount);
            viewHolder.stateList = (TextView) convertView.findViewById(R.id.stateList);
            viewHolder.extraActions = (RelativeLayout) convertView.findViewById(R.id.extraActions);
            viewHolder.extraActionsIconView = (ImageView) convertView.findViewById(R.id.extraActionsIconView);
            viewHolder.shallowWatershedItem = (RelativeLayout) convertView.findViewById(R.id.shallowWatershedItem);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (SnapshotWatershedListAdapter.ViewHolder) convertView.getTag();

        }

        final SnapshotShallowWatershed campaignWatershed = sourceList.get(position);

        // Populate layout elements

        viewHolder.featureName.setText(campaignWatershed.name);

        viewHolder.stateList.setText(HucStates.STATES.get(campaignWatershed.code));

        String subText = UtilityMethods.makeSecondaryListPostCountText(mContext.getResources(),
                campaignWatershed.posts, campaignWatershed.last_active);

        viewHolder.postCount.setTextColor(ContextCompat.getColor(mContext, R.color.black_54));

        viewHolder.postCount.setText(subText);

        long timeDelta = UtilityMethods.timeDelta(
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
                campaignWatershed.last_active);

        Log.v("time-delta", timeDelta + "");

        if (timeDelta > 0 && timeDelta < (DateUtils.DAY_IN_MILLIS * 30)) {

            viewHolder.postCount.setTextColor(ContextCompat.getColor(mContext, R.color.post_count_orange));

        }

        // Add click listeners to layout elements

        //
        // This is really important! Here we're working without a complete
        // `Territory` object but can take advantage of the fallback in
        // `Watershed{Collection}Activity` by creating an empty
        // `Territory` and setting its id to the shallow watershed object's
        // `code` attribute.

        TerritoryProperties territoryProperties = new TerritoryProperties();

        territoryProperties.huc_8_code = campaignWatershed.code;
        territoryProperties.huc_8_name = campaignWatershed.name;

        final Territory territory = new Territory();

        territory.properties = territoryProperties;

        viewHolder.shallowWatershedItem.setOnClickListener(new TerritoryProfileListener(mContext, territory));

        viewHolder.extraActions.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                ModelStorage.storeModel(mSharedPreferences, territory, "stored_territory");

                WatershedExtrasBottomSheetDialogFragment watershedExtrasBottomSheetDialogFragment =
                        new WatershedExtrasBottomSheetDialogFragment();

                watershedExtrasBottomSheetDialogFragment.show(mFragmentManager, "watershed-extras-dialog");

            }

        });

        return convertView;

    }

}