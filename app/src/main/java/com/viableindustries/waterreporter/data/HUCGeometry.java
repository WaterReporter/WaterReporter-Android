package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brendanmcintyre on 7/17/17.
 */

public class HUCGeometry implements Serializable {

    @SerializedName("coordinates")
    public List<List<List<Double>>> coordinates;

    @SerializedName("type")
    public String type;

}
