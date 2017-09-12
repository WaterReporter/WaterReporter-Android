package com.viableindustries.waterreporter.data.objects.hashtag;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.data.objects.GeometryResponse;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 8/26/15.
 */
public class HashTag implements Serializable {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("id")
    public int id;

    @SerializedName("type")
    public String type;

    @SerializedName("properties")
    public HashTagProperties properties;

}
