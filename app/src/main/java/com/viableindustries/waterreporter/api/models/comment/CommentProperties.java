package com.viableindustries.waterreporter.api.models.comment;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.post.ReportPhoto;
import com.viableindustries.waterreporter.api.models.user.User;

import java.util.List;

/**
 * Created by brendanmcintyre on 8/26/15.
 */
public class CommentProperties {

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
