package com.viableindustries.waterreporter.api.models.field_book;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.campaign.CampaignFormField;

import java.util.List;

/**
 * Created by brendanmcintyre on 3/29/18.
 */

public class FieldBookPostBody {

    @SerializedName("data")
    public List<CampaignFormField> data;

    @SerializedName("report_id")
    public int report_id;

}
