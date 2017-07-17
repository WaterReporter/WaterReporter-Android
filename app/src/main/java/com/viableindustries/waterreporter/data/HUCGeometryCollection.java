package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by brendanmcintyre on 7/17/17.
 */

public class HUCGeometryCollection implements Serializable {

    @SerializedName("features")
    public List<HUCFeature> features;

    @SerializedName("type")
    public String type;

}
