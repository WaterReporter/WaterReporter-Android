package com.viableindustries.waterreporter.data.objects.territory;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by brendanmcintyre on 7/17/17.
 */

public class HucGeometry implements Serializable {

    @SerializedName("coordinates")
    public List<List<List<Double>>> coordinates;

    @SerializedName("type")
    public String type;

}
