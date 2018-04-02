package com.viableindustries.waterreporter.api.models.territory;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.geometry.GeometryResponse;

/**
 * Created by brendanmcintyre on 8/26/15.
 */
public class Territory {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("id")
    public long id;

    @SerializedName("properties")
    public TerritoryProperties properties;

    @SerializedName("type")
    public String type;

}
