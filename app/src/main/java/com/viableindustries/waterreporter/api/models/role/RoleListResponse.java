package com.viableindustries.waterreporter.api.models.role;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by brendanmcintyre on 3/30/18.
 */

public class RoleListResponse {

    @SerializedName("features")
    public List<Role> features;

}
