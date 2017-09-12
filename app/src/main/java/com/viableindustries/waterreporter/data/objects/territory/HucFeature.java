package com.viableindustries.waterreporter.data.objects.territory;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 7/17/17.
 */

public class HucFeature implements Serializable {

//    @SerializedName("geometry")
//    public HucGeometry geometry;

    @SerializedName("properties")
    public HucProperties properties;

    @SerializedName("type")
    public String type;

}
