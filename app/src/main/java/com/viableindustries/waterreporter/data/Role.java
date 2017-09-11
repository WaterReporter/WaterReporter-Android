package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 10/31/16.
 */

public class Role {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("id")
    public int id;

    @SerializedName("type")
    public String type;

    @SerializedName("properties")
    public final RoleProperties properties;

}
