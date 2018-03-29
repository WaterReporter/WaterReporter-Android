package com.viableindustries.waterreporter.api.models.field_book;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.campaign.CampaignFormField;

import java.util.List;

/**
 * Created by brendanmcintyre on 3/29/18.
 */

public class FieldBook {

    @SerializedName("created")
    public String created;

    @SerializedName("data")
    public List<CampaignFormField> data;

    @SerializedName("id")
    public int id;

    @SerializedName("owner_id")
    public int owner_id;

    @SerializedName("report_id")
    public int report_id;

    @SerializedName("updated")
    public String updated;

}
