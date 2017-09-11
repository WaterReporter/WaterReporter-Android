package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 8/24/15.
 */
public class Comment implements Serializable {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("id")
    public final int id;

    @SerializedName("type")
    public String type;

    @SerializedName("properties")
    public final CommentProperties properties;

}