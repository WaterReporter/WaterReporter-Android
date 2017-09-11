package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by brendanmcintyre on 8/26/15.
 */
public class HashTagProperties implements Serializable {

    @SerializedName("id")
    public int id;

    @SerializedName("tag")
    public final String tag;

    @SerializedName("reports")
    public final List<Report> reports;

}
