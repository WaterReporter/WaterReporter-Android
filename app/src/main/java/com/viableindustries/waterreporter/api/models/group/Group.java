package com.viableindustries.waterreporter.api.models.group;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.geometry.GeometryResponse;
import com.viableindustries.waterreporter.api.models.organization.OrganizationProperties;

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
