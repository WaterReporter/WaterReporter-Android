package com.viableindustries.waterreporter.user_interface.listeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.viableindustries.waterreporter.CampaignProfileActivity;
import com.viableindustries.waterreporter.api.models.campaign.Campaign;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by brendanmcintyre on 3/22/18.
 */

public class CampaignProfileListener implements View.OnClickListener {

    private final Context mContext;

    private final Campaign mCampaign;

    public CampaignProfileListener(Context aContext, Campaign aCampaign) {
        this.mContext = aContext;
        this.mCampaign = aCampaign;
    }

    @Override
    public void onClick(View view) {

        // Write model to temporary storage in SharedPreferences

        ModelStorage.storeModel(mContext.getSharedPreferences(mContext.getPackageName(), MODE_PRIVATE), mCampaign, "stored_campaign");

        Intent intent = new Intent(mContext, CampaignProfileActivity.class);

        mContext.startActivity(intent);

    }

}
