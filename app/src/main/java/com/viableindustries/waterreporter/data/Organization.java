package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 1/28/16.
 */
public class Organization implements Serializable {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("id")
    public final int id;

    @SerializedName("properties")
    public final OrganizationProperties properties;

    @SerializedName("type")
    public String type;

}
