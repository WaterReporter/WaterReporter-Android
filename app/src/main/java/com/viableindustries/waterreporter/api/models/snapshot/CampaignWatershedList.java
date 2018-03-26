package com.viableindustries.waterreporter.api.models.snapshot;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.campaign.CampaignWatershed;

import java.util.List;

/**
 * Created by brendanmcintyre on 3/26/18.
 */

public class CampaignWatershedList {

    @SerializedName("results")
    public List<CampaignWatershed> watersheds;

}