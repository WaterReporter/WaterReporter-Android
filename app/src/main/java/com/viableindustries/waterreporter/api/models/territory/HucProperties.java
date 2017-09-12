package com.viableindustries.waterreporter.api.models.territory;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by brendanmcintyre on 7/20/17.
 */

public class HucProperties {

    @SerializedName("code")
    public String code;

    @SerializedName("name")
    public String name;

    @SerializedName("bounds")
    public List<Double> bounds;

    @SerializedName("states")
    public HucStateCollection states;

}
