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

    @SerializedName("owner_id")
    public int owner_id;

    @SerializedName("report_id")
    public int report_id;

    @SerializedName("report_state")
    public String report_state;

    @SerializedName("status")
    public String status;

    @SerializedName("subject")
    public String subject;

    @SerializedName("updated")
    public String updated;

}
