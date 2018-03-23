package com.viableindustries.waterreporter.api.models.snapshot;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 3/22/18.
 */

public class UserSnapshot {

    @SerializedName("actions")
    public int actions;

    @SerializedName("comments")
    public int comments;

    @SerializedName("groups")
    public int groups;

    @SerializedName("likes")
    public int likes;

    @SerializedName("posts")
    public int posts;

    @SerializedName("shares")
    public int shares;

    @SerializedName("watersheds")
    public int watersheds;

}
