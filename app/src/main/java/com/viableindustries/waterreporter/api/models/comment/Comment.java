package com.viableindustries.waterreporter.api.models.comment;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.geometry.GeometryResponse;

/**
 * Created by brendanmcintyre on 8/24/15.
 */
public class Comment {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("id")
    public int id;

    @SerializedName("type")
    public String type;

    @SerializedName("properties")
    public CommentProperties properties;

}