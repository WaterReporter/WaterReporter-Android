package com.viableindustries.waterreporter.data.objects.organization;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.data.objects.GeometryResponse;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 1/28/16.
 */
public class Organization implements Serializable {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("id")
    public int id;

    @SerializedName("properties")
    public OrganizationProperties properties;

    @SerializedName("type")
    public String type;

}
