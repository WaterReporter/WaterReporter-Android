package com.viableindustries.waterreporter.api.models.hashtag;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.post.Report;

import java.util.List;

/**
 * Created by brendanmcintyre on 8/26/15.
 */
public class HashTagProperties {

    @SerializedName("id")
    public int id;

    @SerializedName("tag")
    public String tag;

    @SerializedName("reports")
    public List<Report> reports;

}
