package com.viableindustries.waterreporter.api.models.territory;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by brendanmcintyre on 7/17/17.
 */

public class HucGeometryCollection {

    @SerializedName("features")
    public List<HucFeature> features;

    @SerializedName("type")
    public String type;

}
