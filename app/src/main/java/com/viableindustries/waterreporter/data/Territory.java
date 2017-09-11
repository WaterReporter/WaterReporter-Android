package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 8/26/15.
 */
public class Territory implements Serializable {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("id")
    public final int id;

    @SerializedName("properties")
    public final TerritoryProperties properties;

    @SerializedName("type")
    public String type;

}
