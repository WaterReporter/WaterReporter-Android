package com.viableindustries.waterreporter.api.models.organization;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.geometry.GeometryResponse;

/**
 * Created by brendanmcintyre on 1/28/16.
 */
public class Organization {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("id")
    public int id;

    @SerializedName("properties")
    public OrganizationProperties properties;

    @SerializedName("type")
    public String type;

}
