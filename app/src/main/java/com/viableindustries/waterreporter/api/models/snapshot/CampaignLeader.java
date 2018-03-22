package com.viableindustries.waterreporter.api.models.snapshot;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 3/22/18.
 */

public class CampaignLeader {

    @SerializedName("description")
    public String description;

    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("organization_name")
    public String organization_name;

    @SerializedName("picture")
    public String picture;

    @SerializedName("posts")
    public int posts;

    @SerializedName("title")
    public String title;

}
