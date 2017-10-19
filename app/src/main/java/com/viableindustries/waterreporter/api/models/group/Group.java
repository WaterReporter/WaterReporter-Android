package com.viableindustries.waterreporter.api.models.group;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.geometry.GeometryResponse;

/**
 * Created by brendanmcintyre on 10/16/17.
 */

public class Group {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("id")
    public int id;

    @SerializedName("properties")
    public GroupProperties properties;

    @SerializedName("type")
    public String type;

}
