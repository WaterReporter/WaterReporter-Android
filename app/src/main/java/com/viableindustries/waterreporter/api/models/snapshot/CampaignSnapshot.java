package com.viableindustries.waterreporter.api.models.snapshot;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.group.Group;
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.post.ReportPhoto;
import com.viableindustries.waterreporter.api.models.role.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brendanmcintyre on 3/22/18.
 */

public class CampaignSnapshot {

    @SerializedName("actions")
    public int actions;

    @SerializedName("comments")
    public int comments;

    @SerializedName("likes")
    public int likes;

    @SerializedName("members")
    public int members;

    @SerializedName("organizations")
    public int groups;

    @SerializedName("posts")
    public int posts;

    @SerializedName("shares")
    public int shares;

}
