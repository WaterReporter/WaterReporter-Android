package com.viableindustries.waterreporter.api.models.snapshot;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 4/2/18.
 */

public class SnapshotShallowCampaign {

    @SerializedName("description")
    public String description;

    @SerializedName("id")
    public int id;

    @SerializedName("last_active")
    public String last_active;

    @SerializedName("name")
    public String name;

    @SerializedName("picture")
    public String picture;

    @SerializedName("posts")
    public int posts;

}
