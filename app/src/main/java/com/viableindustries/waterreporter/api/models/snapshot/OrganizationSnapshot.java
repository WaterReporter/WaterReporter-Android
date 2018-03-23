package com.viableindustries.waterreporter.api.models.snapshot;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 3/23/18.
 */

public class OrganizationSnapshot {

    @SerializedName("actions")
    public int actions;

    @SerializedName("comments")
    public int comments;

    @SerializedName("likes")
    public int likes;

    @SerializedName("members")
    public int members;

    @SerializedName("posts")
    public int posts;

    @SerializedName("shares")
    public int shares;

    @SerializedName("watersheds")
    public int watersheds;

}
