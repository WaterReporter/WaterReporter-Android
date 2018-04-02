package com.viableindustries.waterreporter.api.models.snapshot;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by brendanmcintyre on 4/2/18.
 */

public class SnapshotCampaignList {

    @SerializedName("results")
    public List<SnapshotShallowCampaign> campaigns;

}
