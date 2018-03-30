package com.viableindustries.waterreporter.api.models.field_book;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.campaign.CampaignFormField;

import java.util.List;

/**
 * Created by brendanmcintyre on 3/30/18.
 */

public class FieldBookListResponse {

    @SerializedName("features")
    public List<FieldBook> features;

}
