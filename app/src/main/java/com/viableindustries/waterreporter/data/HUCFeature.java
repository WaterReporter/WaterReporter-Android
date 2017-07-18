package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 7/17/17.
 */

public class HUCFeature implements Serializable {

    @SerializedName("geometry")
    public HUCGeometry geometry;

    //@SerializedName("properties")
    //public ArrayList<Double> coordinates;

    @SerializedName("type")
    public String type;

}
