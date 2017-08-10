package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by brendanmcintyre on 8/26/15.
 */
public class CommentProperties implements Serializable {

    @SerializedName("body")
    public String body;

    @SerializedName("created")
    public String created;

    @SerializedName("id")
    public int id;

    @SerializedName("images")
    public List<ReportPhoto> images;

    @SerializedName("owner_id")
    public int owner_id;

    @SerializedName("owner")
    public User owner;

    @SerializedName("post_id")
    public int post_id;

    @SerializedName("report")
    public Report report;

    @SerializedName("post_state")
    public String post_state;

    @SerializedName("status")
    public String status;

    @SerializedName("subject")
    public String subject;

    @SerializedName("updated")
    public String updated;

}
