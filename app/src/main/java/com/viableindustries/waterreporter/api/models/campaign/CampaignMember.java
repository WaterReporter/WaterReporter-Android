package com.viableindustries.waterreporter.api.models.campaign;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.post.ReportPhoto;
import com.viableindustries.waterreporter.api.models.user.User;

import java.util.List;

/**
 * Created by brendanmcintyre on 3/26/18.
 */

public class CampaignMember {

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
