package com.viableindustries.waterreporter.data.objects.territory;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by brendanmcintyre on 7/17/17.
 */

public class HucGeometryCollection implements Serializable {

    @SerializedName("features")
    public List<HucFeature> features;

    @SerializedName("type")
    public String type;

}
