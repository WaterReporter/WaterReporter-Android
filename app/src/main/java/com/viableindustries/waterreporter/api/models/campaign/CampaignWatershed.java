package com.viableindustries.waterreporter.api.models.campaign;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.post.ReportPhoto;
import com.viableindustries.waterreporter.api.models.user.User;

import java.util.List;

/**
 * Created by brendanmcintyre on 3/26/18.
 */

public class CampaignWatershed {

    @SerializedName("code")
    public int code;

    @SerializedName("last_active")
    public String last_active;

    @SerializedName("name")
    public String name;

    @SerializedName("posts")
    public int posts;

}

