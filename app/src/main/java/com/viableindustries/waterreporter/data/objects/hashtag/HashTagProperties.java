package com.viableindustries.waterreporter.data.objects.hashtag;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.data.objects.post.Report;

import java.io.Serializable;
import java.util.List;

/**
 * Created by brendanmcintyre on 8/26/15.
 */
public class HashTagProperties implements Serializable {

    @SerializedName("id")
    public int id;

    @SerializedName("tag")
    public String tag;

    @SerializedName("reports")
    public List<Report> reports;

}
