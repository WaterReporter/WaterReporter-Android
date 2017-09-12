package com.viableindustries.waterreporter.data.objects.comment;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.data.objects.GeometryResponse;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 8/24/15.
 */
public class Comment implements Serializable {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("id")
    public int id;

    @SerializedName("type")
    public String type;

    @SerializedName("properties")
    public CommentProperties properties;

}