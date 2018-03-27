package com.viableindustries.waterreporter.api.models.campaign;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by brendanmcintyre on 3/27/18.
 */

public class CampaignForm {

    @SerializedName("campaign_id")
    public String campaign_id;

    @SerializedName("created")
    public String created;

    @SerializedName("description")
    public String description;

    @SerializedName("fields")
    public List<CampaignFormField> fields;

    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("updated")
    public String updated;

}
