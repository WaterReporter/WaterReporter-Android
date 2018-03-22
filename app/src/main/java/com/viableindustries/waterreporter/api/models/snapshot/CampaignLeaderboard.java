package com.viableindustries.waterreporter.api.models.snapshot;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by brendanmcintyre on 3/22/18.
 */

public class CampaignLeaderboard {

    @SerializedName("leaders")
    private List<CampaignLeader> users;

    public List<CampaignLeader> getFeatures() {

        return users;

    }

}
