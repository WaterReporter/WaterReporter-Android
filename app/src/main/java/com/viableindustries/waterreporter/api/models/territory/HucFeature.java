package com.viableindustries.waterreporter.api.models.territory;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 7/17/17.
 */

public class HucFeature {

//    @SerializedName("geometry")
//    public HucGeometry geometry;

    @SerializedName("properties")
    public HucProperties properties;

    @SerializedName("type")
    public String type;

}
