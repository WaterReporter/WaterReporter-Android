package com.viableindustries.waterreporter.api.models.campaign;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.geometry.GeometryResponse;
import com.viableindustries.waterreporter.api.models.organization.OrganizationProperties;

/**
 * Created by brendanmcintyre on 3/21/18.
 */

public class Campaign {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("id")
    public int id;

    @SerializedName("properties")
    public CampaignProperties properties;

    @SerializedName("type")
    public String type;

}
