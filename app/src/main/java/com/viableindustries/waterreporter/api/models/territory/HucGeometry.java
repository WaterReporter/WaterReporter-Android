package com.viableindustries.waterreporter.api.models.territory;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by brendanmcintyre on 7/17/17.
 */

public class HucGeometry {

    @SerializedName("coordinates")
    public List<List<List<Double>>> coordinates;

    @SerializedName("type")
    public String type;

}
