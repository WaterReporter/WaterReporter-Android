package com.viableindustries.waterreporter.data.objects.territory;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.data.objects.GeometryResponse;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 8/26/15.
 */
public class Territory implements Serializable {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("id")
    public int id;

    @SerializedName("properties")
    public TerritoryProperties properties;

    @SerializedName("type")
    public String type;

}
